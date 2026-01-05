-- 1. Calles de la ciudad
CREATE TABLE IF NOT EXISTS calle (
  calle_id         VARCHAR(20) PRIMARY KEY,   -- ST_0947
  nombre           VARCHAR(100) NOT NULL,
  longitud_metros  NUMERIC(8,1),
  latitud_inicio   DOUBLE PRECISION,
  longitud_inicio  DOUBLE PRECISION,
  latitud_fin      DOUBLE PRECISION,
  longitud_fin     DOUBLE PRECISION,
  doble_sentido    BOOLEAN,
  barrio           VARCHAR(100),
  codigo_postal    VARCHAR(10)
);

-- 2. Dispositivo (pantalla LCD en nuestro caso)
CREATE TABLE IF NOT EXISTS dispositivo (
  dispositivo_id      VARCHAR(50) PRIMARY KEY,   
  calle_id            VARCHAR(20) NOT NULL REFERENCES calle(calle_id),

  -- Ubicacion
  latitud             DOUBLE PRECISION,
  longitud            DOUBLE PRECISION,
  distrito            VARCHAR(100),
  barrio              VARCHAR(100),

  -- Gestion del dispositivo
  tipo_dispositivo    VARCHAR(30),  -- 'panel_informativo'
  activo              BOOLEAN      DEFAULT TRUE
);

-- 3. Telemetria enviada por el dispositivo (cada mensaje MQTT)
CREATE TABLE IF NOT EXISTS telemetria_dispositivo (
  id                          BIGSERIAL PRIMARY KEY,
  dispositivo_id              VARCHAR(50) NOT NULL REFERENCES dispositivo(dispositivo_id),

  -- Momento del mensaje (timestamp del JSON)
  instante                    TIMESTAMPTZ NOT NULL,

  -- Lecturas y estado derivados del JSON
  estado_dispositivo          VARCHAR(20),   -- 'activo', 'apagado', 'error', etc.
  mensaje_actual              TEXT,          -- texto mostrado en la pantalla
  tipo_contenido              VARCHAR(30),   -- 'trafico', 'tiempo', 'general', ...

  -- Sensor de luz (0/1)
  luz_ambiente_alta           BOOLEAN,       -- TRUE = hay mucha luz, FALSE = poca luz

  -- Sensor de CO2
  co2_ppm                     NUMERIC(7,1),
  mensaje_calidad_aire        VARCHAR(50),
  fecha_actualizacion_contenido TIMESTAMPTZ,

  -- Mensaje completo tal cual llega del broker
  json_completo               JSONB NOT NULL
);


-- Indices utiles
CREATE INDEX IF NOT EXISTS idx_telemetria_disp_instante
  ON telemetria_dispositivo (instante);

CREATE INDEX IF NOT EXISTS idx_telemetria_disp_tipo_contenido
  ON telemetria_dispositivo (tipo_contenido);

CREATE INDEX IF NOT EXISTS idx_telemetria_disp_disp_instante
  ON telemetria_dispositivo (dispositivo_id, instante);

CREATE INDEX IF NOT EXISTS idx_telemetria_disp_json_gin
  ON telemetria_dispositivo USING GIN (json_completo);
