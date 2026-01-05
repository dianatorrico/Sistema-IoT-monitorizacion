-- Calle donde esta el dispositivo de la practica
INSERT INTO calle (
  calle_id, nombre, longitud_metros,
  latitud_inicio,  longitud_inicio,
  latitud_fin,     longitud_fin,
  doble_sentido, barrio, codigo_postal
) VALUES (
  'ST_0947',
  'Calle de Maria Odiaga',
  605.6,
  40.379452,  -3.7398847,
  40.3751788, -3.7443173,
  TRUE,
  'Latina Puerta del Angel',
  '28011'
);

-- Dispositivo (pantalla LCD)
INSERT INTO dispositivo (
  dispositivo_id, calle_id,
  latitud, longitud, distrito, barrio,
  tipo_dispositivo, activo
) VALUES (
  'DISP_ST_0947_01',     -- este ID es el que deberias usar como sensor_id/panel_id en el JSON
  'ST_0947',
  40.377315, -3.742100,  -- por ejemplo, punto medio de la calle
  'Latina',
  'Puerta del Angel',
  'panel_informativo',
  TRUE
);

-- (Opcional) Una telemetria de ejemplo, para tener algo que consultar desde la web
INSERT INTO telemetria_dispositivo (
  dispositivo_id,
  instante,
  estado_dispositivo,
  mensaje_actual,
  tipo_contenido,
  luz_ambiente_alta,
  co2_ppm,
  mensaje_calidad_aire,
  fecha_actualizacion_contenido,
  json_completo
) VALUES (
  'DISP_ST_0947_01',
  NOW(),
  'activo',
  'Mensaje inicial de prueba',
  'general',
  TRUE,                      -- como si el sensor de luz devolviera 1
  750.0,
  'Moderado',
  NOW(),
  '{"ejemplo": true}'::jsonb
);
