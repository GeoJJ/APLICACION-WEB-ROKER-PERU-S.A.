-- 1. Insertar Roles por defecto
-- Usamos ON CONFLICT DO NOTHING para que el script no falle si reinicias el backend múltiples veces.
INSERT INTO rol (id_rol, nombre_rol) VALUES (1, 'ADMINISTRADOR') ON CONFLICT (id_rol) DO NOTHING;
INSERT INTO rol (id_rol, nombre_rol) VALUES (2, 'VENDEDOR') ON CONFLICT (id_rol) DO NOTHING;
INSERT INTO rol (id_rol, nombre_rol) VALUES (3, 'REPARTIDOR') ON CONFLICT (id_rol) DO NOTHING;
INSERT INTO rol (id_rol, nombre_rol) VALUES (4, 'CLIENTE') ON CONFLICT (id_rol) DO NOTHING;

-- Sincronizar la secuencia de la tabla rol para futuras inserciones desde React
SELECT setval('rol_id_rol_seq', (SELECT MAX(id_rol) FROM rol));

-- 2. Insertar Usuario Administrador Principal
-- Nota: La contraseña está en formato BCrypt. El valor de abajo equivale a la contraseña 'admin123'
INSERT INTO usuario (
    id_usuario, 
    nombre_usuario, 
    correo_usuario, 
    tipopersona_usuario, 
    numero_documento_usuario, 
    telefono_usuario, 
    direccion_usuario, 
    password_usuario, 
    id_rol
) VALUES (
    1, 
    'Administrador Principal', 
    'admin@rokerperu.com', 
    'NATURAL', 
    '00000000000', 
    '999999999', 
    'Sede Central', 
    '$2a$10$ZytCKnIzFncajSLiyE.VQ.v8hvsLoJoKjqfiKe8nda.mJl4/bYdQK',
    1
) ON CONFLICT (id_usuario) DO NOTHING;

-- Sincronizar la secuencia de la tabla usuario
SELECT setval('usuario_id_usuario_seq', (SELECT MAX(id_usuario) FROM usuario));