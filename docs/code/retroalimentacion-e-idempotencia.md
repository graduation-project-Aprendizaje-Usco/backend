# Retroalimentación inmediata e idempotencia de intentos

**Proyecto:** Aprendizaje USCO<br>
**Épica:** EPIC-3 — Práctica adaptativa y retroalimentación<br>
**Tareas:** 110 (subtas 111–113) y 114 (subtas 115–116)<br>
**Responsable:** Juan David Polanco<br>
**Ramas:** `feature/practica-retroalimentacion-idempotencia` en backend y frontend<br>
**Fecha:** noviembre de 2026<br>
**Estado:** implementado y verificado en la rama; pendiente de PR y merge a `develop`

Este documento describe el trabajo de las tareas 110 y 114: qué se construyó, por qué, cómo se comporta la API, qué archivos cambiaron, cómo se probó y qué coordinaciones quedan pendientes. El modelo de práctica, el motor adaptativo y los niveles se explican en [`motor-practica-adaptativa.md`](motor-practica-adaptativa.md).

---

## 1. Resumen

| Tarea / subtarea | Resultado |
|---|---|
| **110 — Retroalimentación inmediata** | El resultado del intento incluye pista, explicación o error común en la misma respuesta del servidor |
| **111 — Retroalimentación en la respuesta del intento** | `FeedbackService` selecciona el texto según el número de intento y si acertó |
| **112 — Panel de retroalimentación** | Componente `PanelRetroalimentacion` bajo el ejercicio, con MathText, `aria-live`, foco y aviso de último intento |
| **113 — Pistas progresivas** | Botón “Ver pista” antes de responder y flujo de segundo intento con clave de idempotencia nueva |
| **114 — Registro con trazabilidad completa** | Cada intento conserva estudiante, ejercicio, resultado, fecha y tiempo (CA-08) |
| **115 — Servicio de registro transaccional** | El intento y la actualización de nivel se guardan en la misma transacción; una prueba verifica el rollback |
| **116 — Protección contra envíos duplicados** | Clave UUID por intento, restricción única y bloqueo del usuario para envíos simultáneos |

**Verificación:** `.\mvnw.cmd verify` → **249 pruebas, 0 fallos**; frontend `npm test` → **98 pruebas**, `npm run lint` y `npm run build` sin errores.

---

## 2. Alcance y relación con otras historias

- Se apoya en la interfaz de práctica de **#104** y en el motor adaptativo de **#90/#95**.
- No cambia el modelo de tablas: usa `intento_practica`, `ejercicio`, `opcion_ejercicio` y `nivel_estudiante` de las migraciones `V5` y `V6`.
- El **historial de resultados (#128)** consume los campos que esta tarea persiste; no necesita cambios en la tabla.
- La prueba de concurrencia de **#149** puede reutilizar el caso de envíos simultáneos que se agregó aquí.
- El CRUD docente de contenidos (#132) no forma parte de este alcance.

---

## 3. Retroalimentación inmediata (#110)

### 3.1 Flujo

1. El estudiante responde un ejercicio desde `POST /api/practica/intentos`.
2. El servidor calcula si la respuesta es correcta y el número de intento (1 o 2).
3. `FeedbackService` elige el texto de retroalimentación y lo devuelve dentro de `retroalimentacion`, en la misma respuesta HTTP.
4. La pantalla muestra el panel debajo del enunciado, sin taparlo, y mueve el foco hacia él para que un lector de pantalla lo anuncie.

### 3.2 Casos de retroalimentación

| Situación | `tipo` | `texto` | `errorComun` |
|---|---|---|---|
| Primer acierto | `EXPLICACION` | Explicación del ejercicio | `null` |
| Primer fallo | `PISTA` | Pista del ejercicio | Sí |
| Segundo acierto | `EXPLICACION` | Explicación del ejercicio | `null` |
| Segundo fallo | `EXPLICACION` | Explicación completa | Sí |

El segundo fallo ya no permite otro intento: la respuesta trae `puedeReintentar: false`.

### 3.3 Backend

| Archivo | Qué hace |
|---|---|
| `modulos/practica/service/FeedbackService.java` | Selecciona pista, explicación y error común según `numeroIntento` y `correcto` |
| `modulos/practica/dto/RetroalimentacionResponse.java` | Contrato `{ tipo, texto, errorComun }` |
| `modulos/practica/dto/ResultadoIntentoResponse.java` | Incluye `numeroIntento`, `esCorrecta`, `puedeReintentar`, `retroalimentacion` y `nivel` |
| `modulos/practica/service/PracticaService.java` | Valida la respuesta, calcula el resultado y arma la respuesta final |

La respuesta correcta nunca se envía antes de responder: `GET /api/practica/siguiente` no incluye `esCorrecta`, `respuestaNumerica` ni `explicacion`.

### 3.4 Frontend

| Archivo | Qué hace |
|---|---|
| `src/features/practica/PanelRetroalimentacion.jsx` | Panel reutilizable con pista o explicación, error común y aviso de último intento |
| `src/features/practica/PracticaPage.jsx` | Integra el panel, el botón “Ver pista” y el flujo de reintento |
| `src/features/practica/PanelRetroalimentacion.test.jsx` | Pruebas de pista, explicación, acierto, foco y ausencia de feedback |
| `src/features/practica/PracticaPage.test.jsx` | Flujo completo: pista, primer fallo, segundo fallo, reintento y siguiente ejercicio |

Decisiones de interfaz:

- El texto siempre viene del backend; el frontend no inventa ni duplica la retroalimentación.
- “Ver pista” revela la pista antes de responder y envía `pistaUsada: true`.
- El panel usa `role="status"`, `aria-live="polite"` y `aria-atomic="true"`.
- Al aparecer, el panel recibe el foco; al pulsar “Intentar de nuevo”, el foco vuelve al enunciado.
- La notación matemática se renderiza con `MathText` (KaTeX), también en el error común.

### 3.5 Criterios de aceptación

- [x] Primer fallo: pista y error común.
- [x] Segundo fallo: explicación completa.
- [x] Acierto: explicación breve.
- [x] La retroalimentación llega en la misma respuesta del intento.
- [x] El panel se ve bajo el ejercicio sin tapar el enunciado.
- [x] El segundo intento queda registrado.

---

## 4. Registro y trazabilidad de intentos (#114)

### 4.1 Datos que guarda cada intento

La tabla `intento_practica` conserva, como mínimo, los cinco datos de CA-08:

| Dato | Columna |
|---|---|
| Estudiante | `usuario_id` |
| Ejercicio | `ejercicio_id` |
| Resultado | `es_correcta` |
| Fecha | `creado_en` |
| Tiempo empleado | `tiempo_seg` |

Además guarda `numero_intento`, `pista_usada`, la opción o respuesta numérica, el snapshot de competencia y nivel, y la clave de idempotencia. La prueba `PracticaControllerTest.cadaIntentoQuedaConsultableConLosCincoDatosDeTrazabilidad` verifica esos campos contra la base.

### 4.2 Idempotencia

El frontend genera un UUID por intento (`claveIdempotencia`) y lo conserva si la petición falla. Así, un reintento de red reenvía la misma clave.

En el backend:

1. Se valida el ejercicio y la respuesta.
2. Se bloquea la fila del usuario con `UsuarioRepository.findParaBloquear` (`SELECT … FOR UPDATE`), dentro de la transacción.
3. Se busca un intento existente con `(usuario_id, clave_idempotencia)`.
4. Si existe, se devuelve **200** con el mismo `intentoId`.
5. Si no existe, se inserta y se devuelve **201**.

La restricción `uq_intento_practica_idempotencia` respalda la regla en la base. El bloqueo del usuario hace que dos envíos simultáneos se atiendan en serie, en lugar de competir y provocar un error de restricción única.

El segundo intento pedagógico usa una clave nueva; el frontend la descarta después de una respuesta confirmada. Esto permite que el segundo intento se registre como un intento distinto.

### 4.3 Transacción

`PracticaService.responder` está anotado con `@Transactional`. En el primer intento, dentro de esa misma transacción:

- se guarda `intento_practica`;
- se actualiza `nivel_estudiante` y su historial.

Si falla la actualización de nivel, el intento también se revierte. La prueba `PracticaTransaccionTest.siFallaLaActualizacionDeNivelElIntentoNoQuedaRegistrado` lo comprueba reemplazando `NivelEstudianteService` por un mock que lanza una excepción.

### 4.4 Concurrencia

`PracticaIdempotenciaConcurrenteTest` lanza **8 envíos simultáneos** con la misma clave y verifica que:

- todos reciben el mismo `intentoId`;
- exactamente uno tiene `creado = true`;
- en la base queda **un solo registro**.

### 4.5 Criterios de aceptación

- [x] Cada intento queda consultable con estudiante, ejercicio, resultado, fecha y tiempo.
- [x] Un doble envío idéntico no crea dos registros.
- [x] El registro del intento y la actualización de nivel son transaccionales.
- [x] Los envíos simultáneos con la misma clave no duplican el intento.

---

## 5. Contrato de API

### 5.1 `GET /api/practica/siguiente`

Parámetro obligatorio: `asignatura=CALCULO_DIFERENCIAL` o `CALCULO_INTEGRAL`.

Devuelve el ejercicio sin la respuesta correcta ni la explicación. Incluye `id`, `codigo`, `enunciado`, `tipo`, `nivel`, `competencia`, `opciones` y `pista`.

### 5.2 `POST /api/practica/intentos`

Cuerpo para opción múltiple:

```json
{
  "ejercicioId": 1,
  "opcionId": 4,
  "tiempoSeg": 42,
  "pistaUsada": false,
  "claveIdempotencia": "550e8400-e29b-41d4-a716-446655440000"
}
```

Para un ejercicio `NUMERICA` se envía `respuestaNumerica` en lugar de `opcionId`.

Respuesta **201** (nuevo) o **200** (repetición idempotente):

```json
{
  "intentoId": 501,
  "numeroIntento": 1,
  "esCorrecta": false,
  "puedeReintentar": true,
  "retroalimentacion": {
    "tipo": "PISTA",
    "texto": "Suma primero los terminos semejantes.",
    "errorComun": "Sumar coeficientes y conservar mal el termino constante."
  },
  "nivel": {
    "competenciaCodigo": "ALGEBRAICA",
    "nivel": 1,
    "rachaAciertos": 0,
    "rachaErrores": 1
  }
}
```

Errores principales:

| Código | Caso |
|---|---|
| 400 | Cuerpo inválido, falta la clave o se envían dos respuestas |
| 401 | Sin token o token inválido |
| 403 | Rol distinto de `ESTUDIANTE` |
| 404 | Ejercicio inexistente o inactivo |
| 409 | Tercer intento o segundo intento después de un primer acierto |
| 422 | La opción no pertenece al ejercicio |

El contrato completo y las reglas del motor están en [`motor-practica-adaptativa.md`](motor-practica-adaptativa.md) y en el README §13.

---

## 6. Cambio de la migración de grupos

La migración de grupos pasó de:

```text
V202610242250__crear_grupos_docentes_estudiantes.sql
```

a:

```text
V7__crear_grupos_docentes_estudiantes.sql
```

El cambio busca que las migraciones sigan la numeración consecutiva (`V1` … `V7`); la siguiente será `V8`. No cambia el contenido SQL ni el esquema.

Consecuencias:

- `src/test/java/.../esquema/EsquemaBaseDatosTest.java` ahora espera `V7__crear_grupos_docentes_estudiantes.sql`.
- Una base local creada antes del cambio conserva en `flyway_schema_history` la versión vieja y Flyway falla con “Detected applied migration not resolved locally”. Como son datos de prueba, se recrea:
  ```bash
  docker compose down
  # borrar la carpeta aprendizaje-usco-backend/postgres_data/
  ./mvnw spring-boot:run
  ```
- Si quedaron migraciones viejas compiladas en `target/`, ejecutar `./mvnw clean test`.
- **No se debe renombrar una migración ya aplicada en staging o producción.** En este proyecto el cambio se hizo antes de tener staging; si una base compartida ya la aplicó, hay que reparar su historial o recrearla.

---

## 7. Archivos modificados

### 7.1 Backend

| Archivo | Cambio |
|---|---|
| `src/main/java/.../modulos/practica/service/PracticaService.java` | Bloqueo del usuario antes de comprobar la idempotencia y registrar el intento |
| `src/main/java/.../modulos/usuario/repository/UsuarioRepository.java` | `findParaBloquear` con `PESSIMISTIC_WRITE` |
| `src/main/resources/db/migration/V7__crear_grupos_docentes_estudiantes.sql` | Renombrada desde `V202610242250…` |
| `src/test/java/.../modulos/practica/controller/PracticaControllerTest.java` | Segundo fallo, cinco datos de trazabilidad y cuerpo con tiempo |
| `src/test/java/.../modulos/practica/service/PracticaIdempotenciaConcurrenteTest.java` | Ocho envíos simultáneos con la misma clave |
| `src/test/java/.../modulos/practica/service/PracticaTransaccionTest.java` | Rollback si falla la actualización de nivel |
| `src/test/java/.../esquema/EsquemaBaseDatosTest.java` | Lista de migraciones con `V7` |
| `docs/README-backend.md` | Reglas de trazabilidad e idempotencia; numeración consecutiva de migraciones |
| `docs/implementacion.md` | Sección de retroalimentación y registro de intentos |
| `docs/code/motor-practica-adaptativa.md` | Referencia a las tareas 110/114 |
| `docs/code/retroalimentacion-e-idempotencia.md` | Este documento |

### 7.2 Frontend

| Archivo | Cambio |
|---|---|
| `src/features/practica/PanelRetroalimentacion.jsx` | Componente nuevo del panel |
| `src/features/practica/PracticaPage.jsx` | Integración del panel, “Ver pista” y foco al reintentar |
| `src/features/practica/PanelRetroalimentacion.test.jsx` | Pruebas del panel |
| `src/features/practica/PracticaPage.test.jsx` | Pruebas de segundo fallo, pista, foco e idempotencia |
| `docs/implementacion.md` | Sección de práctica y retroalimentación |

---

## 8. Pruebas

### 8.1 Backend

| Prueba | Qué cubre |
|---|---|
| `FeedbackServiceTest` | Pista en el primer fallo, explicación en el segundo y explicación en el acierto |
| `PracticaControllerTest` | Endpoints, roles, pista, segundo fallo, segundo intento, misma clave y cinco datos |
| `PracticaIdempotenciaConcurrenteTest` | Envíos simultáneos con la misma clave |
| `PracticaTransaccionTest` | Rollback del intento si falla el nivel |

Comando:

```bash
./mvnw verify
# Windows
.\mvnw.cmd verify
```

Resultado esperado: **249 pruebas, 0 fallos, 0 errores**. Requiere Docker Desktop para Testcontainers.

### 8.2 Frontend

```bash
npm test
npm run lint
npm run build
```

Resultado esperado: **98 pruebas**, lint y build sin errores.

Las pruebas del panel comprueban la etiqueta “Pista”/“Explicación”, el error común, el foco, el aviso de último intento y que no se muestre nada si el backend no envía retroalimentación. Las de la página comprueban el segundo fallo, el uso de una clave nueva para el segundo intento y que el reintento de red conserve la misma clave.

---

## 9. Verificación manual

1. Levantar backend con perfil `dev` y frontend con `npm run dev`.
2. Entrar con `estudiante@aprendizaje.test` / `Estudiante2026*`.
3. Abrir **Práctica**.
4. Pulsar **Ver pista**: debe verse la pista y el intento debe llevar `pistaUsada: true`.
5. Responder mal: debe aparecer **“Pista:”** y el error frecuente.
6. Pulsar **Intentar de nuevo** y fallar otra vez: debe aparecer **“Explicación:”** y “Este fue el último intento para este ejercicio”.
7. Con Swagger, enviar dos veces el mismo cuerpo con la misma `claveIdempotencia`: la primera responde **201** y la segunda **200** con el mismo `intentoId`.
8. Cambiar la clave y volver a fallar: debe llegar `numeroIntento: 2`, `retroalimentacion.tipo: "EXPLICACION"` y `puedeReintentar: false`.

---

## 10. Coordinación y pendientes

- **Historial (#128 / SCRUM-41):** su rama `feature/historial` modifica `EsquemaBaseDatosTest` esperando `V202610242250…`. Al rebasar sobre esta rama debe cambiar esa línea a `V7__crear_grupos_docentes_estudiantes.sql`. También agrega una sección 4 en `docs/implementacion.md`; hay que renumerar una de las dos secciones.
- **Accesibilidad (#52/SCRUM-52):** su rama `feature/accesibilidad` agrega un `resultadoRef` y un efecto de foco en `PracticaPage.jsx`. El panel nuevo ya mueve el foco al resultado y la página ya devuelve el foco al enunciado al reintentar; al rebasar, conservar los cambios de tema y quitar el `resultadoRef` duplicado.
- **Documentación de entrega (SCRUM-53):** `docs/base-de-datos/modelo-fisico.md` todavía menciona `V202610242250__crear_grupos_docentes_estudiantes.sql`; debe actualizarse a `V7`. El README de esa rama habla de migraciones base `V1` a `V6`; debe decir `V1` a `V7` y mantener la numeración consecutiva.
- **Staging:** si una base compartida ya aplicó la migración con el nombre viejo, no se debe renombrar; hay que reparar el historial o recrear la base antes de desplegar.
- **Estado de la rama:** los cambios están verificados, pero aún no se han fusionado a `develop`.

---

## 11. Cómo revisar rápido

1. Leer este documento.
2. Revisar `docs/README-backend.md` §13 para el contrato y las reglas de la API.
3. Revisar [`motor-practica-adaptativa.md`](motor-practica-adaptativa.md) para el modelo y el motor.
4. Abrir `PracticaService.java`, `FeedbackService.java` y `PanelRetroalimentacion.jsx`.
5. Ejecutar `.\mvnw.cmd verify` y, en el frontend, `npm test`, `npm run lint` y `npm run build`.
