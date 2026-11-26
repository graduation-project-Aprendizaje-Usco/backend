-- =====================================================================
-- V202610180900 · Esquema de la prueba diagnóstica (RF-02, RF-03, RF-04)
-- Tablas: prueba_diagnostica, item, opcion, intento_diagnostico,
--         respuesta_diagnostico, resultado_dimension.
-- Modelo físico en docs/base-de-datos/modelo-fisico.md
-- Convención: snake_case singular; pk_, fk_, uq_, ck_ e índices ix_.
-- NO editar este archivo una vez fusionado en develop: crear otra migración.
-- =====================================================================

-- ------------------------------------------------------------ prueba_diagnostica
CREATE TABLE prueba_diagnostica (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre             VARCHAR(150) NOT NULL,
    version            VARCHAR(20)  NOT NULL,
    tiempo_limite_min  INTEGER      NOT NULL,
    activa             BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_prueba_diagnostica         PRIMARY KEY (id),
    CONSTRAINT uq_prueba_nombre_version      UNIQUE (nombre, version),
    CONSTRAINT ck_prueba_tiempo_limite       CHECK (tiempo_limite_min > 0)
);

-- Solo una prueba puede estar activa a la vez (es la que presentan los estudiantes).
CREATE UNIQUE INDEX uq_prueba_una_activa ON prueba_diagnostica (activa) WHERE activa;

COMMENT ON TABLE  prueba_diagnostica                   IS 'Versiones del instrumento de diagnóstico (cap. 3 del informe).';
COMMENT ON COLUMN prueba_diagnostica.tiempo_limite_min IS 'Minutos disponibles desde que el estudiante inicia el intento.';
COMMENT ON COLUMN prueba_diagnostica.activa            IS 'La prueba que se aplica hoy. Como máximo una activa (uq_prueba_una_activa).';

-- ------------------------------------------------------------ item
CREATE TABLE item (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    prueba_id       BIGINT        NOT NULL,
    numero          INTEGER       NOT NULL,
    enunciado       VARCHAR(1000) NOT NULL,
    competencia_id  BIGINT        NOT NULL,
    imagen          VARCHAR(200),
    imagen_alt      VARCHAR(500),
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_item                PRIMARY KEY (id),
    CONSTRAINT fk_item_prueba         FOREIGN KEY (prueba_id)      REFERENCES prueba_diagnostica (id),
    CONSTRAINT fk_item_competencia    FOREIGN KEY (competencia_id) REFERENCES competencia (id),
    CONSTRAINT uq_item_prueba_numero  UNIQUE (prueba_id, numero),
    CONSTRAINT ck_item_numero         CHECK (numero > 0),
    -- Una imagen siempre lleva su texto alternativo (accesibilidad, RNF-05).
    CONSTRAINT ck_item_imagen_alt     CHECK (imagen IS NULL OR imagen_alt IS NOT NULL)
);

CREATE INDEX ix_item_competencia_id ON item (competencia_id);

COMMENT ON TABLE  item           IS 'Preguntas de una prueba diagnóstica, en el orden de "numero".';
COMMENT ON COLUMN item.enunciado IS 'Texto con notación matemática entre $...$ (lo muestra MathText con KaTeX).';
COMMENT ON COLUMN item.imagen    IS 'Ruta de la gráfica en el frontend (public/), p. ej. /img/diagnostico/item-17.png. NULL si no tiene.';

-- ------------------------------------------------------------ opcion
CREATE TABLE opcion (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    item_id      BIGINT       NOT NULL,
    letra        VARCHAR(1)   NOT NULL,
    texto        VARCHAR(500) NOT NULL,
    es_correcta  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_opcion             PRIMARY KEY (id),
    CONSTRAINT fk_opcion_item        FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE,
    CONSTRAINT uq_opcion_item_letra  UNIQUE (item_id, letra),
    -- Permite que respuesta_diagnostico verifique que la opción es de esa pregunta.
    CONSTRAINT uq_opcion_id_item     UNIQUE (id, item_id),
    CONSTRAINT ck_opcion_letra       CHECK (letra IN ('A', 'B', 'C', 'D'))
);

-- Exactamente una clave por pregunta: nunca dos opciones correctas.
CREATE UNIQUE INDEX uq_opcion_una_correcta ON opcion (item_id) WHERE es_correcta;

COMMENT ON COLUMN opcion.es_correcta IS 'Clave de la pregunta. NUNCA se envía al estudiante.';

-- ------------------------------------------------------------ intento_diagnostico
CREATE TABLE intento_diagnostico (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id             BIGINT       NOT NULL,
    prueba_id              BIGINT       NOT NULL,
    estado                 VARCHAR(12)  NOT NULL DEFAULT 'EN_CURSO',
    iniciado_en            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finalizado_en          TIMESTAMPTZ,
    finalizado_por_tiempo  BOOLEAN      NOT NULL DEFAULT FALSE,
    puntaje_total          INTEGER,
    categoria              VARCHAR(20),
    CONSTRAINT pk_intento_diagnostico  PRIMARY KEY (id),
    CONSTRAINT fk_intento_usuario      FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_intento_prueba       FOREIGN KEY (prueba_id)  REFERENCES prueba_diagnostica (id),
    CONSTRAINT ck_intento_estado       CHECK (estado IN ('EN_CURSO', 'FINALIZADO')),
    CONSTRAINT ck_intento_categoria    CHECK (categoria IN ('ALTO_RIESGO', 'RIESGO_MODERADO', 'NIVEL_ADECUADO')),
    CONSTRAINT ck_intento_puntaje      CHECK (puntaje_total >= 0),
    -- En curso: sin fecha de fin ni resultado. Finalizado: con fecha, puntaje y categoría.
    CONSTRAINT ck_intento_coherente    CHECK (
        (estado = 'EN_CURSO'   AND finalizado_en IS NULL     AND puntaje_total IS NULL     AND categoria IS NULL)
     OR (estado = 'FINALIZADO' AND finalizado_en IS NOT NULL AND puntaje_total IS NOT NULL AND categoria IS NOT NULL)),
    CONSTRAINT ck_intento_fechas       CHECK (finalizado_en IS NULL OR finalizado_en >= iniciado_en)
);

-- Criterio de aceptación: un estudiante no puede tener dos intentos EN_CURSO de la misma prueba.
CREATE UNIQUE INDEX uq_intento_en_curso ON intento_diagnostico (usuario_id, prueba_id) WHERE estado = 'EN_CURSO';
CREATE INDEX ix_intento_prueba_id ON intento_diagnostico (prueba_id);

COMMENT ON TABLE  intento_diagnostico                       IS 'Cada presentación de la prueba por un estudiante.';
COMMENT ON COLUMN intento_diagnostico.finalizado_por_tiempo IS 'TRUE si se cerró al vencer tiempo_limite_min (no lo finalizó el estudiante).';
COMMENT ON COLUMN intento_diagnostico.puntaje_total         IS 'Respuestas correctas (0 a 24 en la versión 2.0).';

-- ------------------------------------------------------------ respuesta_diagnostico
CREATE TABLE respuesta_diagnostico (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    intento_id     BIGINT       NOT NULL,
    item_id        BIGINT       NOT NULL,
    opcion_id      BIGINT       NOT NULL,
    respondida_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_respuesta_diagnostico   PRIMARY KEY (id),
    CONSTRAINT fk_respuesta_intento       FOREIGN KEY (intento_id) REFERENCES intento_diagnostico (id) ON DELETE CASCADE,
    CONSTRAINT fk_respuesta_item          FOREIGN KEY (item_id)    REFERENCES item (id),
    -- La opción elegida debe pertenecer a la misma pregunta.
    CONSTRAINT fk_respuesta_opcion_item   FOREIGN KEY (opcion_id, item_id) REFERENCES opcion (id, item_id),
    -- Criterio de aceptación: una sola respuesta por pregunta en cada intento (cambiarla = actualizarla).
    CONSTRAINT uq_respuesta_intento_item  UNIQUE (intento_id, item_id)
);

CREATE INDEX ix_respuesta_item_id ON respuesta_diagnostico (item_id);

COMMENT ON COLUMN respuesta_diagnostico.respondida_en IS 'Última vez que el estudiante eligió o cambió la opción.';

-- ------------------------------------------------------------ resultado_dimension
CREATE TABLE resultado_dimension (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    intento_id      BIGINT        NOT NULL,
    competencia_id  BIGINT        NOT NULL,
    aciertos        INTEGER       NOT NULL,
    total           INTEGER       NOT NULL,
    porcentaje      NUMERIC(5, 2) NOT NULL,
    nivel           VARCHAR(10)   NOT NULL,
    CONSTRAINT pk_resultado_dimension          PRIMARY KEY (id),
    CONSTRAINT fk_resultado_intento            FOREIGN KEY (intento_id)     REFERENCES intento_diagnostico (id) ON DELETE CASCADE,
    CONSTRAINT fk_resultado_competencia        FOREIGN KEY (competencia_id) REFERENCES competencia (id),
    CONSTRAINT uq_resultado_intento_competencia UNIQUE (intento_id, competencia_id),
    CONSTRAINT ck_resultado_aciertos           CHECK (total > 0 AND aciertos BETWEEN 0 AND total),
    CONSTRAINT ck_resultado_porcentaje         CHECK (porcentaje BETWEEN 0 AND 100),
    CONSTRAINT ck_resultado_nivel              CHECK (nivel IN ('BAJO', 'MEDIO', 'ALTO'))
);

CREATE INDEX ix_resultado_competencia_id ON resultado_dimension (competencia_id);

COMMENT ON TABLE resultado_dimension IS 'Puntaje de un intento por competencia (algebraica, lógica, funcional).';
