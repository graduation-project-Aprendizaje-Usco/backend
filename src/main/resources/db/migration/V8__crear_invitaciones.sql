-- Invitaciones de estudiantes por grupo (RF-01, RF-13, #150).
-- La invitación es la fuente de verdad antes de que exista la cuenta: guarda el
-- correo y los nombres del listado del docente. Cuando el estudiante se registra,
-- se crea el usuario y la fila de grupo_estudiante, y la invitación se marca usada.

CREATE TABLE invitacion (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    grupo_id       BIGINT       NOT NULL,
    correo         VARCHAR(150) NOT NULL,
    nombres        VARCHAR(100) NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    token_hash     VARCHAR(64)  NOT NULL,
    vence_en       TIMESTAMPTZ  NOT NULL,
    usada_en       TIMESTAMPTZ,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_invitacion PRIMARY KEY (id),
    CONSTRAINT uq_invitacion_token_hash UNIQUE (token_hash),
    CONSTRAINT uq_invitacion_grupo_correo UNIQUE (grupo_id, correo),
    CONSTRAINT fk_invitacion_grupo FOREIGN KEY (grupo_id)
        REFERENCES grupo (id) ON DELETE CASCADE,
    CONSTRAINT ck_invitacion_correo_minuscula CHECK (correo = lower(correo))
);

CREATE INDEX ix_invitacion_correo ON invitacion (correo);
CREATE INDEX ix_invitacion_grupo_id ON invitacion (grupo_id);

COMMENT ON TABLE invitacion IS 'Invitaciones de estudiantes a un grupo; el token se guarda hasheado.';
COMMENT ON COLUMN invitacion.token_hash IS 'SHA-256 del token que viaja en el enlace; nunca se guarda en claro.';
