-- =====================================================================
-- V1 · Esquema inicial: rol, usuario, competencia
-- Modelo físico en docs/base-de-datos/modelo-fisico.md
-- Convención: tablas y columnas en snake_case singular;
-- restricciones con prefijo pk_, fk_, uq_, ck_ e índices con ix_.
-- NO editar este archivo una vez fusionado en develop: crear otra migración.
-- =====================================================================

CREATE TABLE rol (
    id      BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre  VARCHAR(20) NOT NULL,
    CONSTRAINT pk_rol        PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre),
    CONSTRAINT ck_rol_nombre CHECK (nombre IN ('ESTUDIANTE', 'DOCENTE', 'ADMIN'))
);

COMMENT ON TABLE  rol        IS 'Roles de la plataforma (RF-01). Debe coincidir con el enum NombreRol y con el claim "rol" del JWT.';
COMMENT ON COLUMN rol.nombre IS 'ESTUDIANTE, DOCENTE o ADMIN';


CREATE TABLE usuario (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    correo              VARCHAR(150) NOT NULL,
    password_hash       VARCHAR(100) NOT NULL,
    rol_id              BIGINT       NOT NULL,
    programa_academico  VARCHAR(120),
    estado              VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    creado_en           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_usuario                 PRIMARY KEY (id),
    -- La restricción UNIQUE crea automáticamente el índice uq_usuario_correo,
    -- que es el que usa el login (búsqueda por correo).
    CONSTRAINT uq_usuario_correo          UNIQUE (correo),
    CONSTRAINT fk_usuario_rol             FOREIGN KEY (rol_id) REFERENCES rol (id),
    CONSTRAINT ck_usuario_estado          CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    -- El correo se guarda siempre en minúsculas: "Ana@USCO.edu.co" y "ana@usco.edu.co" son el mismo usuario.
    CONSTRAINT ck_usuario_correo_minuscula CHECK (correo = lower(correo))
);

-- Índice de la llave foránea (PostgreSQL no lo crea solo): filtros y conteos por rol.
CREATE INDEX ix_usuario_rol_id ON usuario (rol_id);

COMMENT ON TABLE  usuario                    IS 'Personas que usan la plataforma (RF-01, RF-13).';
COMMENT ON COLUMN usuario.password_hash      IS 'Hash BCrypt de la contraseña; nunca la contraseña en texto plano.';
COMMENT ON COLUMN usuario.programa_academico IS 'Programa del estudiante; NULL para docentes y administradores.';
COMMENT ON COLUMN usuario.estado             IS 'ACTIVO o INACTIVO. Un usuario INACTIVO no puede iniciar sesión.';


CREATE TABLE competencia (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo       VARCHAR(30)  NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(500),
    CONSTRAINT pk_competencia         PRIMARY KEY (id),
    CONSTRAINT uq_competencia_codigo  UNIQUE (codigo),
    CONSTRAINT ck_competencia_codigo  CHECK (codigo = upper(codigo))
);

COMMENT ON TABLE  competencia        IS 'Competencias matemáticas diagnosticadas y reforzadas (RF-14).';
COMMENT ON COLUMN competencia.codigo IS 'Código estable en mayúsculas (ALGEBRAICA, LOGICA, FUNCIONAL). Nuevas competencias se agregan con una migración de datos, sin cambiar código (RNF-09).';
