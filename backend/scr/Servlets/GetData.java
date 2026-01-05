package servlets;

import Database.ConectionDDBB;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import logic.Log;
import logic.Logic;
import logic.Measurement;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Servlet GetData.
 *
 * Atiende:
 *  - /GetData                    -> comportamiento original (medidas plantilla)
 *  - /api/dispositivos           -> lista de dispositivos (tabla dispositivo)
 *  - /api/dispositivos/*         -> estado actual (última telemetría)
 *  - /api/telemetria             -> historial por día (telemetria_dispositivo)
 */
@WebServlet(
    urlPatterns = {
        "/GetData",
        "/api/dispositivos",
        "/api/dispositivos/*",
        "/api/telemetria",
        "/api/panel/actuator",
        "/api/telemetriaHora",
        "/api/telemetriaCo2"
    }
)
public class GetData extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public GetData() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath(); // p.ej. /api/dispositivos
        String pathInfo    = request.getPathInfo();    // p.ej. /DISP_ST_0947_01/estado o null

        Log.log.info("GetData::doGet servletPath=" + servletPath + " pathInfo=" + pathInfo);

        if ("/api/dispositivos".equals(servletPath)) {
            // Parte REST de dispositivos
            if (pathInfo == null || "/".equals(pathInfo)) {
                // GET /api/dispositivos  -> lista de dispositivos
                handleDispositivosLista(request, response);
            } else {
                // GET /api/dispositivos/{id}/estado -> estado actual
                handleDispositivoEstado(request, response, pathInfo);
            }

        } else if ("/api/telemetria".equals(servletPath)) {
            // Historial por día
            handleTelemetryHistory(request, response);

        } else if ("/api/telemetriaHora".equals(servletPath)) {   // ← AÑADIR ESTO
            handleTelemetryByHour(request, response);
        } else if ("/api/telemetriaCo2".equals(servletPath)) {
            handleTelemetryByCo2(request, response);
        } else {
            // Comportamiento original de la plantilla (/GetData)
            handleMeasurements(request, response);
        }
    }

    /**
     * Comportamiento original de GetData:
     * devuelve las medidas almacenadas en la BBDD (plantilla).
     * Si no lo usas, simplemente no llames a /GetData en la práctica.
     */
    private void handleMeasurements(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Log.log.info("--Get values from DB (GetData)--");
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            try {
                ArrayList<Measurement> values = Logic.getDataFromDB();
                String jsonMeasurements = new Gson().toJson(values);
                Log.log.info("Values => " + jsonMeasurements);
                out.println(jsonMeasurements);
            } catch (NumberFormatException nfe) {
                out.println("-1");
                Log.log.error("Number Format Exception: " + nfe);
            } catch (IndexOutOfBoundsException iobe) {
                out.println("-1");
                Log.log.error("Index out of bounds Exception: " + iobe);
            } catch (Exception e) {
                out.println("-1");
                Log.log.error("Exception: " + e);
            }
        }
    }

    
    /**
     * formateamos la fecha.
     */
    private String formatTimestamp(java.sql.Timestamp ts) {
        if (ts == null) return null;

        ZoneId madrid = ZoneId.of("Europe/Madrid");
        ZonedDateTime zdt = ts.toInstant().atZone(madrid);

        return zdt.toLocalDateTime().toString().replace("T", " ");
    }
    
    /**
     * Lista de dispositivos desde la tabla "dispositivo".
     * Rellena los <select> del index.html.
     */
    private void handleDispositivosLista(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        JsonArray dispositivosJson = new JsonArray();

        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = db.obtainConnection(true);

            String sql =
                    "SELECT dispositivo_id, calle_id, tipo_dispositivo, activo " +
                    "FROM dispositivo " +
                    "ORDER BY dispositivo_id";

            ps = ConectionDDBB.getStatement(con, sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject obj = new JsonObject();

                String id    = rs.getString("dispositivo_id");
                String calle = rs.getString("calle_id");
                String tipo  = rs.getString("tipo_dispositivo");

                Boolean activo = null;
                try {
                    activo = rs.getBoolean("activo");
                    if (rs.wasNull()) {
                        activo = null;
                    }
                } catch (SQLException ignored) {}

                // Si quieres ocultar desactivados:
                if (activo != null && !activo) {
                    continue;
                }

                obj.addProperty("id", id);

                // Texto que verá el usuario en el combo
                String nombre = (tipo != null ? tipo : "");
                if (calle != null && !calle.isEmpty()) {
                    if (!nombre.isEmpty()) {
                        nombre += " ";
                    }
                    nombre += "(" + calle + ")";
                }
                if (nombre.isEmpty()) {
                    nombre = id;
                }
                obj.addProperty("nombre", nombre);
                obj.addProperty("calle", calle);

                dispositivosJson.add(obj);
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Error consultando dispositivos");
            error.addProperty("detalle", e.toString());
            Log.log.error("Error consultando dispositivos", e);

            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            db.closeConnection(con);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(dispositivosJson.toString());
        }
    }

    /**
     * Estado actual del dispositivo: última fila de telemetria_dispositivo.
     * pathInfo será algo como "/DISP_ST_0947_01/estado".
     */
    private void handleDispositivoEstado(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        // pathInfo: "/DISP_ST_0947_01/estado" -> "DISP_ST_0947_01"
        String clean = pathInfo;
        if (clean.startsWith("/")) {
            clean = clean.substring(1); // "DISP_ST_0947_01/estado"
        }
        String[] parts = clean.split("/");
        String dispositivoId = (parts.length > 0) ? parts[0] : null;

        if (dispositivoId == null || dispositivoId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Dispositivo no especificado");
            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        JsonObject obj = new JsonObject();

        try {
            con = db.obtainConnection(true);

            String sql =
                    "SELECT dispositivo_id, instante, estado_dispositivo, mensaje_actual, " +
                    "       tipo_contenido, luz_ambiente_alta, co2_ppm, " +
                    "       mensaje_calidad_aire, fecha_actualizacion_contenido " +
                    "FROM telemetria_dispositivo " +
                    "WHERE dispositivo_id = ? " +
                    "ORDER BY instante DESC " +
                    "LIMIT 1";

            ps = ConectionDDBB.getStatement(con, sql);
            ps.setString(1, dispositivoId);
            rs = ps.executeQuery();

            if (rs.next()) {
                obj.addProperty("dispositivo_id", rs.getString("dispositivo_id"));
                if (rs.getTimestamp("instante") != null) {
                    obj.addProperty("instante", formatTimestamp(rs.getTimestamp("instante")));
                }

                String mensajeActual = rs.getString("mensaje_actual");
                obj.addProperty("mensaje_actual", mensajeActual != null ? mensajeActual : "");

                String estado = rs.getString("estado_dispositivo");
                obj.addProperty("estado_dispositivo", estado != null ? estado : "SIN_DATOS");

                String tipoContenido = rs.getString("tipo_contenido");
                if (tipoContenido != null) {
                    obj.addProperty("tipo_contenido", tipoContenido);
                }

                Object co2Obj = rs.getObject("co2_ppm");
                if (co2Obj != null) {
                    obj.addProperty("co2_ppm", rs.getDouble("co2_ppm"));
                }

                boolean luzAlta = rs.getBoolean("luz_ambiente_alta");
                if (!rs.wasNull()) {
                    obj.addProperty("luz_ambiente_alta", luzAlta);
                }

                String msgCalidad = rs.getString("mensaje_calidad_aire");
                if (msgCalidad != null) {
                    obj.addProperty("mensaje_calidad_aire", msgCalidad);
                }

            } else {
                // Sin registros de telemetría para ese dispositivo
                obj.addProperty("dispositivo_id", dispositivoId);
                obj.addProperty("estado_dispositivo", "SIN_DATOS");
                obj.addProperty("mensaje_actual", "(sin datos de telemetría)");
                obj.addProperty("mensaje_calidad_aire", "Desconocida");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Error consultando telemetría");
            error.addProperty("detalle", e.toString());
            Log.log.error("Error consultando telemetría", e);

            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            db.closeConnection(con);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(obj.toString());
        }
    }

    /**
     * Historial de telemetría por día:
     * GET /api/telemetria?dispositivo=DISP_ST_0947_01&fecha=2025-11-30
     */
    private void handleTelemetryHistory(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String dispositivoId = req.getParameter("dispositivo");
        String fechaStr      = req.getParameter("fecha"); // formato esperado: YYYY-MM-DD

        if (dispositivoId == null || dispositivoId.isEmpty()
                || fechaStr == null || fechaStr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Parámetros 'dispositivo' y 'fecha' son obligatorios");
            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        JsonArray historico = new JsonArray();

        try {
            con = db.obtainConnection(true);

            String sql =
                    "SELECT dispositivo_id, instante, estado_dispositivo, mensaje_actual, " +
                    "       tipo_contenido, luz_ambiente_alta, co2_ppm, mensaje_calidad_aire " +
                    "FROM telemetria_dispositivo " +
                    "WHERE dispositivo_id = ? " +
                    "  AND instante::date = ?::date " +
                    "ORDER BY instante ASC";

            ps = ConectionDDBB.getStatement(con, sql);
            ps.setString(1, dispositivoId);
            ps.setString(2, fechaStr);
            rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject obj = new JsonObject();

                obj.addProperty("dispositivo_id", rs.getString("dispositivo_id"));
                if (rs.getTimestamp("instante") != null) {
                    obj.addProperty("instante", formatTimestamp(rs.getTimestamp("instante")));
                }

                String estado = rs.getString("estado_dispositivo");
                if (estado != null) obj.addProperty("estado_dispositivo", estado);

                String mensaje = rs.getString("mensaje_actual");
                if (mensaje != null) obj.addProperty("mensaje_actual", mensaje);

                String tipo = rs.getString("tipo_contenido");
                if (tipo != null) obj.addProperty("tipo_contenido", tipo);

                Object co2Obj = rs.getObject("co2_ppm");
                if (co2Obj != null) {
                    obj.addProperty("co2_ppm", rs.getDouble("co2_ppm"));
                }

                boolean luzAlta = rs.getBoolean("luz_ambiente_alta");
                if (!rs.wasNull()) {
                    obj.addProperty("luz_ambiente_alta", luzAlta);
                }

                String msgCalidad = rs.getString("mensaje_calidad_aire");
                if (msgCalidad != null) {
                    obj.addProperty("mensaje_calidad_aire", msgCalidad);
                }

                historico.add(obj);
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Error consultando historial de telemetría");
            error.addProperty("detalle", e.toString());
            Log.log.error("Error consultando historial de telemetría", e);

            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            db.closeConnection(con);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(historico.toString());
        }
    }
    
        /**
     * Historial filtrado por tramo horario:
     * GET /api/telemetriaHora?dispositivo=ID&fecha=YYYY-MM-DD&horaInicio=HH:MM&horaFin=HH:MM
     */
    private void handleTelemetryByHour(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String dispositivoId = req.getParameter("dispositivo");
        String fechaStr      = req.getParameter("fecha");
        String horaIni       = req.getParameter("horaInicio");
        String horaFin       = req.getParameter("horaFin");

        // Validaciones básicas
        if (dispositivoId == null || dispositivoId.isEmpty() ||
            fechaStr == null || fechaStr.isEmpty() ||
            horaIni == null  || horaFin == null) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Parámetros necesarios: dispositivo, fecha, horaInicio y horaFin");
            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        // Construimos timestamps del día seleccionado
        String desde = fechaStr + " " + horaIni;
        String hasta = fechaStr + " " + horaFin;

        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        JsonArray historico = new JsonArray();

        try {
            con = db.obtainConnection(true);

            String sql =
                "SELECT dispositivo_id, instante, estado_dispositivo, mensaje_actual, " +
                "       tipo_contenido, luz_ambiente_alta, co2_ppm, mensaje_calidad_aire " +
                "FROM telemetria_dispositivo " +
                "WHERE dispositivo_id = ? " +
                "  AND instante BETWEEN ?::timestamp AND ?::timestamp " +
                "ORDER BY instante ASC";

            ps = ConectionDDBB.getStatement(con, sql);
            ps.setString(1, dispositivoId);
            ps.setString(2, desde);
            ps.setString(3, hasta);

            rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject obj = new JsonObject();

                obj.addProperty("instante", formatTimestamp(rs.getTimestamp("instante")));
                obj.addProperty("mensaje_actual", rs.getString("mensaje_actual"));
                obj.addProperty("estado_dispositivo", rs.getString("estado_dispositivo"));
                obj.addProperty("tipo_contenido", rs.getString("tipo_contenido"));

                Object co2Obj = rs.getObject("co2_ppm");
                if (co2Obj != null) obj.addProperty("co2_ppm", rs.getDouble("co2_ppm"));

                boolean luzAlta = rs.getBoolean("luz_ambiente_alta");
                if (!rs.wasNull()) obj.addProperty("luz_ambiente_alta", luzAlta);

                String msgCalidad = rs.getString("mensaje_calidad_aire");
                if (msgCalidad != null) obj.addProperty("mensaje_calidad_aire", msgCalidad);

                historico.add(obj);
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Error consultando historial por hora");
            error.addProperty("detalle", e.toString());
            Log.log.error("Error consultando historial horario", e);

            try (PrintWriter out = resp.getWriter()) {
                out.print(error.toString());
            }
            return;

        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            db.closeConnection(con);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(historico.toString());
        }
    }

    /**
     * Filtrar telemetría por CO2:
     * GET /api/telemetriaCo2?dispositivo=ID&fecha=YYYY-MM-DD&co2Min=XXX&co2Max=YYY
     */
    private void handleTelemetryByCo2(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String dispositivo = req.getParameter("dispositivo");
        String fecha = req.getParameter("fecha");
        String co2Min = req.getParameter("co2Min");
        String co2Max = req.getParameter("co2Max");

        if (dispositivo == null || fecha == null || co2Min == null || co2Max == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Parámetros requeridos\"}");
            return;
        }

        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        JsonArray list = new JsonArray();

        try {
            con = db.obtainConnection(true);

            String sql =
                "SELECT dispositivo_id, instante, mensaje_actual, co2_ppm, luz_ambiente_alta, mensaje_calidad_aire " +
                "FROM telemetria_dispositivo " +
                "WHERE dispositivo_id = ? AND instante::date = ?::date " +
                "AND co2_ppm BETWEEN ? AND ? " +
                "ORDER BY instante ASC";

            ps = con.prepareStatement(sql);
            ps.setString(1, dispositivo);
            ps.setString(2, fecha);
            ps.setDouble(3, Double.parseDouble(co2Min));
            ps.setDouble(4, Double.parseDouble(co2Max));

            rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("instante", rs.getTimestamp("instante").toString());
                o.addProperty("mensaje_actual", rs.getString("mensaje_actual"));
                o.addProperty("co2_ppm", rs.getDouble("co2_ppm"));
                o.addProperty("luz_ambiente_alta", rs.getBoolean("luz_ambiente_alta"));
                o.addProperty("mensaje_calidad_aire", rs.getString("mensaje_calidad_aire"));
                list.add(o);
            }

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Error consultando CO2\"}");
            e.printStackTrace();
            return;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            db.closeConnection(con);
        }

        resp.getWriter().write(list.toString());
    }

    private void handlePanelActuator(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        // Puedes recibir datos como parámetros de formulario o JSON.
        // Versión simple: parámetros tipo form (application/x-www-form-urlencoded)
        String mensaje = req.getParameter("mensaje");
        String forzarStr = req.getParameter("forzar");

        boolean forzar = "true".equalsIgnoreCase(forzarStr)
                      || "on".equalsIgnoreCase(forzarStr);

        if (mensaje == null || mensaje.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"El mensaje no puede estar vacío\"}");
            }
            return;
        }

        mensaje = mensaje.trim();

        try {
            // Llama al helper de la plantilla que añadimos en MQTTPublisher
            mqtt.MQTTPublisher.publishToInformationDisplay(mensaje, forzar);

            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"ok\"}");
            }
        } catch (Exception e) {
            logic.Log.logmqtt.error("Error enviando mensaje MQTT al panel", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"No se ha podido publicar el mensaje en MQTT\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
          String servletPath = request.getServletPath(); // p.ej. /api/panel/actuator

        if ("/api/panel/actuator".equals(servletPath)) {
            // Aquí gestionamos el botón "Enviar mensaje al panel"
            handlePanelActuator(request, response);
        } else {
            // Para el resto de rutas, seguimos haciendo lo mismo de antes
            doGet(request, response);
        }
    }
}
