-- =====================================================================
-- Practica adaptativa: contenidos, ejercicios, intentos y niveles.
-- Convencion: snake_case singular; pk_, fk_, uq_, ck_ e indices ix_.
-- No editar despues de fusionar: los cambios van en una migracion nueva.
-- =====================================================================

-- ------------------------------------------------------------ contenido
CREATE TABLE contenido (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    competencia_id BIGINT       NOT NULL,
    asignatura     VARCHAR(80)  NOT NULL,
    tema           VARCHAR(120) NOT NULL,
    titulo         VARCHAR(150) NOT NULL,
    descripcion    VARCHAR(500),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por     BIGINT,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_contenido                    PRIMARY KEY (id),
    CONSTRAINT fk_contenido_competencia        FOREIGN KEY (competencia_id) REFERENCES competencia (id),
    CONSTRAINT fk_contenido_creado_por         FOREIGN KEY (creado_por) REFERENCES usuario (id),
    CONSTRAINT uq_contenido_titulo             UNIQUE (asignatura, tema, titulo),
    CONSTRAINT uq_contenido_id_competencia     UNIQUE (id, competencia_id),
    CONSTRAINT ck_contenido_asignatura         CHECK (asignatura IN ('CALCULO_DIFERENCIAL', 'CALCULO_INTEGRAL')),
    CONSTRAINT ck_contenido_textos             CHECK (btrim(asignatura) <> '' AND btrim(tema) <> '' AND btrim(titulo) <> '')
);

CREATE INDEX ix_contenido_competencia_activo ON contenido (competencia_id, activo);

COMMENT ON TABLE contenido IS 'Agrupacion tematica de ejercicios por asignatura y competencia.';
COMMENT ON COLUMN contenido.creado_por IS 'NULL para los datos semilla; el docente que lo crea en la aplicacion en otro caso.';

-- ------------------------------------------------------------ ejercicio
CREATE TABLE ejercicio (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    contenido_id        BIGINT        NOT NULL,
    competencia_id      BIGINT        NOT NULL,
    codigo              VARCHAR(40)   NOT NULL,
    nivel               SMALLINT      NOT NULL,
    tipo                VARCHAR(20)   NOT NULL,
    enunciado           VARCHAR(2000) NOT NULL,
    respuesta_numerica  NUMERIC(20, 6),
    tolerancia          NUMERIC(20, 6),
    pista               VARCHAR(1000) NOT NULL,
    explicacion         VARCHAR(2000) NOT NULL,
    error_comun         VARCHAR(1000),
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_por          BIGINT,
    actualizado_por     BIGINT,
    creado_en           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_ejercicio                         PRIMARY KEY (id),
    CONSTRAINT fk_ejercicio_contenido_competencia   FOREIGN KEY (contenido_id, competencia_id)
        REFERENCES contenido (id, competencia_id),
    CONSTRAINT fk_ejercicio_creado_por              FOREIGN KEY (creado_por) REFERENCES usuario (id),
    CONSTRAINT fk_ejercicio_actualizado_por         FOREIGN KEY (actualizado_por) REFERENCES usuario (id),
    CONSTRAINT uq_ejercicio_codigo                  UNIQUE (codigo),
    CONSTRAINT ck_ejercicio_nivel                   CHECK (nivel BETWEEN 1 AND 3),
    CONSTRAINT ck_ejercicio_tipo                    CHECK (tipo IN ('OPCION_MULTIPLE', 'NUMERICA')),
    CONSTRAINT ck_ejercicio_respuesta               CHECK (
        (tipo = 'NUMERICA'
            AND respuesta_numerica IS NOT NULL
            AND tolerancia IS NOT NULL
            AND tolerancia > 0)
        OR
        (tipo = 'OPCION_MULTIPLE'
            AND respuesta_numerica IS NULL
            AND tolerancia IS NULL)
    ),
    CONSTRAINT ck_ejercicio_textos                  CHECK (
        btrim(enunciado) <> '' AND btrim(pista) <> '' AND btrim(explicacion) <> ''
    )
);

CREATE INDEX ix_ejercicio_competencia_nivel_activo ON ejercicio (competencia_id, nivel, activo);
CREATE INDEX ix_ejercicio_contenido_id ON ejercicio (contenido_id);

COMMENT ON TABLE ejercicio IS 'Banco de ejercicios de practica; la clave de opcion multiple vive en opcion_ejercicio.';
COMMENT ON COLUMN ejercicio.nivel IS 'Dificultad del ejercicio: 1 basico, 2 intermedio, 3 avanzado.';
COMMENT ON COLUMN ejercicio.codigo IS 'Identificador estable del ejercicio; unico en todo el banco.';

-- ------------------------------------------------------------ opcion_ejercicio
CREATE TABLE opcion_ejercicio (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    ejercicio_id BIGINT       NOT NULL,
    letra        VARCHAR(1)   NOT NULL,
    texto        VARCHAR(500) NOT NULL,
    es_correcta  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_opcion_ejercicio            PRIMARY KEY (id),
    CONSTRAINT fk_opcion_ejercicio            FOREIGN KEY (ejercicio_id) REFERENCES ejercicio (id) ON DELETE CASCADE,
    CONSTRAINT uq_opcion_ejercicio_letra      UNIQUE (ejercicio_id, letra),
    CONSTRAINT uq_opcion_ejercicio_id_ejercicio UNIQUE (id, ejercicio_id),
    CONSTRAINT ck_opcion_ejercicio_letra      CHECK (letra IN ('A', 'B', 'C', 'D')),
    CONSTRAINT ck_opcion_ejercicio_texto      CHECK (btrim(texto) <> '')
);

CREATE UNIQUE INDEX uq_opcion_ejercicio_una_correcta
    ON opcion_ejercicio (ejercicio_id) WHERE es_correcta;

COMMENT ON COLUMN opcion_ejercicio.es_correcta IS 'Clave del ejercicio. NUNCA se envia al estudiante.';

-- ------------------------------------------------------------ intento_practica
CREATE TABLE intento_practica (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id            BIGINT       NOT NULL,
    ejercicio_id          BIGINT       NOT NULL,
    competencia_id        BIGINT       NOT NULL,
    nivel_ejercicio       SMALLINT     NOT NULL,
    numero_intento        SMALLINT     NOT NULL,
    opcion_ejercicio_id   BIGINT,
    respuesta_numerica    NUMERIC(20, 6),
    es_correcta           BOOLEAN      NOT NULL,
    tiempo_seg            INTEGER      NOT NULL,
    pista_usada           BOOLEAN      NOT NULL DEFAULT FALSE,
    clave_idempotencia    UUID         NOT NULL,
    creado_en             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_intento_practica                 PRIMARY KEY (id),
    CONSTRAINT fk_intento_practica_usuario         FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_intento_practica_ejercicio       FOREIGN KEY (ejercicio_id) REFERENCES ejercicio (id),
    CONSTRAINT fk_intento_practica_competencia     FOREIGN KEY (competencia_id) REFERENCES competencia (id),
    CONSTRAINT fk_intento_practica_opcion          FOREIGN KEY (opcion_ejercicio_id, ejercicio_id)
        REFERENCES opcion_ejercicio (id, ejercicio_id),
    CONSTRAINT uq_intento_practica_idempotencia    UNIQUE (usuario_id, clave_idempotencia),
    CONSTRAINT uq_intento_practica_numero          UNIQUE (usuario_id, ejercicio_id, numero_intento),
    CONSTRAINT ck_intento_practica_numero          CHECK (numero_intento BETWEEN 1 AND 2),
    CONSTRAINT ck_intento_practica_tiempo          CHECK (tiempo_seg BETWEEN 0 AND 86400),
    CONSTRAINT ck_intento_practica_respuesta       CHECK (
        (opcion_ejercicio_id IS NOT NULL AND respuesta_numerica IS NULL)
        OR
        (opcion_ejercicio_id IS NULL AND respuesta_numerica IS NOT NULL)
    )
);

CREATE INDEX ix_intento_practica_usuario_fecha ON intento_practica (usuario_id, creado_en DESC);
CREATE INDEX ix_intento_practica_ejercicio_id ON intento_practica (ejercicio_id);
CREATE INDEX ix_intento_practica_usuario_ejercicio_correcta
    ON intento_practica (usuario_id, ejercicio_id, es_correcta);

COMMENT ON TABLE intento_practica IS 'Cada respuesta enviada por un estudiante. El primer intento alimenta el nivel.';
COMMENT ON COLUMN intento_practica.numero_intento IS '1 = intento que cuenta para el nivel; 2 = reintento pedagogico.';
COMMENT ON COLUMN intento_practica.nivel_ejercicio IS 'Snapshot del nivel del ejercicio al momento del intento.';
COMMENT ON COLUMN intento_practica.competencia_id IS 'Snapshot de la competencia para conservar el historial.';

-- ------------------------------------------------------------ nivel_estudiante
CREATE TABLE nivel_estudiante (
    usuario_id     BIGINT      NOT NULL,
    competencia_id BIGINT      NOT NULL,
    nivel          SMALLINT    NOT NULL,
    racha_aciertos SMALLINT    NOT NULL DEFAULT 0,
    racha_errores  SMALLINT    NOT NULL DEFAULT 0,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_nivel_estudiante             PRIMARY KEY (usuario_id, competencia_id),
    CONSTRAINT fk_nivel_estudiante_usuario     FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_nivel_estudiante_competencia FOREIGN KEY (competencia_id) REFERENCES competencia (id),
    CONSTRAINT ck_nivel_estudiante_nivel       CHECK (nivel BETWEEN 1 AND 3),
    CONSTRAINT ck_nivel_estudiante_rachas      CHECK (racha_aciertos >= 0 AND racha_errores >= 0)
);

CREATE INDEX ix_nivel_estudiante_competencia ON nivel_estudiante (competencia_id);

COMMENT ON TABLE nivel_estudiante IS 'Nivel actual de dificultad de practica por estudiante y competencia.';
COMMENT ON COLUMN nivel_estudiante.nivel IS 'No es el nivel de dominio del diagnostico: es la dificultad que el motor ofrece.';

-- ------------------------------------------------------------ historial_nivel
CREATE TABLE historial_nivel (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id             BIGINT      NOT NULL,
    competencia_id         BIGINT      NOT NULL,
    nivel_anterior         SMALLINT,
    nivel_nuevo            SMALLINT    NOT NULL,
    motivo                 VARCHAR(30) NOT NULL,
    intento_practica_id    BIGINT,
    intento_diagnostico_id BIGINT,
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_historial_nivel                  PRIMARY KEY (id),
    CONSTRAINT fk_historial_nivel_usuario          FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_nivel_competencia      FOREIGN KEY (competencia_id) REFERENCES competencia (id),
    CONSTRAINT fk_historial_nivel_intento_practica FOREIGN KEY (intento_practica_id)
        REFERENCES intento_practica (id) ON DELETE SET NULL,
    CONSTRAINT fk_historial_nivel_intento_diagnostico FOREIGN KEY (intento_diagnostico_id)
        REFERENCES intento_diagnostico (id) ON DELETE SET NULL,
    CONSTRAINT ck_historial_nivel_nuevo            CHECK (nivel_nuevo BETWEEN 1 AND 3),
    CONSTRAINT ck_historial_nivel_anterior         CHECK (nivel_anterior IS NULL OR nivel_anterior BETWEEN 1 AND 3),
    CONSTRAINT ck_historial_nivel_motivo           CHECK (motivo IN ('INICIALIZACION', 'DIAGNOSTICO', 'RACHA_ACIERTOS', 'RACHA_ERRORES', 'AJUSTE_MANUAL'))
);

CREATE INDEX ix_historial_nivel_usuario_competencia_fecha
    ON historial_nivel (usuario_id, competencia_id, creado_en);
