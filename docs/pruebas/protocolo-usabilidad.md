# Protocolo de validación de usabilidad

**Proyecto:** Aprendizaje USCO
**Requisito:** RNF-01
**Tareas relacionadas:** #103, #143, #144
**Estado:** protocolo listo; simulación interna técnica realizada; sesiones con usuarios reales pendientes.

## 1. Objetivo

Comprobar que estudiantes, docentes y administradores pueden completar las tareas principales de la plataforma sin ayuda, con una experiencia aceptable.

## 2. Umbral de aceptación

- **≥ 80 % de las tareas completadas sin ayuda.**
- **SUS ≥ 68.**
- Sin errores críticos que impidan terminar una tarea.

## 3. Participantes

### Sesiones reales

- 4 a 6 participantes.
- Al menos 2 estudiantes y 2 docentes; si es posible, 1 administrador.
- Estudiantes: haber usado una plataforma educativa o estar cursando Cálculo.
- Docentes: dictar o haber dictado Cálculo Diferencial.
- No deben haber participado en el desarrollo del MVP.

### Simulación interna

- 2 o 3 integrantes del equipo que no hayan sido moderadores de la sesión.
- Sirve para probar el protocolo, los tiempos y las instrucciones; **no sustituye** la validación con usuarios reales.

## 4. Tareas

| # | Rol | Tarea | Criterio de éxito | Tiempo máximo orientativo |
|---|---|---|---|---|
| T1 | Todos | Iniciar sesión con una cuenta de prueba | Entra al inicio de su rol sin ayuda | 2 min |
| T2 | Estudiante | Presentar la prueba diagnóstica | Inicia, responde los 24 ítems y finaliza | 30 min |
| T3 | Estudiante | Resolver tres ejercicios de práctica | Envía los tres, recibe retroalimentación y avanza | 10 min |
| T4 | Estudiante | Consultar progreso e historial | Encuentra sus indicadores y al menos una actividad del historial | 4 min |
| T5 | Docente | Consultar un grupo y el detalle de un estudiante | Selecciona el grupo, ve indicadores y abre el detalle | 5 min |
| T6 | Administrador | Crear o desactivar un usuario | Completa el flujo y entiende la confirmación | 5 min |

## 5. Métricas

Por tarea:

- Completada sin ayuda: sí / no.
- Ayuda necesaria: ninguna, pista, intervención.
- Tiempo.
- Errores observados.
- Comentarios del participante.

Por sesión:

- Porcentaje de tareas sin ayuda.
- Incidencias de usabilidad.
- Puntaje SUS.
- Observaciones generales.

## 6. Instrumento SUS

Después de las tareas, aplicar el cuestionario SUS de 10 afirmaciones con escala Likert de 1 a 5:

1. Me gustaría usar esta plataforma con frecuencia.
2. Encontré la plataforma innecesariamente compleja.
3. Pensé que la plataforma era fácil de usar.
4. Creo que necesitaría ayuda técnica para usar la plataforma.
5. Encontré las funciones de la plataforma bien integradas.
6. Pensé que había demasiada inconsistencia en la plataforma.
7. Imagino que la mayoría de las personas aprendería a usar la plataforma rápidamente.
8. Encontré la plataforma muy difícil de usar.
9. Me sentí muy seguro al usar la plataforma.
10. Necesité aprender muchas cosas antes de poder usar la plataforma.

**Cálculo:** para los ítems impares se resta 1; para los pares se resta el valor de 5. La suma se multiplica por 2,5. El resultado va de 0 a 100.

## 7. Procedimiento de sesión

1. Recibir al participante y explicar el objetivo.
2. Firmar el consentimiento informado (fuera del repositorio).
3. Pedir que piense en voz alta.
4. Recordar que se evalúa la plataforma, no a la persona.
5. Ejecutar las tareas una por una, sin ayudar salvo que se bloquee.
6. Registrar observaciones con códigos `P01`, `P02`, etc.
7. Aplicar el SUS al final.
8. Agradecer y cerrar.

## 8. Roles del equipo

- **Moderador:** explica la sesión, lee las tareas y evita dar pistas.
- **Observador:** toma tiempos, errores y comentarios.
- **Responsable de datos:** verifica la anonimización y resguarda los consentimientos.

## 9. Manejo de datos

- No se guardan nombres, correos reales ni grabaciones sin consentimiento.
- Los participantes se identifican como `P01`, `P02`, etc.
- Los consentimientos firmados se guardan fuera del repositorio.
- Solo se publican observaciones anonimizadas.
- Tratamiento conforme a la Ley 1581 de 2012.

## 10. Resultados

- Tabla de observaciones: [`observaciones-piloto.md`](observaciones-piloto.md).
- Cálculo del SUS: tarea #144, a cargo de Nicolás.
- Comparación con el umbral: RNF-01.

## 11. Pendientes manuales

- Reclutar y agendar a los participantes.
- Firmar los consentimientos.
- Ejecutar las sesiones con usuarios reales.
- Aplicar y tabular el SUS.
- Repetir el recorrido en staging cuando exista #33.
