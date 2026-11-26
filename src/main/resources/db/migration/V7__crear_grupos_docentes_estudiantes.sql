-- Grupos académicos y asociación de estudiantes con su docente responsable.
-- No se almacenan datos de usuario duplicados: ambas relaciones apuntan a usuario.id.

CREATE TABLE grupo (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo      VARCHAR(30)  NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    docente_id  BIGINT       NOT NULL,
    creado_en   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_grupo          PRIMARY KEY (id),
    CONSTRAINT uq_grupo_codigo   UNIQUE (codigo),
    CONSTRAINT fk_grupo_docente  FOREIGN KEY (docente_id) REFERENCES usuario (id),
    CONSTRAINT ck_grupo_codigo   CHECK (codigo = upper(codigo))
);

CREATE INDEX ix_grupo_docente_id ON grupo (docente_id);

COMMENT ON TABLE grupo IS 'Grupos académicos y su docente responsable (SCRUM-159).';
COMMENT ON COLUMN grupo.docente_id IS 'Usuario responsable; la aplicación valida que tenga rol DOCENTE.';

CREATE TABLE grupo_estudiante (
    grupo_id       BIGINT      NOT NULL,
    estudiante_id  BIGINT      NOT NULL,
    creado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_grupo_estudiante PRIMARY KEY (grupo_id, estudiante_id),
    CONSTRAINT fk_grupo_estudiante_grupo FOREIGN KEY (grupo_id)
        REFERENCES grupo (id) ON DELETE CASCADE,
    CONSTRAINT fk_grupo_estudiante_usuario FOREIGN KEY (estudiante_id)
        REFERENCES usuario (id)
);

CREATE INDEX ix_grupo_estudiante_estudiante_id ON grupo_estudiante (estudiante_id);

COMMENT ON TABLE grupo_estudiante IS 'Estudiantes inscritos en cada grupo; la PK impide asociaciones duplicadas.';
