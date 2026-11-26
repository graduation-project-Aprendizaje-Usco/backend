CREATE TABLE activacion_cuenta (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id     BIGINT       NOT NULL,
    token_hash     VARCHAR(64)  NOT NULL,
    vence_en       TIMESTAMPTZ  NOT NULL,
    usada_en       TIMESTAMPTZ,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_activacion_cuenta PRIMARY KEY (id),
    CONSTRAINT uq_activacion_cuenta_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_activacion_cuenta_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_activacion_cuenta_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

COMMENT ON TABLE activacion_cuenta IS 'Enlace de un solo uso para definir la contraseña de cuentas creadas por administración.';
COMMENT ON COLUMN activacion_cuenta.token_hash IS 'SHA-256 del token enviado por correo; nunca se guarda en claro.';
