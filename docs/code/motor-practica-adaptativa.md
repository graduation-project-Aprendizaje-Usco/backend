# Motor de práctica adaptativa — contexto y registro de implementación

**Proyecto:** Aprendizaje USCO  
**Épica:** EPIC-3 — Práctica adaptativa y retroalimentación  
**Tareas:** 82, 87, 90 y 95  
**Rama de trabajo:** `feature/motor-practica-adaptativa`  
**Fecha de implementación:** octubre de 2026

Este documento reúne en un solo lugar el contexto técnico, las decisiones de diseño, los archivos creados o modificados, los contratos API, las reglas del motor, las pruebas y el estado de la implementación. Su objetivo es que cualquier integrante o evaluador pueda revisar qué se hizo y dónde encontrarlo.

---

## 1. Resumen de la implementación

Se implementó el módulo backend de práctica adaptativa sobre la base existente de autenticación, roles, competencias y diagnóstico.

| Tarea | Resultado |
|---|---|
| **82 — Banco de ejercicios** | 90 ejercicios semilla: 45 de Cálculo Diferencial y 45 de Cálculo Integral, 5 por asignatura × competencia × nivel |
| **87 — Modelo de datos** | Migraciones, entidades, repositorios y restricciones para contenido, ejercicio, opciones, intentos, niveles e historial |
| **90 — Motor adaptativo** | Servicio determinista y endpoint `GET /api/practica/siguiente`, con filtro obligatorio por asignatura |
| **95 — Niveles** | Nivel inicial desde diagnóstico, reglas N=3/M=3, límites 1–3, evento de inicialización y respaldo lazy |

También se implementó el registro de intentos y la retroalimentación inmediata necesarios para que el motor y los niveles funcionen de punta a punta. El CRUD docente de #132/#133/#134 **no** se implementó en esta fase; el modelo queda preparado para esas historias.

**Estado de validación:** `.\mvnw.cmd verify` termina en `BUILD SUCCESS` con **199 pruebas, 0 fallos y 0 errores**. Requiere Docker Desktop para Testcontainers.

---

## 2. Arquitectura real del proyecto

- Backend: Java 21, Spring Boot 4.1.1, Maven Wrapper.
- Persistencia: PostgreSQL 17, Spring Data JPA, Flyway, `ddl-auto=validate`.
- Seguridad: Spring Security, JWT HS256, `@PreAuthorize` por rol.
- Pruebas: JUnit 5, MockMvc, Testcontainers, AssertJ, Mockito.
- Frontend: React 19, Vite, MUI, Axios, KaTeX. **No se modificó el frontend en estas tareas.**
- Organización backend por módulos funcionales y capas `controller/service/repository/dto/model`.

La documentación existente del proyecto sigue siendo la referencia para autenticación, diagnóstico y convenciones generales:

- `docs/README-backend.md`
- `docs/CONTRIBUTING.md`
- `docs/base-de-datos/modelo-fisico.md`
- `docs/tareas-y-responsables.md`

---

## 3. Tarea 82 — Banco de ejercicios

### 3.1 Composición de la semilla

| Asignatura | ALGEBRAICA | LOGICA | FUNCIONAL | Total |
|---|---:|---:|---:|---:|
| `CALCULO_DIFERENCIAL` | 15 | 15 | 15 | 45 |
| `CALCULO_INTEGRAL` | 15 | 15 | 15 | 45 |
| **Total** | **30** | **30** | **30** | **90** |

Cada combinación asignatura × competencia × nivel tiene exactamente **5 ejercicios**.

La semilla contiene 78 ejercicios de opción múltiple y 12 numéricos.

### 3.2 Fuente revisable

`docs/datos/ejercicios_practica.csv` es la fuente de la semilla. Columnas:

```text
codigo;asignatura;tema;dimension;nivel;tipo;enunciado;opcion_a;opcion_b;opcion_c;opcion_d;respuesta;tolerancia;pista;explicacion;error_comun;activo
```

Reglas aplicadas:

- `codigo` único y estable.
- `asignatura`: `CALCULO_DIFERENCIAL` o `CALCULO_INTEGRAL`.
- `dimension`: `ALGEBRAICA`, `LOGICA` o `FUNCIONAL`.
- `nivel`: 1, 2 o 3.
- `tipo`: `OPCION_MULTIPLE` o `NUMERICA`.
- Opción múltiple: cuatro opciones y una única letra correcta A–D.
- Numérica: opciones vacías, respuesta numérica y tolerancia positiva.
- Enunciado, pista y explicación obligatorios.
- Notación matemática entre `$...$` para `MathText`/KaTeX.

La migración `V202610221010__datos_ejercicios_semilla.sql` se genera a partir de ese CSV. Una prueba de integración compara el CSV con la base.

### 3.3 Criterios de dificultad

| Nivel | Criterio |
|---:|---|
| 1 | Un concepto o un paso; aplicación directa |
| 2 | Dos o tres pasos; elección de procedimiento o representación |
| 3 | Varios pasos; interpretación, transferencia o justificación |

La dificultad no se mide por la longitud del enunciado. La revisión pedagógica final debe hacerla un docente.

### 3.4 Contenido semilla vs. contenido docente

- Semilla: `creado_por IS NULL`.
- Creado por docente: `creado_por` con el usuario autenticado.
- La semilla se carga por Flyway.
- Los ejercicios nuevos se crean por la aplicación; **no requieren una migración por ejercicio**.
- Un ejercicio con intentos no se elimina: se desactiva.
- Si tiene intentos, se rechazan cambios de tipo, respuesta correcta, opciones o competencia. Para hacerlo se crea un ejercicio nuevo y se desactiva el anterior.
- Se pueden editar enunciado, pista, explicación, error común y nivel, conservando los snapshots del intento.

La administración docente pertenece a #132/#133/#134 y no forma parte de estas cuatro tareas.

---

## 4. Tarea 87 — Modelo de datos

### 4.1 Migraciones

| Migración | Contenido |
|---|---|
| `V202610221000__esquema_practica.sql` | Tablas, restricciones e índices de práctica |
| `V202610221010__datos_ejercicios_semilla.sql` | 90 ejercicios semilla y sus opciones |

No se editaron `V1`–`V4`. La migración nueva solo crea tablas; no hay operaciones destructivas ni cambios a datos existentes.

### 4.2 Tablas

#### `contenido`

Agrupa ejercicios por asignatura, tema y competencia.

| Columna | Tipo | Regla |
|---|---|---|
| `id` | `BIGINT IDENTITY` | PK |
| `competencia_id` | `BIGINT` | FK → `competencia` |
| `asignatura` | `VARCHAR(80)` | `CALCULO_DIFERENCIAL` o `CALCULO_INTEGRAL` |
| `tema` | `VARCHAR(120)` | Texto validado; puede crecer sin cambio de esquema |
| `titulo` | `VARCHAR(150)` | `UNIQUE (asignatura, tema, titulo)` |
| `descripcion` | `VARCHAR(500)` | Nula |
| `activo` | `BOOLEAN` | Default `TRUE` |
| `creado_por` | `BIGINT` | FK → `usuario`; `NULL` en semilla |
| `creado_en` / `actualizado_en` | `TIMESTAMPTZ` | Auditoría |

#### `ejercicio`

| Columna | Tipo | Regla |
|---|---|---|
| `id` | `BIGINT IDENTITY` | PK |
| `contenido_id` | `BIGINT` | FK compuesta con competencia |
| `competencia_id` | `BIGINT` | FK → `contenido(id, competencia_id)` |
| `codigo` | `VARCHAR(40)` | UNIQUE |
| `nivel` | `SMALLINT` | CHECK 1–3 |
| `tipo` | `VARCHAR(20)` | `OPCION_MULTIPLE` o `NUMERICA` |
| `enunciado` | `VARCHAR(2000)` | No vacío |
| `respuesta_numerica` | `NUMERIC(20,6)` | Solo tipo numérico |
| `tolerancia` | `NUMERIC(20,6)` | Solo tipo numérico; `> 0` |
| `pista` / `explicacion` | `VARCHAR(1000/2000)` | Retroalimentación |
| `error_comun` | `VARCHAR(1000)` | Nulo |
| `activo` | `BOOLEAN` | Default `TRUE` |
| `creado_por` / `actualizado_por` | `BIGINT` | FK → `usuario` |
| `creado_en` / `actualizado_en` | `TIMESTAMPTZ` | Auditoría |

La FK compuesta impide que un ejercicio quede asociado a una competencia distinta de la de su contenido.

#### `opcion_ejercicio`

- `id`, `ejercicio_id`, `letra` A–D, `texto`, `es_correcta`.
- `UNIQUE (ejercicio_id, letra)`.
- `UNIQUE (id, ejercicio_id)` para la FK compuesta del intento.
- Índice parcial único: una sola opción correcta por ejercicio.

#### `intento_practica`

| Columna | Tipo | Regla |
|---|---|---|
| `id` | `BIGINT IDENTITY` | PK |
| `usuario_id` | `BIGINT` | FK → `usuario` |
| `ejercicio_id` | `BIGINT` | FK → `ejercicio` |
| `competencia_id` | `BIGINT` | Snapshot histórico |
| `nivel_ejercicio` | `SMALLINT` | Snapshot del nivel |
| `numero_intento` | `SMALLINT` | 1 o 2 |
| `opcion_ejercicio_id` | `BIGINT` | Nulo si es numérica |
| `respuesta_numerica` | `NUMERIC(20,6)` | Nulo si es opción múltiple |
| `es_correcta` | `BOOLEAN` | Resultado calculado |
| `tiempo_seg` | `INTEGER` | 0–86400 |
| `pista_usada` | `BOOLEAN` | Default `FALSE` |
| `clave_idempotencia` | `UUID` | `UNIQUE (usuario_id, clave)` |
| `creado_en` | `TIMESTAMPTZ` | Fecha del intento |

Restricciones adicionales:

- Exactamente una respuesta: opción o valor numérico.
- La opción debe pertenecer al ejercicio mediante FK compuesta.
- `UNIQUE (usuario_id, ejercicio_id, numero_intento)`.
- No se borran intentos.
- No se guarda el texto de la retroalimentación en el intento; el MVP la deriva del ejercicio.

#### `nivel_estudiante`

- PK compuesta `(usuario_id, competencia_id)`.
- `nivel` 1–3.
- `racha_aciertos` y `racha_errores` (`SMALLINT >= 0`).
- `actualizado_en`.
- Se bloquea la fila al actualizar para evitar carreras.

#### `historial_nivel`

- Registra inicialización y cambios reales de nivel.
- Motivos: `INICIALIZACION`, `DIAGNOSTICO`, `RACHA_ACIERTOS`, `RACHA_ERRORES`, `AJUSTE_MANUAL`.
- Referencias opcionales a `intento_practica` e `intento_diagnostico`.
- Solo inserción.

### 4.3 Retroalimentación

No se creó una tabla `retroalimentacion` en el MVP. La retroalimentación es contenido del ejercicio:

- `pista`
- `explicacion`
- `error_comun`

El backend selecciona el texto según el número de intento. Si más adelante se requiere auditoría del feedback mostrado, se puede agregar una tabla o columna nullable sin rediseñar el ejercicio.

---

## 5. Tarea 95 — Niveles de dificultad

### 5.1 Nivel inicial

| Diagnóstico por competencia | Nivel de práctica |
|---|---|
| `BAJO` | 1 |
| `MEDIO` | 2 |
| `ALTO` | 3 |
| Sin diagnóstico | 1 |

Se usa `resultado_dimension.nivel`, no la categoría global del intento.

La inicialización es idempotente: si existe `nivel_estudiante`, no se sobrescribe.

### 5.2 Reglas

Configuración en `application.properties`:

```properties
app.practica.niveles.aciertos-para-subir=3
app.practica.niveles.errores-para-bajar=3
app.practica.niveles.nivel-inicial-sin-diagnostico=1
```

- Solo el primer intento de cada ejercicio cuenta para racha y nivel.
- Acierto: incrementa `racha_aciertos` y reinicia `racha_errores`.
- Error: incrementa `racha_errores` y reinicia `racha_aciertos`.
- Tres aciertos seguidos suben un nivel, salvo en nivel 3.
- Tres errores seguidos bajan un nivel, salvo en nivel 1.
- Al cambiar de nivel se reinician ambas rachas.
- En los límites no se sale de 1–3.
- El segundo intento se registra, pero no actualiza nivel ni racha.
- Un segundo acierto tras feedback no cuenta como dominio para el motor.

### 5.3 Inicialización desde el diagnóstico

`DiagnosticoService` publica `DiagnosticoFinalizadoEvent` al cerrar un intento. `NivelInicialListener` escucha después del commit y crea los niveles que falten. Si falla, el motor hace inicialización lazy en la primera práctica.

La modificación a `DiagnosticoService` fue mínima: inyectar `ApplicationEventPublisher` y publicar el evento después de guardar el intento y sus resultados.

### 5.4 Diferencia entre niveles

- `ejercicio.nivel`: dificultad del contenido.
- `nivel_estudiante.nivel`: dificultad que el motor ofrece.
- `resultado_dimension.nivel`: nivel de dominio del diagnóstico.
- `intento_diagnostico.categoria`: categoría global de riesgo.

No son intercambiables.

---

## 6. Tarea 90 — Motor de selección adaptativa

### 6.1 Principios

- Basado en reglas, determinista y explicable.
- Sin IA ni aprendizaje automático.
- Solo usa el historial del estudiante autenticado.
- Nunca mezcla asignaturas.
- No repite ejercicios acertados en el primer intento mientras haya alternativas.
- No expone la respuesta correcta ni la explicación antes de responder.

### 6.2 Entrada

- `asignatura` obligatoria.
- Nivel actual por competencia.
- Último resultado del diagnóstico por competencia.
- Últimos 10 intentos de práctica con `numero_intento = 1`.
- Ejercicios acertados en el primer intento.
- Ejercicios vistos en la ventana reciente.
- Candidatos activos de la asignatura.

### 6.3 Prioridad y score

```text
prioridad = 3 si diagnóstico BAJO o precisión reciente < 50 %
prioridad = 2 si diagnóstico MEDIO o precisión reciente < 70 %
prioridad = 1 en otro caso
```

Dentro de la misma prioridad:

```text
score = 0.45 * necesidadDiagnostico
      + 0.35 * (1 - precisionRecienteSuavizada)
      + 0.10 * nivelRelativo
      + 0.10 * rezago
```

- `precisionRecienteSuavizada = (aciertos + 1) / (intentos + 2)`.
- `nivelRelativo = (3 - nivel) / 2`.
- `rezago = 1` si nunca practicó; si practicó, `min(días desde el último intento / 7, 1)`.

Desempates: mayor prioridad, mayor score, menos intentos recientes, menor id de competencia.

### 6.4 Selección del ejercicio

1. Filtrar candidatos activos de la competencia elegida.
2. Preferir los no acertados en el primer intento.
3. Preferir los no vistos en los últimos 10 intentos.
4. Preferir el nivel actual; si no hay, probar nivel adyacente.
5. Si no hay de ese nivel, usar cualquier ejercicio activo de la competencia.
6. Ordenar por cercanía al nivel, no acertado, no reciente e id ascendente.
7. Si la competencia no tiene ejercicios, pasar a la siguiente.
8. Si ninguna tiene, responder `404`.

### 6.5 Segundo intento

- Solo se permite si el primer intento fue incorrecto.
- El servidor deriva `numero_intento`; no lo envía el cliente.
- El segundo intento no actualiza el nivel.
- Un acierto en el segundo intento no marca el ejercicio como dominado para el motor.

### 6.6 Trazabilidad

El servicio devuelve un motivo explicable y un mapa de scores que se usa para logging. No se guarda una tabla de selección en el MVP.

---

## 7. Contratos API

Todos los endpoints requieren `Authorization: Bearer <token>` y rol `ESTUDIANTE`.

### 7.1 `GET /api/practica/siguiente`

Parámetro obligatorio:

- `asignatura`: `CALCULO_DIFERENCIAL` o `CALCULO_INTEGRAL`.

Ejemplo:

```http
GET /api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL
Authorization: Bearer <token>
```

Respuesta 200:

```json
{
  "id": 1,
  "codigo": "DIF-ALG-1-01",
  "enunciado": "Simplifica $2x+3x-4$.",
  "tipo": "OPCION_MULTIPLE",
  "nivel": 1,
  "competencia": {
    "codigo": "ALGEBRAICA",
    "nombre": "Competencia algebraica"
  },
  "opciones": [
    { "id": 1, "letra": "A", "texto": "$5x-4$" }
  ],
  "pista": "Suma primero los terminos semejantes."
}
```

Errores:

| Código | Caso |
|---:|---|
| 400 | Falta `asignatura` o no es válida |
| 401 | Sin token o token inválido |
| 403 | Rol distinto de `ESTUDIANTE` |
| 404 | No hay ejercicios activos de esa asignatura |

Nunca incluye `esCorrecta`, `respuestaNumerica` ni `explicacion`.

### 7.2 `POST /api/practica/intentos`

Cuerpo para opción múltiple:

```json
{
  "ejercicioId": 1,
  "opcionId": 1,
  "tiempoSeg": 45,
  "pistaUsada": false,
  "claveIdempotencia": "550e8400-e29b-41d4-a716-446655440000"
}
```

Para un ejercicio `NUMERICA` se envía `respuestaNumerica` en lugar de `opcionId`.

Respuesta 201 (primer registro) o 200 (repetición idempotente):

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

Feedback:

| Situación | Feedback |
|---|---|
| Primer acierto | Explicación |
| Primer fallo | Pista + error común |
| Segundo acierto | Explicación; no cuenta para nivel |
| Segundo fallo | Explicación completa; sin más reintentos |

Errores:

| Código | Caso |
|---:|---|
| 400 | Cuerpo inválido o combinación de respuesta incorrecta |
| 401 | Sin token o token inválido |
| 403 | Rol distinto de `ESTUDIANTE` |
| 404 | Ejercicio inexistente o inactivo |
| 409 | Tercer intento, o segundo intento tras un primer acierto |
| 422 | La opción no pertenece al ejercicio |

### 7.3 Peticiones manuales

Con el backend en perfil `dev`:

```http
@base = http://localhost:8080

POST {{base}}/api/auth/login
Content-Type: application/json

{ "correo": "estudiante@aprendizaje.test", "password": "Estudiante2026*" }

GET {{base}}/api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL
Authorization: Bearer <token>

POST {{base}}/api/practica/intentos
Authorization: Bearer <token>
Content-Type: application/json

{
  "ejercicioId": 1,
  "opcionId": 1,
  "tiempoSeg": 30,
  "pistaUsada": false,
  "claveIdempotencia": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 8. Configuración

En `src/main/resources/application.properties`:

```properties
app.practica.asignaturas=CALCULO_DIFERENCIAL,CALCULO_INTEGRAL
app.practica.niveles.aciertos-para-subir=3
app.practica.niveles.errores-para-bajar=3
app.practica.niveles.nivel-inicial-sin-diagnostico=1
```

Propiedades del diagnóstico existentes:

- `app.diagnostico.gracia-segundos`
- `app.diagnostico.barrido.*`
- `app.diagnostico.umbrales.global.*`
- `app.diagnostico.umbrales.dimension.*`

---

## 9. Archivos creados y modificados

### 9.1 Migraciones

- `src/main/resources/db/migration/V202610221000__esquema_practica.sql`
- `src/main/resources/db/migration/V202610221010__datos_ejercicios_semilla.sql`

### 9.2 Contenido

- `src/main/java/.../modulos/contenido/model/Contenido.java`
- `.../contenido/model/Ejercicio.java`
- `.../contenido/model/OpcionEjercicio.java`
- `.../contenido/model/NivelDificultad.java`
- `.../contenido/model/NivelDificultadConverter.java`
- `.../contenido/model/TipoEjercicio.java`
- `.../contenido/repository/ContenidoRepository.java`
- `.../contenido/repository/EjercicioRepository.java`
- `.../contenido/dto/CompetenciaResponse.java`

### 9.3 Práctica

- `.../practica/model/IntentoPractica.java`
- `.../practica/model/NivelEstudiante.java`
- `.../practica/model/NivelEstudianteId.java`
- `.../practica/model/HistorialNivel.java`
- `.../practica/model/MotivoCambioNivel.java`
- `.../practica/repository/IntentoPracticaRepository.java`
- `.../practica/repository/NivelEstudianteRepository.java`
- `.../practica/repository/HistorialNivelRepository.java`
- `.../practica/dto/EjercicioPracticaResponse.java`
- `.../practica/dto/OpcionPracticaResponse.java`
- `.../practica/dto/IntentoPracticaRequest.java`
- `.../practica/dto/ResultadoIntentoResponse.java`
- `.../practica/dto/RetroalimentacionResponse.java`
- `.../practica/dto/NivelResponse.java`
- `.../practica/config/PracticaConfig.java`
- `.../practica/config/PracticaProperties.java`
- `.../practica/config/ReglasNivelProperties.java`
- `.../practica/service/AsignaturaValidator.java`
- `.../practica/service/SeleccionEjercicioService.java`
- `.../practica/service/NivelService.java`
- `.../practica/service/NivelEstudianteService.java`
- `.../practica/service/FeedbackService.java`
- `.../practica/service/PracticaService.java`
- `.../practica/controller/PracticaController.java`
- `.../practica/event/NivelInicialListener.java`
- `.../diagnostico/event/DiagnosticoFinalizadoEvent.java`
- `.../common/exception/SolicitudInvalidaException.java`

### 9.4 Archivos existentes modificados

- `.../diagnostico/service/DiagnosticoService.java` — publica `DiagnosticoFinalizadoEvent`.
- `.../diagnostico/repository/ResultadoDimensionRepository.java` — consulta los resultados finalizados del estudiante.
- `src/main/resources/application.properties` — configuración de práctica.
- `docs/README-backend.md` — documentación de la API de práctica.
- `docs/base-de-datos/modelo-fisico.md` — tablas y decisiones del modelo.
- `src/test/java/.../esquema/EsquemaBaseDatosTest.java` — lista de migraciones actualizada.

### 9.5 Datos y documentación

- `docs/datos/ejercicios_practica.csv` — 90 ejercicios semilla.
- `docs/code/motor-practica-adaptativa.md` — este documento.

---

## 10. Pruebas

### 10.1 Pruebas nuevas

| Clase | Propósito |
|---|---|
| `NivelServiceTest` | Subida/bajada, límites y rachas |
| `SeleccionEjercicioServiceTest` | Prioridad, repetición, fallback y determinismo |
| `FeedbackServiceTest` | Pista, explicación y acierto |
| `AsignaturaValidatorTest` | Asignatura válida, ausente y desconocida |
| `NivelEstudianteServiceTest` | Nivel por defecto, diagnóstico, idempotencia y umbrales |
| `EsquemaPracticaTest` | Migraciones, 90 semilla, restricciones, CSV/base y contenido dinámico |
| `PracticaControllerTest` | Endpoints, roles, respuestas, idempotencia y feedback |

### 10.2 Resultado

```text
.\mvnw.cmd test    -> 199 pruebas, 0 fallos, 0 errores
.\mvnw.cmd verify  -> BUILD SUCCESS
```

Testcontainers levanta PostgreSQL 17. Docker Desktop debe estar iniciado.

### 10.3 Casos cubiertos

- 90 ejercicios activos, 45 por asignatura, 5 por combinación.
- Exactamente una opción correcta por ejercicio de opción múltiple.
- Ejercicios numéricos sin opciones.
- Nivel fuera de 1–3 rechazado.
- Tipo/respuesta incoherente rechazada.
- Opción de otro ejercicio rechazada.
- Idempotencia y número de intento únicos.
- Asignatura obligatoria y validada.
- No mezcla de asignaturas.
- No repetición de ejercicios acertados en primer intento.
- Fallback por nivel y por competencia sin ejercicios.
- Nivel inicial desde diagnóstico y valores BAJO/MEDIO/ALTO.
- N=3 y M=3, límites 1 y 3.
- No fuga de clave ni explicación en `GET /siguiente`.
- Rol `DOCENTE`/`ADMIN` recibe 403.
- Persistencia de intento, feedback y snapshots.
- Inserción de un ejercicio sin migración y desactivación.

---

## 11. Limitaciones y trabajo pendiente

- El CRUD docente y su interfaz son las historias #132/#133/#134; no se implementaron aquí.
- El motor no usa IA ni aprendizaje automático.
- El feedback no se audita por intento en el MVP; se puede añadir después.
- No se implementó la integración con grupos académicos; la asignatura se recibe por parámetro.
- No se modificó el frontend. La interfaz de práctica es la historia #104.
- La semilla debe revisarse pedagógicamente con un docente antes de considerarse contenido definitivo.
- Los cambios están en la rama `feature/motor-practica-adaptativa` y **no fueron commiteados** por el agente.

---

## 12. Cómo revisar rápidamente

1. Leer este documento.
2. Abrir `docs/base-de-datos/modelo-fisico.md` para el modelo.
3. Abrir `docs/README-backend.md` para la API y configuración.
4. Revisar `docs/datos/ejercicios_practica.csv` para el banco.
5. Ejecutar:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

6. Probar manualmente con `docs/code/motor-practica-adaptativa.md`, sección 7.3.
