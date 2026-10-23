-- =====================================================================
-- Datos semilla del banco de practica (90 ejercicios).
-- Fuente revisable: docs/datos/ejercicios_practica.csv
-- 45 ejercicios de Calculo Diferencial y 45 de Calculo Integral.
-- NO editar una vez fusionado: los cambios van en otra migracion.
-- =====================================================================

INSERT INTO contenido (competencia_id, asignatura, tema, titulo, descripcion)
SELECT c.id, 'CALCULO_DIFERENCIAL', 'Algebra y funciones', 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA', 'Contenido semilla de ALGEBRAICA para CALCULO_DIFERENCIAL.'
FROM competencia c WHERE c.codigo = 'ALGEBRAICA';

INSERT INTO contenido (competencia_id, asignatura, tema, titulo, descripcion)
SELECT c.id, 'CALCULO_DIFERENCIAL', 'Funciones limites y derivadas', 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL', 'Contenido semilla de FUNCIONAL para CALCULO_DIFERENCIAL.'
FROM competencia c WHERE c.codigo = 'FUNCIONAL';

INSERT INTO contenido (competencia_id, asignatura, tema, titulo, descripcion)
SELECT c.id, 'CALCULO_DIFERENCIAL', 'Razonamiento logico', 'Semilla - CALCULO_DIFERENCIAL - LOGICA', 'Contenido semilla de LOGICA para CALCULO_DIFERENCIAL.'
FROM competencia c WHERE c.codigo = 'LOGICA';

INSERT INTO contenido (competencia_id, asignatura, tema, titulo, descripcion)
SELECT c.id, 'CALCULO_INTEGRAL', 'Algebra para integracion', 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA', 'Contenido semilla de ALGEBRAICA para CALCULO_INTEGRAL.'
FROM competencia c WHERE c.codigo = 'ALGEBRAICA';

INSERT INTO contenido (competencia_id, asignatura, tema, titulo, descripcion)
SELECT c.id, 'CALCULO_INTEGRAL', 'Integrales y aplicaciones', 'Semilla - CALCULO_INTEGRAL - FUNCIONAL', 'Contenido semilla de FUNCIONAL para CALCULO_INTEGRAL.'
FROM competencia c WHERE c.codigo = 'FUNCIONAL';

INSERT INTO contenido (competencia_id, asignatura, tema, titulo, descripcion)
SELECT c.id, 'CALCULO_INTEGRAL', 'Razonamiento logico', 'Semilla - CALCULO_INTEGRAL - LOGICA', 'Contenido semilla de LOGICA para CALCULO_INTEGRAL.'
FROM competencia c WHERE c.codigo = 'LOGICA';

-- DIF-ALG-1-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-1-01', 1, 'OPCION_MULTIPLE', 'Simplifica $2x+3x-4$.', NULL, NULL, 'Suma primero los terminos semejantes.', '$2x+3x=5x$, entonces la expresion queda $5x-4$.', 'Sumar coeficientes y conservar mal el termino constante.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$5x-4$'), ('B', '$5x$'), ('C', '$6x-4$'), ('D', '$x-4$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-1-01';

-- DIF-ALG-1-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-1-02', 1, 'OPCION_MULTIPLE', 'Si $3x+5=20$, entonces $x$ es:', NULL, NULL, 'Resta 5 en ambos lados y luego divide entre 3.', '$3x=15$ y al dividir entre 3 se obtiene $x=5$.', 'Olvidar restar el 5 antes de dividir.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$5$'), ('B', '$3$'), ('C', '$15$'), ('D', '$25$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-1-02';

-- DIF-ALG-1-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-1-03', 1, 'OPCION_MULTIPLE', 'Factoriza $x^2-5x+6$.', NULL, NULL, 'Busca dos numeros que sumen -5 y multipliquen 6.', 'Los numeros -2 y -3 suman -5 y multiplican 6, por eso el producto es $(x-2)(x-3)$.', 'Cambiar los signos de los factores.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$(x-2)(x-3)$'), ('B', '$(x+2)(x+3)$'), ('C', '$(x-1)(x-6)$'), ('D', '$(x-2)(x+3)$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-1-03';

-- DIF-ALG-1-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-1-04', 1, 'NUMERICA', 'Si $f(x)=2x-1$, calcula $f(3)$.', 5, 0.0001, 'Sustituye $x=3$ en la formula.', '$f(3)=2(3)-1=6-1=5$.', 'Multiplicar por 3 y no restar 1.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';

-- DIF-ALG-1-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-1-05', 1, 'OPCION_MULTIPLE', 'La solucion de $x^2=16$ es:', NULL, NULL, 'Una ecuacion cuadratica puede tener dos soluciones.', 'Al extraer raiz cuadrada se obtiene $x=4$ o $x=-4$, es decir $x=\pm 4$.', 'Olvidar la solucion negativa.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'C'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x=4$'), ('B', '$x=-4$'), ('C', '$x=\pm 4$'), ('D', '$x=8$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-1-05';

-- DIF-ALG-2-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-2-01', 2, 'OPCION_MULTIPLE', 'Simplifica $\frac{x^2-9}{x-3}$ para $x\ne 3$.', NULL, NULL, 'Factoriza el numerador como diferencia de cuadrados.', '$x^2-9=(x-3)(x+3)$ y al cancelar queda $x+3$.', 'Cancelar terminos que no son factores.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x+3$'), ('B', '$x-3$'), ('C', '$x^2+3$'), ('D', '$1$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-2-01';

-- DIF-ALG-2-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-2-02', 2, 'OPCION_MULTIPLE', 'Resuelve $x^2-5x+6=0$.', NULL, NULL, 'Factoriza el trinomio.', '$(x-2)(x-3)=0$ implica $x=2$ o $x=3$.', 'No cambiar los signos al igualar cada factor a cero.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x=2$ y $x=3$'), ('B', '$x=-2$ y $x=-3$'), ('C', '$x=1$ y $x=6$'), ('D', '$x=-1$ y $x=-6$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-2-02';

-- DIF-ALG-2-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-2-03', 2, 'OPCION_MULTIPLE', 'Resuelve la inecuacion $2x-3<7$.', NULL, NULL, 'Suma 3 y divide entre 2 manteniendo el sentido de la desigualdad.', '$2x<10$, entonces $x<5$.', 'Invertir la desigualdad sin multiplicar por un numero negativo.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x<5$'), ('B', '$x>5$'), ('C', '$x<2$'), ('D', '$x>2$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-2-03';

-- DIF-ALG-2-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-2-04', 2, 'OPCION_MULTIPLE', 'Factoriza $2x^2+7x+3$.', NULL, NULL, 'Busca dos factores cuyo producto de extremos sea 6 y cuya suma de productos cruzados sea 7.', 'Al multiplicar $(2x+1)(x+3)$ se obtiene $2x^2+7x+3$.', 'Elegir factores que no reproducen el termino lineal.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$(2x+1)(x+3)$'), ('B', '$(2x+3)(x+1)$'), ('C', '$(2x-1)(x-3)$'), ('D', '$(x+1)(x+3)$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-2-04';

-- DIF-ALG-2-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-2-05', 2, 'NUMERICA', 'Si $f(x)=x^2$ y $g(x)=x+1$, calcula $f(g(2))$.', 9, 0.0001, 'Primero calcula $g(2)$ y luego evalua $f$ en ese resultado.', '$g(2)=3$ y $f(3)=3^2=9$.', 'Aplicar las funciones en orden contrario.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';

-- DIF-ALG-3-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-3-01', 3, 'OPCION_MULTIPLE', 'Simplifica $\frac{x^2-1}{x^2-3x+2}$ para $x\ne 1,2$.', NULL, NULL, 'Factoriza numerador y denominador.', '$x^2-1=(x-1)(x+1)$ y $x^2-3x+2=(x-1)(x-2)$, se cancela $x-1$.', 'Cancelar $x$ en lugar del factor comun.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac{x+1}{x-2}$'), ('B', '$\frac{x-1}{x-2}$'), ('C', '$\frac{x+1}{x-1}$'), ('D', '$\frac{1}{x-2}$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-3-01';

-- DIF-ALG-3-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-3-02', 3, 'OPCION_MULTIPLE', 'Resuelve $\sqrt{x+5}=x-1$ considerando solo soluciones validas.', NULL, NULL, 'Eleva al cuadrado y verifica las soluciones en la ecuacion original.', 'Al elevar al cuadrado se obtiene $x^2-3x-4=0$, con soluciones 4 y -1. Solo $x=4$ cumple la ecuacion original.', 'No verificar la solucion extraña.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x=4$'), ('B', '$x=-1$'), ('C', '$x=4$ y $x=-1$'), ('D', '$x=5$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-3-02';

-- DIF-ALG-3-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-3-03', 3, 'OPCION_MULTIPLE', 'Factoriza completamente $x^3-4x$.', NULL, NULL, 'Saca factor comun y usa diferencia de cuadrados.', '$x(x^2-4)=x(x-2)(x+2)$.', 'Dejar la diferencia de cuadrados sin factorizar.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x(x-2)(x+2)$'), ('B', '$x(x^2-4x)$'), ('C', '$(x-2)(x^2+2x+4)$'), ('D', '$x(x-4)(x+4)$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-3-03';

-- DIF-ALG-3-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-3-04', 3, 'OPCION_MULTIPLE', 'Resuelve $|2x-1|<5$.', NULL, NULL, 'La desigualdad $|u|<a$ equivale a $-a<u<a$.', '$-5<2x-1<5$, al sumar 1 y dividir entre 2 se obtiene $-2<x<3$.', 'Resolver solo una de las dos desigualdades.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$-2<x<3$'), ('B', '$x<3$'), ('C', '$x>-2$'), ('D', '$x<-2$ o $x>3$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-ALG-3-04';

-- DIF-ALG-3-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-ALG-3-05', 3, 'NUMERICA', 'Cuantos valores reales se excluyen del dominio de $f(x)=\frac{1}{x^2-9}$?', 2, 0.0001, 'Busca los valores que anulan el denominador.', '$x^2-9=0$ en $x=3$ y $x=-3$, por eso se excluyen dos valores.', 'Contar solo el valor positivo.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Algebra y funciones' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - ALGEBRAICA';

-- DIF-LOG-1-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-1-01', 1, 'OPCION_MULTIPLE', 'Cual de las siguientes expresiones es una proposicion?', NULL, NULL, 'Una proposicion afirma algo que puede ser verdadero o falso.', '$2+2=4$ tiene valor de verdad verdadero. Las otras no son afirmaciones con valor de verdad.', 'Confundir una orden o pregunta con una proposicion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2+2=4$'), ('B', 'Que hora es?'), ('C', '$x+1$'), ('D', 'Estudia mucho')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-1-01';

-- DIF-LOG-1-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-1-02', 1, 'OPCION_MULTIPLE', 'Cual es la negacion de Todos los numeros son pares?', NULL, NULL, 'La negacion de todo es existe al menos uno que no.', 'La negacion de una universal es una existencial con el predicado negado.', 'Usar la contraria en lugar de la negacion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Existe al menos un numero que no es par'), ('B', 'Ningun numero es par'), ('C', 'Todos los numeros son impares'), ('D', 'Algun numero es par')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-1-02';

-- DIF-LOG-1-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-1-03', 1, 'OPCION_MULTIPLE', 'Si $p$ es verdadera y $q$ es falsa, el valor de $p\land q$ es:', NULL, NULL, 'La conjuncion solo es verdadera si ambas partes lo son.', 'Como $q$ es falsa, $p\land q$ es falsa.', 'Confundir la conjuncion con la disyuncion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Falso'), ('B', 'Verdadero'), ('C', 'Indeterminado'), ('D', 'No es proposicion')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-1-03';

-- DIF-LOG-1-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-1-04', 1, 'OPCION_MULTIPLE', 'Si llueve, entonces la cancha se moja. Si sabemos que llueve, que se concluye?', NULL, NULL, 'Aplica la regla modus ponens.', 'Si se cumple el antecedente, se cumple el consecuente.', 'Invertir la condicion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'La cancha se moja'), ('B', 'No llueve'), ('C', 'La cancha no se moja'), ('D', 'Nada se puede concluir')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-1-04';

-- DIF-LOG-1-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-1-05', 1, 'OPCION_MULTIPLE', 'Todo numero real tiene cuadrado no negativo es:', NULL, NULL, 'Piensa en el signo de cualquier cuadrado real.', 'Para todo $x\in\mathbb{R}$, $x^2\ge 0$, por eso es universal y verdadera.', 'Creer que un contraejemplo aislado invalida el enunciado.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Una proposicion universal verdadera'), ('B', 'Una proposicion existencial falsa'), ('C', 'Una pregunta'), ('D', 'Una proposicion condicional falsa')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-1-05';

-- DIF-LOG-2-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-2-01', 2, 'OPCION_MULTIPLE', 'La contrapositiva de Si $x$ es par, entonces $x^2$ es par es:', NULL, NULL, 'La contrapositiva de $p\to q$ es $\neg q\to\neg p$.', 'Se niegan y se intercambian antecedente y consecuente.', 'Usar la reciproca en lugar de la contrapositiva.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Si $x^2$ no es par, entonces $x$ no es par'), ('B', 'Si $x$ no es par, entonces $x^2$ no es par'), ('C', 'Si $x^2$ es par, entonces $x$ es par'), ('D', 'Si $x$ es impar, entonces $x^2$ es impar')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-2-01';

-- DIF-LOG-2-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-2-02', 2, 'OPCION_MULTIPLE', 'La negacion de Existe un $x$ tal que $x^2=2$ es:', NULL, NULL, 'La negacion de existe es para todo con el predicado negado.', 'Se cambia el cuantificador y se niega la igualdad.', 'Mantener el cuantificador existencial.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Para todo $x$, $x^2\ne 2$'), ('B', 'Existe un $x$ tal que $x^2\ne 2$'), ('C', 'Para todo $x$, $x^2=2$'), ('D', 'No existe $x$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-2-02';

-- DIF-LOG-2-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-2-03', 2, 'OPCION_MULTIPLE', 'Si $p\to q$ y $p$ son verdaderas, entonces:', NULL, NULL, 'Es la regla de modus ponens.', 'Si el antecedente es verdadero y la implicacion tambien, el consecuente debe ser verdadero.', 'Neguir el consecuente.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$q$ es verdadera'), ('B', '$q$ es falsa'), ('C', '$p$ es falsa'), ('D', 'No se puede concluir')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-2-03';

-- DIF-LOG-2-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-2-04', 2, 'OPCION_MULTIPLE', 'Cual es condicion suficiente para que un numero sea divisible por 6?', NULL, NULL, 'Si un numero es divisible por 12, tambien lo es por 6.', '12 es multiplo de 6, por eso la divisibilidad por 12 garantiza la divisibilidad por 6.', 'Confundir condicion suficiente con necesaria.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Ser divisible por 12'), ('B', 'Ser divisible por 3'), ('C', 'Ser par'), ('D', 'Ser multiplo de 9')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-2-04';

-- DIF-LOG-2-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-2-05', 2, 'OPCION_MULTIPLE', 'Un contraejemplo a Todo numero primo es impar es:', NULL, NULL, 'Busca un primo que no sea impar.', '2 es primo y es par, por eso refuta el enunciado.', 'Elegir un numero compuesto.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2$'), ('B', '$3$'), ('C', '$5$'), ('D', '$9$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-2-05';

-- DIF-LOG-3-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-3-01', 3, 'OPCION_MULTIPLE', 'Si la proposicion $p\to q$ es falsa, entonces:', NULL, NULL, 'Una implicacion solo es falsa cuando el antecedente es verdadero y el consecuente falso.', 'Ese es el unico caso que hace falsa a $p\to q$.', 'Creer que la implicacion es falsa cuando ambos son falsos.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$p$ es verdadera y $q$ es falsa'), ('B', '$p$ es falsa y $q$ es verdadera'), ('C', '$p$ y $q$ son verdaderas'), ('D', '$p$ y $q$ son falsas')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-3-01';

-- DIF-LOG-3-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-3-02', 3, 'OPCION_MULTIPLE', 'La negacion de Para todo $\epsilon>0$ existe $\delta>0$ tal que se cumple $P$ es:', NULL, NULL, 'Niega los cuantificadores en orden.', '$\neg(\forall\epsilon\exists\delta P)$ es $\exists\epsilon\forall\delta\neg P$.', 'Cambiar el orden de los cuantificadores sin negar.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Existe $\epsilon>0$ tal que para todo $\delta>0$ no se cumple $P$'), ('B', 'Para todo $\epsilon>0$ existe $\delta>0$ tal que no se cumple $P$'), ('C', 'Existe $\delta>0$ tal que para todo $\epsilon>0$ se cumple $P$'), ('D', 'No existe ningun $\epsilon>0$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-3-02';

-- DIF-LOG-3-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-3-03', 3, 'OPCION_MULTIPLE', 'Si $f$ es derivable entonces $f$ es continua. La reciproca es:', NULL, NULL, 'La reciproca intercambia antecedente y consecuente sin negarlos.', 'La reciproca de $p\to q$ es $q\to p$.', 'Confundir reciproca con contrapositiva.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Si $f$ es continua entonces $f$ es derivable'), ('B', 'Si $f$ no es continua entonces no es derivable'), ('C', 'Si $f$ es derivable entonces es continua'), ('D', 'Si $f$ no es derivable entonces no es continua')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-3-03';

-- DIF-LOG-3-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-3-04', 3, 'OPCION_MULTIPLE', 'Cual argumento es valido?', NULL, NULL, 'Es el silogismo hipotetico.', 'Encadenar dos implicaciones permite concluir la implicacion de extremos.', 'Afirmar el consecuente.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Si $p\to q$ y $q\to r$, entonces $p\to r$'), ('B', 'Si $p\to q$ y $q$, entonces $p$'), ('C', 'Si $p\to q$ y $\neg p$, entonces $\neg q$'), ('D', 'Si $p\lor q$, entonces $p$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-3-04';

-- DIF-LOG-3-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-LOG-3-05', 3, 'OPCION_MULTIPLE', 'La continuidad de $f$ en $a$ es condicion ___ para que $f$ tenga limite en $a$.', NULL, NULL, 'La continuidad exige que el limite exista y coincida con el valor.', 'Toda funcion continua en $a$ tiene limite en $a$, pero una funcion puede tener limite sin ser continua.', 'Confundir suficiencia con necesidad.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Suficiente pero no necesaria'), ('B', 'Necesaria pero no suficiente'), ('C', 'Necesaria y suficiente'), ('D', 'Ni necesaria ni suficiente')) AS o (letra, texto)
WHERE e.codigo = 'DIF-LOG-3-05';

-- DIF-FUN-1-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-1-01', 1, 'OPCION_MULTIPLE', 'Si $f(x)=3x+2$, entonces $f(4)$ es:', NULL, NULL, 'Sustituye $x=4$.', '$f(4)=3(4)+2=14$.', 'Sumar 4 en lugar de multiplicar.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$14$'), ('B', '$12$'), ('C', '$10$'), ('D', '$18$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-1-01';

-- DIF-FUN-1-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-1-02', 1, 'OPCION_MULTIPLE', 'El dominio de $f(x)=\frac{1}{x}$ es:', NULL, NULL, 'El denominador no puede ser cero.', 'La funcion no esta definida en $x=0$, por eso se excluye ese valor.', 'Incluir el cero en el dominio.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Todos los reales excepto 0'), ('B', 'Todos los reales'), ('C', 'Los reales positivos'), ('D', 'Los enteros')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-1-02';

-- DIF-FUN-1-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-1-03', 1, 'OPCION_MULTIPLE', 'El rango de $f(x)=x^2$ es:', NULL, NULL, 'Un cuadrado real nunca es negativo.', 'Incluye el 0 porque $0^2=0$.', 'Excluir el cero del rango.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$[0,\infty)$'), ('B', '$(-\infty,\infty)$'), ('C', '$(0,\infty)$'), ('D', '$[1,\infty)$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-1-03';

-- DIF-FUN-1-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-1-04', 1, 'OPCION_MULTIPLE', 'La pendiente de la recta $y=2x-3$ es:', NULL, NULL, 'Compara con la forma $y=mx+b$.', 'El coeficiente de $x$ es 2, por eso la pendiente es 2.', 'Confundir la pendiente con el intercepto.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2$'), ('B', '$-3$'), ('C', '$3$'), ('D', '$\frac{1}{2}$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-1-04';

-- DIF-FUN-1-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-1-05', 1, 'NUMERICA', 'Si $f(x)=x^2-1$, calcula $f(-2)$.', 3, 0.0001, 'Sustituye $x=-2$ y recuerda que el cuadrado es positivo.', '$f(-2)=(-2)^2-1=4-1=3$.', 'Calcular el cuadrado como negativo.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';

-- DIF-FUN-2-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-2-01', 2, 'OPCION_MULTIPLE', '$\lim_{x\to 2}(3x-1)$ es:', NULL, NULL, 'La funcion es polinomica y continua, evalua directamente.', '$3(2)-1=5$.', 'Sustituir mal el valor de $x$.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$5$'), ('B', '$6$'), ('C', '$2$'), ('D', '$1$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-2-01';

-- DIF-FUN-2-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-2-02', 2, 'OPCION_MULTIPLE', 'La derivada de $f(x)=x^3$ es:', NULL, NULL, 'Aplica la regla de la potencia.', 'La derivada de $x^n$ es $nx^{n-1}$, con $n=3$ queda $3x^2$.', 'Bajar el exponente sin restarle uno.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$3x^2$'), ('B', '$x^2$'), ('C', '$3x$'), ('D', '$x^4$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-2-02';

-- DIF-FUN-2-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-2-03', 2, 'OPCION_MULTIPLE', 'Si $f(x)=2x+1$ y $g(x)=x^2$, entonces $(f\circ g)(x)$ es:', NULL, NULL, 'Primero aplica $g$ y luego $f$.', '$f(g(x))=2(x^2)+1=2x^2+1$.', 'Elevar al cuadrado toda la funcion $f$.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2x^2+1$'), ('B', '$4x^2+1$'), ('C', '$2x^2+2x+1$'), ('D', '$x^2+2x+1$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-2-03';

-- DIF-FUN-2-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-2-04', 2, 'OPCION_MULTIPLE', '$\lim_{x\to 0}\frac{\sin x}{x}$ es:', NULL, NULL, 'Es un limite notable.', 'Cerca de 0, $\sin x$ y $x$ son equivalentes, por eso el cociente tiende a 1.', 'Sustituir y concluir que es indefinido.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$1$'), ('B', '$0$'), ('C', '$\infty$'), ('D', 'No existe')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-2-04';

-- DIF-FUN-2-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-2-05', 2, 'NUMERICA', 'La derivada de $f(x)=x^2$ evaluada en $x=3$ es:', 6, 0.0001, 'Deriva y luego evalua.', '$f''(x)=2x$, entonces $f''(3)=6$.', 'Evaluar la funcion original en lugar de la derivada.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';

-- DIF-FUN-3-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-3-01', 3, 'OPCION_MULTIPLE', '$\lim_{x\to 1}\frac{x^2-1}{x-1}$ es:', NULL, NULL, 'Factoriza el numerador.', '$\frac{(x-1)(x+1)}{x-1}=x+1$ y al evaluar en 1 se obtiene 2.', 'Sustituir directamente y concluir que no existe.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2$'), ('B', '$1$'), ('C', '$0$'), ('D', 'No existe')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-3-01';

-- DIF-FUN-3-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-3-02', 3, 'OPCION_MULTIPLE', 'La derivada de $f(x)=(2x+1)^5$ es:', NULL, NULL, 'Usa la regla de la cadena.', 'La derivada externa es $5(2x+1)^4$ y la interna es 2, el producto es $10(2x+1)^4$.', 'Olvidar multiplicar por la derivada interna.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$10(2x+1)^4$'), ('B', '$5(2x+1)^4$'), ('C', '$10x(2x+1)^4$'), ('D', '$(2x+1)^4$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-3-02';

-- DIF-FUN-3-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-3-03', 3, 'OPCION_MULTIPLE', 'La recta tangente a $y=x^2$ en $x=1$ es:', NULL, NULL, 'La pendiente es $y''(1)$ y pasa por $(1,1)$.', '$y''(1)=2$, con punto $(1,1)$ la ecuacion es $y-1=2(x-1)$, es decir $y=2x-1$.', 'Usar el punto equivocado.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$y=2x-1$'), ('B', '$y=x-1$'), ('C', '$y=2x+1$'), ('D', '$y=x^2+1$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-3-03';

-- DIF-FUN-3-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-3-04', 3, 'OPCION_MULTIPLE', 'La funcion $f(x)=x^3-3x$ tiene un minimo local en:', NULL, NULL, 'Deriva, iguala a cero y usa la segunda derivada.', '$f''(x)=3x^2-3$ se anula en $\pm1$. $f''''(1)=6>0$, por eso hay minimo en $x=1$.', 'Confundir el maximo con el minimo.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x=1$'), ('B', '$x=-1$'), ('C', '$x=0$'), ('D', '$x=3$')) AS o (letra, texto)
WHERE e.codigo = 'DIF-FUN-3-04';

-- DIF-FUN-3-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'DIF-FUN-3-05', 3, 'NUMERICA', 'La derivada de $f(x)=x^3-3x$ evaluada en $x=2$ es:', 9, 0.0001, 'Deriva y sustituye.', '$f''(x)=3x^2-3$, entonces $f''(2)=12-3=9$.', 'Olvidar restar 3.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_DIFERENCIAL' AND p.tema = 'Funciones limites y derivadas' AND p.titulo = 'Semilla - CALCULO_DIFERENCIAL - FUNCIONAL';

-- INT-ALG-1-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-1-01', 1, 'OPCION_MULTIPLE', '$\int 2x\,dx$ es:', NULL, NULL, 'Busca una funcion cuya derivada sea $2x$.', 'La derivada de $x^2$ es $2x$, por eso la primitiva es $x^2+C$.', 'Olvidar la constante de integracion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x^2+C$'), ('B', '$2x+C$'), ('C', '$x^2$'), ('D', '$2+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-1-01';

-- INT-ALG-1-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-1-02', 1, 'OPCION_MULTIPLE', 'Simplifica $3x+2x-5$.', NULL, NULL, 'Suma los terminos semejantes.', '$3x+2x=5x$, entonces queda $5x-5$.', 'Eliminar el termino constante.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$5x-5$'), ('B', '$5x$'), ('C', '$6x-5$'), ('D', '$x-5$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-1-02';

-- INT-ALG-1-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-1-03', 1, 'OPCION_MULTIPLE', 'Si $F(x)=x^3$, entonces $F''(x)$ es:', NULL, NULL, 'Aplica la regla de la potencia.', 'La derivada de $x^3$ es $3x^2$.', 'Dividir en lugar de multiplicar por el exponente.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$3x^2$'), ('B', '$x^2$'), ('C', '$3x$'), ('D', '$x^4$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-1-03';

-- INT-ALG-1-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-1-04', 1, 'NUMERICA', '$\int_0^2 3\,dx$ es:', 6, 0.0001, 'La integral de una constante es la constante por la longitud del intervalo.', '$3(2-0)=6$.', 'Multiplicar por el limite superior solamente.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';

-- INT-ALG-1-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-1-05', 1, 'OPCION_MULTIPLE', 'Factoriza $x^2-4$.', NULL, NULL, 'Es una diferencia de cuadrados.', '$x^2-4=(x-2)(x+2)$.', 'Usar factores que no reproducen el termino lineal nulo.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$(x-2)(x+2)$'), ('B', '$(x-4)(x+1)$'), ('C', '$(x-2)^2$'), ('D', '$(x+4)(x-1)$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-1-05';

-- INT-ALG-2-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-2-01', 2, 'OPCION_MULTIPLE', '$\int(3x^2+2x)\,dx$ es:', NULL, NULL, 'Integra cada termino con la regla de la potencia.', 'La primitiva de $3x^2$ es $x^3$ y la de $2x$ es $x^2$.', 'Derivar en lugar de integrar.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x^3+x^2+C$'), ('B', '$3x^3+x^2+C$'), ('C', '$x^3+2x^2+C$'), ('D', '$6x+2+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-2-01';

-- INT-ALG-2-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-2-02', 2, 'OPCION_MULTIPLE', '$\int_1^3 2x\,dx$ es:', NULL, NULL, 'La primitiva es $x^2$, evalua en los limites.', '$3^2-1^2=9-1=8$.', 'Restar al reves o evaluar solo un limite.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$8$'), ('B', '$6$'), ('C', '$4$'), ('D', '$9$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-2-02';

-- INT-ALG-2-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-2-03', 2, 'OPCION_MULTIPLE', '$\int(2x+1)^2\,dx$ es:', NULL, NULL, 'Usa sustitucion con $u=2x+1$.', '$du=2dx$, por eso la integral es $\frac12\cdot\frac{u^3}{3}+C=\frac{(2x+1)^3}{6}+C$.', 'Olvidar dividir entre la derivada interna.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac{(2x+1)^3}{6}+C$'), ('B', '$\frac{(2x+1)^3}{3}+C$'), ('C', '$(2x+1)^3+C$'), ('D', '$2(2x+1)^3+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-2-03';

-- INT-ALG-2-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-2-04', 2, 'OPCION_MULTIPLE', 'Simplifica $\frac{x^2-1}{x-1}$ para $x\ne 1$.', NULL, NULL, 'Factoriza la diferencia de cuadrados.', '$\frac{(x-1)(x+1)}{x-1}=x+1$.', 'Cancelar terminos que no son factores.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x+1$'), ('B', '$x-1$'), ('C', '$x^2+1$'), ('D', '$1$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-2-04';

-- INT-ALG-2-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-2-05', 2, 'NUMERICA', '$\int_0^1 x^2\,dx$ es:', 0.333333, 0.0001, 'La primitiva de $x^2$ es $x^3/3$.', '$\frac{1^3}{3}-\frac{0^3}{3}=\frac13$.', 'Integrar como $x^3$ sin dividir entre 3.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';

-- INT-ALG-3-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-3-01', 3, 'OPCION_MULTIPLE', '$\int\frac{1}{x^2-1}\,dx$ es:', NULL, NULL, 'Descompone en fracciones parciales.', '$\frac{1}{x^2-1}=\frac{1/2}{x-1}-\frac{1/2}{x+1}$, al integrar queda la expresion con logaritmos.', 'Olvidar el factor $\frac12$.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac12\ln\left|\frac{x-1}{x+1}\right|+C$'), ('B', '$\ln|x^2-1|+C$'), ('C', '$\frac12\ln|x^2-1|+C$'), ('D', '$\arctan x+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-3-01';

-- INT-ALG-3-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-3-02', 3, 'OPCION_MULTIPLE', '$\int x e^x\,dx$ es:', NULL, NULL, 'Integra por partes con $u=x$ y $dv=e^x dx$.', '$\int x e^x dx=x e^x-\int e^x dx=(x-1)e^x+C$.', 'Elegir mal las partes.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$(x-1)e^x+C$'), ('B', '$(x+1)e^x+C$'), ('C', '$xe^x+C$'), ('D', '$e^x+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-3-02';

-- INT-ALG-3-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-3-03', 3, 'OPCION_MULTIPLE', '$\int\sin^2 x\,dx$ es:', NULL, NULL, 'Usa $\sin^2 x=\frac{1-\cos(2x)}{2}$.', 'Al integrar se obtiene $\frac{x}{2}-\frac{\sin(2x)}{4}+C$.', 'Integrar $\cos(2x)$ sin dividir entre 2.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac{x}{2}-\frac{\sin(2x)}{4}+C$'), ('B', '$\frac{x}{2}+\frac{\sin(2x)}{4}+C$'), ('C', '$-\frac{\cos(2x)}{2}+C$'), ('D', '$\sin x\cos x+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-3-03';

-- INT-ALG-3-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-3-04', 3, 'OPCION_MULTIPLE', '$\int_1^\infty\frac{1}{x^2}\,dx$ es:', NULL, NULL, 'Es una integral impropia, calcula el limite.', '$\int_1^b x^{-2}dx=1-\frac1b$, al tomar $b\to\infty$ queda 1.', 'Tratar el infinito como un numero.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$1$'), ('B', '$\infty$'), ('C', '$0$'), ('D', '$\frac12$')) AS o (letra, texto)
WHERE e.codigo = 'INT-ALG-3-04';

-- INT-ALG-3-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-ALG-3-05', 3, 'NUMERICA', '$\int_0^1 xe^x\,dx$ es:', 1, 0.0001, 'Integra por partes y evalua.', 'La primitiva es $(x-1)e^x$, en 1 vale 0 y en 0 vale -1, la diferencia es 1.', 'Evaluar mal el limite inferior.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Algebra para integracion' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - ALGEBRAICA';

-- INT-LOG-1-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-1-01', 1, 'OPCION_MULTIPLE', 'Si $f$ es continua en $[a,b]$, entonces $f$ es integrable en $[a,b]$. Esta afirmacion es:', NULL, NULL, 'La continuidad es una condicion suficiente de integrabilidad.', 'Toda funcion continua en un intervalo cerrado es integrable alli.', 'Creer que se necesita derivabilidad.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Verdadera para toda funcion continua'), ('B', 'Falsa siempre'), ('C', 'Verdadera solo si $f$ es positiva'), ('D', 'Verdadera solo si $f$ es derivable')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-1-01';

-- INT-LOG-1-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-1-02', 1, 'OPCION_MULTIPLE', 'La negacion de Toda funcion continua es integrable es:', NULL, NULL, 'Niega la universal con una existencial.', 'Se afirma que existe al menos una funcion continua que no es integrable.', 'Usar la reciproca.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Existe una funcion continua que no es integrable'), ('B', 'Ninguna funcion continua es integrable'), ('C', 'Toda funcion no continua es integrable'), ('D', 'Existe una funcion integrable que no es continua')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-1-02';

-- INT-LOG-1-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-1-03', 1, 'OPCION_MULTIPLE', 'Si $p$ es verdadera y $q$ es falsa, $p\lor q$ es:', NULL, NULL, 'La disyuncion es verdadera si al menos una parte lo es.', 'Como $p$ es verdadera, $p\lor q$ es verdadera.', 'Exigir que ambas sean verdaderas.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Verdadera'), ('B', 'Falsa'), ('C', 'Indeterminada'), ('D', 'No es proposicion')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-1-03';

-- INT-LOG-1-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-1-04', 1, 'OPCION_MULTIPLE', 'Si una funcion es derivable entonces es continua. Si $f$ no es continua, se concluye:', NULL, NULL, 'Usa la contrapositiva.', 'La contrapositiva de derivable implica continua es no continua implica no derivable.', 'Neguir el antecedente.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$f$ no es derivable'), ('B', '$f$ es derivable'), ('C', '$f$ es integrable'), ('D', 'No se puede concluir')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-1-04';

-- INT-LOG-1-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-1-05', 1, 'OPCION_MULTIPLE', 'La igualdad $\int_a^a f(x)\,dx=0$ es:', NULL, NULL, 'El intervalo de integracion tiene longitud cero.', 'Sin importar el valor de $f$, el area sobre un intervalo de longitud cero es 0.', 'Creer que depende de la funcion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Verdadera para toda funcion integrable'), ('B', 'Falsa siempre'), ('C', 'Verdadera solo si $f=0$'), ('D', 'Verdadera solo si $a=0$')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-1-05';

-- INT-LOG-2-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-2-01', 2, 'OPCION_MULTIPLE', 'La contrapositiva de Si $f$ es integrable entonces $f$ es acotada es:', NULL, NULL, 'La contrapositiva de $p\to q$ es $\neg q\to\neg p$.', 'Se niegan y se intercambian los enunciados.', 'Usar la reciproca.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Si $f$ no es acotada entonces $f$ no es integrable'), ('B', 'Si $f$ no es integrable entonces $f$ no es acotada'), ('C', 'Si $f$ es acotada entonces $f$ es integrable'), ('D', 'Si $f$ es integrable entonces $f$ es continua')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-2-01';

-- INT-LOG-2-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-2-02', 2, 'OPCION_MULTIPLE', 'La negacion de Existe una primitiva para toda funcion continua es:', NULL, NULL, 'Niega la universal interna.', 'La negacion de para toda funcion continua existe primitiva es existe una funcion continua sin primitiva.', 'Mantener el cuantificador universal.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Existe una funcion continua sin primitiva'), ('B', 'Toda funcion continua tiene primitiva'), ('C', 'Ninguna funcion continua tiene primitiva'), ('D', 'Toda funcion sin primitiva es continua')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-2-02';

-- INT-LOG-2-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-2-03', 2, 'OPCION_MULTIPLE', 'Si $f$ es continua, $f\ge 0$ en $[a,b]$ y $\int_a^b f=0$, entonces:', NULL, NULL, 'Si una funcion continua no negativa tuviera un valor positivo, el area seria positiva.', 'Por continuidad, un valor positivo implicaria un intervalo donde $f>0$ y la integral no seria cero.', 'Ignorar la continuidad.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$f=0$ en $[a,b]$'), ('B', '$f>0$ en algun punto'), ('C', '$f$ es constante positiva'), ('D', 'No se puede concluir')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-2-03';

-- INT-LOG-2-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-2-04', 2, 'OPCION_MULTIPLE', 'Que $f$ sea continua es condicion ___ para que $f$ tenga primitiva.', NULL, NULL, 'Toda funcion continua tiene primitiva, pero hay funciones no continuas con primitiva.', 'La continuidad garantiza la existencia, pero no es obligatoria.', 'Confundir suficiencia con necesidad.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Suficiente pero no necesaria'), ('B', 'Necesaria pero no suficiente'), ('C', 'Necesaria y suficiente'), ('D', 'Ni necesaria ni suficiente')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-2-04';

-- INT-LOG-2-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-2-05', 2, 'OPCION_MULTIPLE', 'Un contraejemplo a Toda funcion integrable es continua es:', NULL, NULL, 'Busca una funcion integrable con una discontinuidad.', 'La funcion escalon es integrable y tiene saltos, por eso no es continua.', 'Elegir una funcion continua.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Una funcion escalon'), ('B', 'Una funcion constante'), ('C', 'Una funcion polinomica'), ('D', 'Una funcion exponencial')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-2-05';

-- INT-LOG-3-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-3-01', 3, 'OPCION_MULTIPLE', 'Si $f$ es impar y el intervalo $[-a,a]$ es simetrico, entonces $\int_{-a}^{a}f(x)\,dx$ es:', NULL, NULL, 'Las areas de los lados se cancelan.', 'Para una funcion impar, $f(-x)=-f(x)$, la integral sobre el intervalo simetrico es cero.', 'Sumar las areas sin considerar el signo.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$0$'), ('B', '$2\int_0^a f$'), ('C', '$\int_0^a f$'), ('D', 'No existe')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-3-01';

-- INT-LOG-3-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-3-02', 3, 'OPCION_MULTIPLE', 'Si $f$ es continua en $[a,b]$ y $F(x)=\int_a^x f(t)\,dt$, entonces:', NULL, NULL, 'Es el Teorema Fundamental del Calculo.', 'La acumulacion de una funcion continua tiene derivada igual a la funcion.', 'Derivar el integrando sin aplicar la regla de la cadena cuando el limite es variable.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$F$ es derivable y $F''=f$'), ('B', '$F$ es constante'), ('C', '$F$ no es derivable'), ('D', '$F''=0$')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-3-02';

-- INT-LOG-3-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-3-03', 3, 'OPCION_MULTIPLE', 'La negacion de Para toda particion $P$, $U(f,P)=L(f,P)$ es:', NULL, NULL, 'Niega la universal con una existencial.', 'Se conserva la particion y se niega la igualdad.', 'Cambiar el cuantificador y mantener la igualdad.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'Existe una particion $P$ tal que $U(f,P)\ne L(f,P)$'), ('B', 'Para toda particion $P$, $U(f,P)\ne L(f,P)$'), ('C', 'Existe una particion $P$ tal que $U(f,P)=L(f,P)$'), ('D', 'No existe ninguna particion')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-3-03';

-- INT-LOG-3-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-3-04', 3, 'OPCION_MULTIPLE', 'Si $f\le g$ en $[a,b]$, entonces $\int_a^b f\le\int_a^b g$. La reciproca:', NULL, NULL, 'La desigualdad de integrales no permite comparar punto a punto.', 'Dos funciones pueden tener integrales ordenadas sin cumplir $f\le g$ en todos los puntos.', 'Confundir la proposicion con su reciproca.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', 'No es valida en general'), ('B', 'Siempre es valida'), ('C', 'Solo vale si $f$ y $g$ son positivas'), ('D', 'Solo vale si $a=0$')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-3-04';

-- INT-LOG-3-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-LOG-3-05', 3, 'OPCION_MULTIPLE', 'Si $f$ es continua, no negativa y $\int_a^b f=0$, entonces:', NULL, NULL, 'Si $f$ fuera positiva en un punto, por continuidad lo seria en un intervalo.', 'Una funcion continua no negativa con integral cero debe ser identicamente cero.', 'Suponer que la integral cero permite valores positivos aislados.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Razonamiento logico' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - LOGICA';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$f=0$ en $[a,b]$'), ('B', '$f$ es positiva en algun punto'), ('C', '$f$ es discontinua'), ('D', '$f$ no es integrable')) AS o (letra, texto)
WHERE e.codigo = 'INT-LOG-3-05';

-- INT-FUN-1-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-1-01', 1, 'OPCION_MULTIPLE', '$\int_0^2 1\,dx$ es:', NULL, NULL, 'La integral de 1 mide la longitud del intervalo.', 'El intervalo mide $2-0=2$.', 'Multiplicar por el limite superior sin restar el inferior.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2$'), ('B', '$1$'), ('C', '$0$'), ('D', '$4$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-1-01';

-- INT-FUN-1-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-1-02', 1, 'OPCION_MULTIPLE', 'El area bajo $y=2x$ desde $x=0$ hasta $x=1$ es:', NULL, NULL, 'Calcula $\int_0^1 2x\,dx$.', 'La primitiva es $x^2$, evaluando queda $1-0=1$.', 'Usar la altura final en lugar del area.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$1$'), ('B', '$2$'), ('C', '$\frac12$'), ('D', '$4$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-1-02';

-- INT-FUN-1-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-1-03', 1, 'OPCION_MULTIPLE', 'Si $F(x)=x^2$, entonces $F(3)-F(1)$ es:', NULL, NULL, 'Evalua la funcion en los dos limites y resta.', '$9-1=8$.', 'Restar los limites antes de evaluar.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$8$'), ('B', '$6$'), ('C', '$9$'), ('D', '$4$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-1-03';

-- INT-FUN-1-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-1-04', 1, 'NUMERICA', '$\int_0^3 2\,dx$ es:', 6, 0.0001, 'La integral de una constante es constante por longitud.', '$2(3-0)=6$.', 'Integrar como si fuera $2x$.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';

-- INT-FUN-1-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-1-05', 1, 'OPCION_MULTIPLE', 'La derivada de $G(x)=\int_0^x t^2\,dt$ es:', NULL, NULL, 'Aplica el Teorema Fundamental del Calculo.', 'La derivada de la acumulacion de $t^2$ es $x^2$.', 'Derivar el limite superior y olvidar evaluar el integrando.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$x^2$'), ('B', '$x^3$'), ('C', '$2x$'), ('D', '$0$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-1-05';

-- INT-FUN-2-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-2-01', 2, 'OPCION_MULTIPLE', '$\int_0^1(x^2+1)\,dx$ es:', NULL, NULL, 'Integra termino a termino y evalua.', '$\frac{x^3}{3}+x$ de 0 a 1 es $\frac13+1=\frac43$.', 'Olvidar integrar el 1.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac43$'), ('B', '$1$'), ('C', '$\frac13$'), ('D', '$2$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-2-01';

-- INT-FUN-2-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-2-02', 2, 'OPCION_MULTIPLE', 'El area entre $y=x$ y $y=x^2$ de $x=0$ a $x=1$ es:', NULL, NULL, 'Plantea $\int_0^1(x-x^2)\,dx$.', 'La integral es $\frac{x^2}{2}-\frac{x^3}{3}$, evaluando queda $\frac12-\frac13=\frac16$.', 'Restar en el orden equivocado.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac16$'), ('B', '$\frac12$'), ('C', '$\frac13$'), ('D', '$1$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-2-02';

-- INT-FUN-2-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-2-03', 2, 'OPCION_MULTIPLE', '$\int 2x\cos(x^2)\,dx$ es:', NULL, NULL, 'Usa $u=x^2$.', '$du=2x\,dx$, por eso la integral es $\sin u+C=\sin(x^2)+C$.', 'Olvidar la sustitucion.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\sin(x^2)+C$'), ('B', '$\cos(x^2)+C$'), ('C', '$2\sin(x^2)+C$'), ('D', '$x^2\sin(x^2)+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-2-03';

-- INT-FUN-2-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-2-04', 2, 'OPCION_MULTIPLE', 'El valor promedio de $f(x)=x$ en $[0,2]$ es:', NULL, NULL, 'El promedio es $\frac{1}{b-a}\int_a^b f$.', '$\frac12\int_0^2 x\,dx=\frac12\cdot2=1$.', 'Dividir entre la longitud equivocada.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$1$'), ('B', '$2$'), ('C', '$\frac12$'), ('D', '$0$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-2-04';

-- INT-FUN-2-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-2-05', 2, 'NUMERICA', '$\int_0^1 3x^2\,dx$ es:', 1, 0.0001, 'La primitiva de $3x^2$ es $x^3$.', '$1^3-0^3=1$.', 'Dividir entre 3 despues de integrar.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';

-- INT-FUN-3-01
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-3-01', 3, 'OPCION_MULTIPLE', '$\int_0^\infty e^{-x}\,dx$ es:', NULL, NULL, 'Es una integral impropia, calcula el limite.', '$\int_0^b e^{-x}dx=1-e^{-b}$, al tomar $b\to\infty$ queda 1.', 'Evaluar el limite inferior como 1.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$1$'), ('B', '$\infty$'), ('C', '$0$'), ('D', '$e$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-3-01';

-- INT-FUN-3-02
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-3-02', 3, 'OPCION_MULTIPLE', '$\int x\ln x\,dx$ es:', NULL, NULL, 'Integra por partes con $u=\ln x$ y $dv=x\,dx$.', '$\int x\ln x\,dx=\frac{x^2}{2}\ln x-\int\frac{x}{2}dx=\frac{x^2}{2}\ln x-\frac{x^2}{4}+C$.', 'Olvidar restar la integral de $v\,du$.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac{x^2}{2}\ln x-\frac{x^2}{4}+C$'), ('B', '$\frac{x^2}{2}\ln x+\frac{x^2}{4}+C$'), ('C', '$x\ln x-x+C$'), ('D', '$\frac{x^2}{2}+C$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-3-02';

-- INT-FUN-3-03
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-3-03', 3, 'OPCION_MULTIPLE', 'El area entre $y=x^2$ y $y=2x$ de $x=0$ a $x=2$ es:', NULL, NULL, 'La recta esta por encima en el intervalo.', '$\int_0^2(2x-x^2)dx=x^2-\frac{x^3}{3}$, evaluando queda $4-\frac83=\frac43$.', 'Integrar la diferencia al reves.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$\frac43$'), ('B', '$2$'), ('C', '$\frac83$'), ('D', '$\frac23$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-3-03';

-- INT-FUN-3-04
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-3-04', 3, 'OPCION_MULTIPLE', 'Si $F(x)=\int_0^{x^2}\sin t\,dt$, entonces $F''(x)$ es:', NULL, NULL, 'Aplica el Teorema Fundamental del Calculo con regla de la cadena.', 'La derivada es $\sin(x^2)\cdot2x$.', 'Olvidar la derivada del limite superior.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';
INSERT INTO opcion_ejercicio (ejercicio_id, letra, texto, es_correcta)
SELECT e.id, o.letra, o.texto, o.letra = 'A'
FROM ejercicio e
CROSS JOIN (VALUES ('A', '$2x\sin(x^2)$'), ('B', '$\sin(x^2)$'), ('C', '$2x\cos(x^2)$'), ('D', '$\cos(x^2)$')) AS o (letra, texto)
WHERE e.codigo = 'INT-FUN-3-04';

-- INT-FUN-3-05
INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, respuesta_numerica, tolerancia, pista, explicacion, error_comun, activo)
SELECT p.id, p.competencia_id, 'INT-FUN-3-05', 3, 'NUMERICA', '$\int_1^e\frac{1}{x}\,dx$ es:', 1, 0.0001, 'La primitiva de $1/x$ es $\ln|x|$.', '$\ln e-\ln 1=1-0=1$.', 'Evaluar como $1/e$.', TRUE
FROM contenido p WHERE p.asignatura = 'CALCULO_INTEGRAL' AND p.tema = 'Integrales y aplicaciones' AND p.titulo = 'Semilla - CALCULO_INTEGRAL - FUNCIONAL';

