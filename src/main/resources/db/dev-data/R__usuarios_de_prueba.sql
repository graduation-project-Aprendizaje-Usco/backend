-- =====================================================================
-- Usuarios de prueba: SOLO desarrollo y staging.
-- Se carga porque application-dev.properties agrega classpath:db/dev-data
-- a spring.flyway.locations. Con el perfil por defecto o "test" no existe.
--
-- Migración repetible (R__): Flyway la vuelve a ejecutar cada vez que este
-- archivo cambia, después de las migraciones versionadas. Por eso es
-- idempotente (ON CONFLICT): restablece contraseña, rol y estado.
--
-- Contraseñas (BCrypt, costo 10, compatible con BCryptPasswordEncoder):
--   estudiante@aprendizaje.test  ->  Estudiante2026*
--   docente@aprendizaje.test     ->  Docente2026*
--   admin@aprendizaje.test       ->  Admin2026*
-- Dominio .test: reservado para pruebas, nunca será un correo real.
-- =====================================================================

INSERT INTO usuario (nombres, apellidos, correo, password_hash, rol_id, programa_academico)
SELECT 'Estudiante', 'De Prueba', 'estudiante@aprendizaje.test',
       '$2a$10$BUcgQkbwXcUyqpCH.cxy4.r0pxctyDw/M62NvTcwjkRjDGnGpcdHq',
       r.id, 'Ingeniería de Software'
FROM rol r WHERE r.nombre = 'ESTUDIANTE'
ON CONFLICT (correo) DO UPDATE
   SET password_hash = EXCLUDED.password_hash, rol_id = EXCLUDED.rol_id,
       estado = 'ACTIVO', actualizado_en = now();

INSERT INTO usuario (nombres, apellidos, correo, password_hash, rol_id, programa_academico)
SELECT 'Docente', 'De Prueba', 'docente@aprendizaje.test',
       '$2a$10$ivzhCmKlO..IzZywIkOKk.3jkn66RZ.Zgb5RAHLqlccuz1jNGHEQq',
       r.id, NULL
FROM rol r WHERE r.nombre = 'DOCENTE'
ON CONFLICT (correo) DO UPDATE
   SET password_hash = EXCLUDED.password_hash, rol_id = EXCLUDED.rol_id,
       estado = 'ACTIVO', actualizado_en = now();

INSERT INTO usuario (nombres, apellidos, correo, password_hash, rol_id, programa_academico)
SELECT 'Administrador', 'De Prueba', 'admin@aprendizaje.test',
       '$2a$10$fevUvdbeE1FCAXZBFSvwXO91i7L0ZK0OPy5OeN.92UtMg2Tf77DpG',
       r.id, NULL
FROM rol r WHERE r.nombre = 'ADMIN'
ON CONFLICT (correo) DO UPDATE
   SET password_hash = EXCLUDED.password_hash, rol_id = EXCLUDED.rol_id,
       estado = 'ACTIVO', actualizado_en = now();
