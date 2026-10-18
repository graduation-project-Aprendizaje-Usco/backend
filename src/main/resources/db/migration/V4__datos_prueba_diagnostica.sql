-- =====================================================================
-- V202610180905 · Prueba Diagnóstica de Competencias Matemáticas, versión 2.0
-- Datos que necesitan TODOS los entornos: la prueba activa, sus 24 ítems
-- (8 por competencia) y las 4 opciones de cada uno, con su clave.
--
-- Fuente: "Instrumento de Diagnóstico: Competencias Matemáticas", v2.0,
-- incluida su "Tabla de Especificaciones" (claves). Tiempo: 1,5 min por ítem = 36 min.
-- Notación matemática entre $...$ (la muestra MathText con KaTeX en el frontend).
-- Las gráficas de los ítems 17 a 19 están en el frontend: public/img/diagnostico/.
--
-- NO editar una vez fusionado en develop: una corrección de un ítem va en otra
-- migración (UPDATE), y una versión nueva del instrumento es otra prueba con
-- activa = TRUE (desactivando la anterior).
-- =====================================================================

INSERT INTO prueba_diagnostica (nombre, version, tiempo_limite_min, activa)
VALUES ('Prueba Diagnóstica de Competencias Matemáticas', '2.0', 36, TRUE);


-- Ítem 1 · ALGEBRAICA · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 1, 'Si $3x + 5 = 20$, entonces $x$ es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$3$'), ('B', '$5$'), ('C', '$15$'), ('D', '$25$')) AS o (letra, texto)
WHERE i.numero = 1;

-- Ítem 2 · ALGEBRAICA · clave A
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 2, 'Simplifique: $2x + 3x - 4$', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'A'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$5x - 4$'), ('B', '$5x$'), ('C', '$2x - 4$'), ('D', '$3x - 4$')) AS o (letra, texto)
WHERE i.numero = 2;

-- Ítem 3 · ALGEBRAICA · clave A
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 3, 'El resultado de $(x + 2)(x + 3)$ es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'A'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$x^2 + 5x + 6$'), ('B', '$x^2 + 6$'), ('C', '$x^2 + 5x$'), ('D', '$2x^2 + 6$')) AS o (letra, texto)
WHERE i.numero = 3;

-- Ítem 4 · ALGEBRAICA · clave A
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 4, 'Si $f(x) = 2x - 1$, entonces $f(3)$ es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'A'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$5$'), ('B', '$6$'), ('C', '$7$'), ('D', '$4$')) AS o (letra, texto)
WHERE i.numero = 4;

-- Ítem 5 · ALGEBRAICA · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 5, 'Resolver: $x^2 = 16$', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$x = 4$'), ('B', '$x = -4$'), ('C', '$x = \pm 4$'), ('D', '$x = 8$')) AS o (letra, texto)
WHERE i.numero = 5;

-- Ítem 6 · ALGEBRAICA · clave A
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 6, 'Factorizar: $x^2 - 9$', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'A'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$(x - 3)(x + 3)$'), ('B', '$(x - 9)(x + 1)$'), ('C', '$(x - 3)^2$'), ('D', '$x(x - 9)$')) AS o (letra, texto)
WHERE i.numero = 6;

-- Ítem 7 · ALGEBRAICA · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 7, 'Si $2(x - 3) = 4$, entonces $x$ es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$2$'), ('B', '$5$'), ('C', '$1$'), ('D', '$3$')) AS o (letra, texto)
WHERE i.numero = 7;

-- Ítem 8 · ALGEBRAICA · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 8, 'El perímetro de un rectángulo es 24 cm. Si su largo es el doble de su ancho ($a$), ¿cuál ecuación representa esta situación?', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'ALGEBRAICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$2a + 2(2a) = 24$'), ('B', '$a + 2a = 24$'), ('C', '$2(a + 2a) = 24$'), ('D', '$a \cdot 2a = 24$')) AS o (letra, texto)
WHERE i.numero = 8;

-- Ítem 9 · LOGICA · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 9, 'Si todos los ingenieros estudian matemáticas y Juan estudia ingeniería, entonces:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'Juan no estudia matemáticas'), ('B', 'Juan estudia matemáticas'), ('C', 'No se puede determinar'), ('D', 'Juan estudia física')) AS o (letra, texto)
WHERE i.numero = 9;

-- Ítem 10 · LOGICA · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 10, '¿Cuál número continúa la secuencia $2, 4, 8, 16, \ldots$?', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$18$'), ('B', '$24$'), ('C', '$32$'), ('D', '$30$')) AS o (letra, texto)
WHERE i.numero = 10;

-- Ítem 11 · LOGICA · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 11, 'Si 3 máquinas producen 6 piezas en 1 hora, ¿cuántas piezas producen 6 máquinas en 1 hora?', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '6'), ('B', '9'), ('C', '12'), ('D', '18')) AS o (letra, texto)
WHERE i.numero = 11;

-- Ítem 12 · LOGICA · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 12, 'Si hoy es lunes, ¿qué día será en 10 días?', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'Miércoles'), ('B', 'Jueves'), ('C', 'Viernes'), ('D', 'Sábado')) AS o (letra, texto)
WHERE i.numero = 12;

-- Ítem 13 · LOGICA · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 13, 'Si $A > B$ y $B > C$, entonces:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$A < C$'), ('B', '$A = C$'), ('C', '$A > C$'), ('D', 'No se puede determinar')) AS o (letra, texto)
WHERE i.numero = 13;

-- Ítem 14 · LOGICA · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 14, 'En una clase hay 20 estudiantes y el 60 % son mujeres. ¿Cuántas mujeres hay?', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '10'), ('B', '12'), ('C', '14'), ('D', '8')) AS o (letra, texto)
WHERE i.numero = 14;

-- Ítem 15 · LOGICA · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 15, 'Un auto recorre 120 km en 2 horas. Su velocidad es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '50 km/h'), ('B', '60 km/h'), ('C', '70 km/h'), ('D', '80 km/h')) AS o (letra, texto)
WHERE i.numero = 15;

-- Ítem 16 · LOGICA · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 16, 'Si una proposición es verdadera y su negación es falsa, entonces:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'LOGICA';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'Es una contradicción'), ('B', 'Es una tautología'), ('C', 'Cumple el principio de no contradicción'), ('D', 'Es una falacia')) AS o (letra, texto)
WHERE i.numero = 16;

-- Ítem 17 · FUNCIONAL · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 17, 'Observa la gráfica de las funciones A y B. ¿Cuál de las siguientes afirmaciones es VERDADERA en el intervalo $x \in (-1, 0)$?', c.id, '/img/diagnostico/item-17.png', 'Gráfica de dos funciones. A es una recta creciente que corta el eje y en 1. B es una parábola que abre hacia abajo, con vértice en (0, 1), y corta el eje x en -1 y en 1.'
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'La función A y la función B son decrecientes'), ('B', 'La función B crece y la función A decrece'), ('C', 'La función A y la función B son crecientes'), ('D', 'La función A crece y la función B decrece')) AS o (letra, texto)
WHERE i.numero = 17;

-- Ítem 18 · FUNCIONAL · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 18, 'La gráfica muestra la relación entre la velocidad de un coche y el tiempo que tarda en hacer un viaje. La relación descrita por la función C es:', c.id, '/img/diagnostico/item-18.png', 'Gráfica de la curva C con la velocidad v en el eje vertical y el tiempo t en el eje horizontal: v disminuye a medida que t aumenta; pasa por los puntos (1, 1) y (2, 0.5).'
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'Directamente proporcional'), ('B', 'Inversamente proporcional'), ('C', 'Lineal creciente'), ('D', 'Constante')) AS o (letra, texto)
WHERE i.numero = 18;

-- Ítem 19 · FUNCIONAL · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 19, 'Para la función representada en la gráfica, ¿cuál es su dominio (valores de $x$ que puede tomar)?', c.id, '/img/diagnostico/item-19.png', 'Gráfica de la función constante f(x) = 2: una recta horizontal a la altura 2, con círculos vacíos (puntos excluidos) en x = -2 y en x = 2.'
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'Todos los números reales'), ('B', '$x \in (-\infty, -2) \cup (-2, 2) \cup (2, +\infty)$'), ('C', 'Solo los números $-2$ y $2$'), ('D', 'Solo $x > -2$ y $x > 2$')) AS o (letra, texto)
WHERE i.numero = 19;

-- Ítem 20 · FUNCIONAL · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 20, 'Si $f(x) = 3x$ y $x$ aumenta, entonces:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$f(x)$ disminuye'), ('B', '$f(x)$ aumenta'), ('C', '$f(x)$ es constante'), ('D', '$f(x)$ cambia aleatoriamente')) AS o (letra, texto)
WHERE i.numero = 20;

-- Ítem 21 · FUNCIONAL · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 21, 'Si el precio de un producto sube y la demanda baja, la relación es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'Directa'), ('B', 'Inversa'), ('C', 'Constante'), ('D', 'Nula')) AS o (letra, texto)
WHERE i.numero = 21;

-- Ítem 22 · FUNCIONAL · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 22, 'La pendiente positiva de una recta indica que:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'La función decrece'), ('B', 'La función aumenta'), ('C', 'Es constante'), ('D', 'Es vertical')) AS o (letra, texto)
WHERE i.numero = 22;

-- Ítem 23 · FUNCIONAL · clave B
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 23, 'Si una gráfica cruza el eje $Y$ en 5, significa que:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'B'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', 'La pendiente es 5'), ('B', 'El intercepto es 5'), ('C', 'El dominio es 5'), ('D', 'El rango es 5')) AS o (letra, texto)
WHERE i.numero = 23;

-- Ítem 24 · FUNCIONAL · clave C
INSERT INTO item (prueba_id, numero, enunciado, competencia_id, imagen, imagen_alt)
SELECT p.id, 24, 'Si el área de un cuadrado depende del lado $L$, la expresión correcta es:', c.id, NULL, NULL
FROM prueba_diagnostica p, competencia c
WHERE p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas' AND p.version = '2.0' AND c.codigo = 'FUNCIONAL';
INSERT INTO opcion (item_id, letra, texto, es_correcta)
SELECT i.id, o.letra, o.texto, o.letra = 'C'
FROM item i
JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.version = '2.0' AND p.nombre = 'Prueba Diagnóstica de Competencias Matemáticas'
CROSS JOIN (VALUES ('A', '$L + L$'), ('B', '$2L$'), ('C', '$L^2$'), ('D', '$L^3$')) AS o (letra, texto)
WHERE i.numero = 24;
