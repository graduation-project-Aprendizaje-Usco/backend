# Especificación de Requisitos y Comportamiento Funcional

## Aprendizaje USCO

---

## 6.1. Información general

### 6.1.1 Propósito

Este documento consolida los requisitos funcionales y no funcionales, las historias de usuario, los criterios de aceptación y las reglas de negocio del sistema **Aprendizaje USCO**. Sirve como referencia contractual para el comportamiento esperado del software y como insumo para el plan de pruebas.

### 6.1.2 Alcance

Cubre los módulos de autenticación, diagnóstico, práctica adaptativa, contenidos, progreso, analítica docente y administración de usuarios, en los estados actualmente identificados en el repositorio.

### 6.1.3 Referencias

- [Documento Maestro de Ingeniería de Software](./01_documento_maestro_ingenieria_software.md)
- [Aseguramiento de Calidad y Plan de Pruebas](./03_aseguramiento_calidad_y_plan_pruebas.md)
- `aprendizaje-usco-backend/docs/README-backend.md`
- `aprendizaje-usco-backend/docs/tareas-y-responsables.md`

### 6.1.4 Definiciones

- **Estudiante:** usuario con rol `ESTUDIANTE`.
- **Docente:** usuario con rol `DOCENTE`.
- **Administrador:** usuario con rol `ADMIN`.
- **Ítem:** pregunta de la prueba diagnóstica.
- **Ejercicio:** pregunta de práctica adaptativa.

### 6.1.5 Actores

| Actor | Rol en el sistema |
|---|---|
| Estudiante | Presenta diagnóstico, practica y consulta progreso. |
| Docente | Revisa desempeño grupal y gestiona actividades. |
| Administrador | Gestiona usuarios y roles. |
| Sistema | Califica, categoriza, selecciona ejercicios y cierra intentos vencidos. |

### 6.1.6 Restricciones

- El registro de estudiantes requiere correo institucional `@usco.edu.co` con formato `u` + 11 dígitos.
- La prueba diagnóstica se presenta una sola vez por estudiante en el MVP.
- El motor adaptativo no usa inteligencia artificial; es basado en reglas.

### 6.1.7 Supuestos

- Los docentes y administradores son creados por un administrador.
- Los estudiantes pertenecerán a un grupo docente en una versión futura.
- PostgreSQL 17 está disponible en todos los entornos.

### 6.1.8 Estado y control de versiones

| Versión | Fecha | Descripción |
|---|---|---|
| 1.0 | 2026-10-26 | Especificación inicial derivada de la auditoría del repositorio. |

---

## 6.2. Catálogo de funcionalidades

| Módulo | Funcionalidad | Estado |
|---|---|---|
| Autenticación | Inicio de sesión | Implementado |
| Autenticación | Registro de estudiantes | Implementado |
| Autenticación | Cierre de sesión | Implementado |
| Autenticación | Confirmación de sesión (`/me`) | Implementado |
| Diagnóstico | Iniciar/retomar prueba | Implementado (backend) |
| Diagnóstico | Responder ítems | Implementado (backend) |
| Diagnóstico | Finalizar prueba | Implementado (backend) |
| Diagnóstico | Calcular puntaje y categoría | Implementado (backend) |
| Práctica | Seleccionar siguiente ejercicio | Implementado (backend) |
| Práctica | Registrar intento | Implementado (backend) |
| Práctica | Retroalimentación progresiva | Implementado (backend) |
| Práctica | Ajustar nivel por racha | Implementado (backend) |
| Contenidos | Modelo de contenidos y ejercicios | Implementado |
| Contenidos | CRUD docente de contenidos | Diseñado |
| Progreso | Dashboard del estudiante | Diseñado |
| Analítica | Dashboard del docente | Diseñado |
| Administración | Gestión de usuarios y roles | Diseñado |
| Grupos | Asociación docente-estudiante | Diseñado |

---

## 6.3. Requisitos funcionales

### RF-01 — Autenticación y control de acceso

#### RF-AUT-001: Inicio de sesión

| Campo | Descripción |
|---|---|
| **Nombre** | Inicio de sesión con correo y contraseña. |
| **Descripción** | El sistema debe permitir a un usuario iniciar sesión con su correo y contraseña, validar las credenciales y devolver un token JWT con su rol. |
| **Actor** | Usuario registrado. |
| **Precondiciones** | El usuario existe y está activo. |
| **Flujo principal** | 1. El usuario ingresa correo y contraseña. 2. El backend valida credenciales. 3. Se emite token JWT. 4. El frontend redirige al inicio del rol. |
| **Flujos alternativos** | Credenciales inválidas → 401 con mensaje genérico. Usuario inactivo → 401. |
| **Entradas** | `{ correo, password }` |
| **Salidas** | `{ token, tipo, expiraEn, usuario }` |
| **Prioridad** | Alta |
| **Estado** | Implementado y verificado |
| **Evidencia** | `AuthController.java`, `AuthService.java`, `LoginPage.jsx` |

#### RF-AUT-002: Registro de estudiantes

| Campo | Descripción |
|---|---|
| **Nombre** | Registro público de estudiantes. |
| **Descripción** | El sistema debe permitir que un estudiante se registre con correo institucional, contraseña, nombres, apellidos y programa académico. |
| **Actor** | Estudiante no registrado. |
| **Precondiciones** | El correo no existe en la base. |
| **Flujo principal** | 1. El estudiante completa el formulario. 2. El backend valida los datos. 3. Se crea el usuario con rol ESTUDIANTE. 4. Se devuelve 201. |
| **Flujos alternativos** | Correo ya registrado → 409. Datos inválidos → 400. |
| **Entradas** | `{ nombres, apellidos, correo, password, programaAcademico }` |
| **Salidas** | `{ id, nombres, correo, rol }` |
| **Prioridad** | Alta |
| **Estado** | Implementado y verificado |
| **Evidencia** | `AuthController.java`, `RegistroPage.jsx`, `reglasRegistro.js` |

#### RF-AUT-003: Control de acceso por rol

| Campo | Descripción |
|---|---|
| **Nombre** | Restricción de endpoints y rutas por rol. |
| **Descripción** | El backend debe rechazar con 403 las peticiones de un rol a recursos de otro rol. El frontend debe redirigir a `/no-autorizado`. |
| **Actor** | Sistema. |
| **Precondiciones** | El usuario tiene sesión válida. |
| **Flujo principal** | 1. El usuario solicita un recurso. 2. El sistema valida el rol del token. 3. Si no tiene permiso, responde 403 o redirige. |
| **Prioridad** | Alta |
| **Estado** | Implementado y verificado |
| **Evidencia** | `SecurityConfig.java`, `ProtectedRoute.jsx`, `AuthContext.jsx` |

### RF-02 — Aplicación de la prueba diagnóstica

#### RF-DIA-001: Iniciar prueba diagnóstica

| Campo | Descripción |
|---|---|
| **Nombre** | Iniciar o retomar intento diagnóstico. |
| **Descripción** | El estudiante puede iniciar la prueba activa. Si ya tiene un intento (en curso o finalizado), se devuelve el mismo. |
| **Actor** | Estudiante. |
| **Precondiciones** | Existe una prueba activa. El usuario tiene rol ESTUDIANTE. |
| **Flujo principal** | 1. POST `/api/diagnosticos/intentos`. 2. Se crea o recupera el intento. 3. Se devuelve el estado con los 24 ítems (sin clave). |
| **Flujos alternativos** | No hay prueba activa → 404. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `DiagnosticoController.java`, `DiagnosticoService.java` |

#### RF-DIA-002: Responder ítems

| Campo | Descripción |
|---|---|
| **Nombre** | Registrar o cambiar respuesta de un ítem. |
| **Descripción** | El estudiante puede enviar la opción elegida para un ítem. Si ya respondió, se actualiza. |
| **Actor** | Estudiante. |
| **Precondiciones** | El intento está EN_CURSO y pertenece al estudiante. |
| **Flujo principal** | 1. PUT `/api/diagnosticos/intentos/{id}/respuestas/{itemId}`. 2. Se valida que el ítem pertenezca al intento. 3. Se guarda la respuesta. |
| **Flujos alternativos** | Intento finalizado → 409. Ítem no pertenece → 404/422. Tiempo vencido → 422. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `DiagnosticoController.java` |

#### RF-DIA-003: Finalizar prueba

| Campo | Descripción |
|---|---|
| **Nombre** | Entregar prueba y calificar. |
| **Descripción** | El estudiante finaliza el intento; el sistema calcula puntaje, categoría y niveles por competencia. |
| **Actor** | Estudiante. |
| **Precondiciones** | El intento existe, es del estudiante y está EN_CURSO. |
| **Flujo principal** | 1. POST `/api/diagnosticos/intentos/{id}/finalizar`. 2. Se califican las respuestas. 3. Se guardan resultados. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `DiagnosticoService.java`, `CalculadoraPuntaje.java` |

### RF-03 — Cálculo automático del puntaje

#### RF-DIA-004: Calcular puntaje total y por dimensión

| Campo | Descripción |
|---|---|
| **Nombre** | Cálculo de puntaje diagnóstico. |
| **Descripción** | Al finalizar, el sistema calcula el puntaje total (0-24) y los aciertos por competencia (0-8). Las preguntas sin responder cuentan como incorrectas. |
| **Actor** | Sistema. |
| **Precondiciones** | El intento se está finalizando. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `CalculadoraPuntaje.java`, `DiagnosticoService.java` |

### RF-04 — Asignación de categoría de desempeño

#### RF-DIA-005: Categorizar desempeño global y por competencia

| Campo | Descripción |
|---|---|
| **Nombre** | Categorización del desempeño. |
| **Descripción** | Asignar categoría global (`ALTO_RIESGO`, `RIESGO_MODERADO`, `NIVEL_ADECUADO`) y nivel por competencia (`BAJO`, `MEDIO`, `ALTO`) según umbrales configurables. |
| **Actor** | Sistema. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `UmbralesDiagnostico.java`, `application.properties` |

### RF-05 — Motor de selección adaptativa

#### RF-PRA-001: Seleccionar siguiente ejercicio

| Campo | Descripción |
|---|---|
| **Nombre** | Siguiente ejercicio adaptativo. |
| **Descripción** | El sistema debe seleccionar el siguiente ejercicio según la asignatura, el nivel actual por competencia, el diagnóstico y el historial reciente. |
| **Actor** | Sistema (invocado por estudiante). |
| **Precondiciones** | El estudiante tiene sesión válida. |
| **Entradas** | `asignatura` (CALCULO_DIFERENCIAL o CALCULO_INTEGRAL) |
| **Salidas** | Ejercicio sin clave ni explicación. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `SeleccionEjercicioService.java`, `PracticaController.java` |

### RF-06 — Interfaz de ejercicios interactivos

#### RF-PRA-002: Resolver ejercicios (frontend)

| Campo | Descripción |
|---|---|
| **Nombre** | Pantalla de ejercicios interactivos. |
| **Descripción** | El frontend debe mostrar el ejercicio seleccionado, permitir responder (opción múltiple o numérica) y mostrar el resultado. |
| **Actor** | Estudiante. |
| **Prioridad** | Alta |
| **Estado** | Diseñado (backend listo; frontend provisional) |
| **Evidencia** | `PracticaPage.jsx` (provisional), contrato en README-backend |

### RF-07 — Retroalimentación inmediata

#### RF-PRA-003: Mostrar retroalimentación según intento

| Campo | Descripción |
|---|---|
| **Nombre** | Retroalimentación progresiva. |
| **Descripción** | Primer fallo → pista + error común. Segundo fallo → explicación completa. Acierto → explicación breve. |
| **Actor** | Sistema. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `FeedbackService.java` |

### RF-08 — Registro de intentos con trazabilidad

#### RF-PRA-004: Registrar intento de práctica

| Campo | Descripción |
|---|---|
| **Nombre** | Registrar respuesta a ejercicio. |
| **Descripción** | Persistir cada intento con estudiante, ejercicio, resultado, tiempo empleado y clave de idempotencia. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `PracticaService.java`, `IntentoPractica.java` |

### RF-09 — Indicadores de progreso del estudiante

#### RF-PRO-001: Dashboard de progreso

| Campo | Descripción |
|---|---|
| **Nombre** | Mostrar progreso del estudiante. |
| **Descripción** | El estudiante debe consultar su nivel por competencia, aciertos, ejercicios completados y tendencia. |
| **Actor** | Estudiante. |
| **Prioridad** | Media |
| **Estado** | Diseñado |
| **Evidencia** | Tarea #119 en `tareas-y-responsables.md`, `ProgresoPage.jsx` (provisional) |

### RF-10 — Indicadores de grupo para el docente

#### RF-ANA-001: Dashboard de desempeño grupal

| Campo | Descripción |
|---|---|
| **Nombre** | Consultar desempeño de grupos. |
| **Descripción** | El docente debe ver agregaciones de desempeño solo de los estudiantes de sus grupos. |
| **Actor** | Docente. |
| **Prioridad** | Media |
| **Estado** | Diseñado |
| **Evidencia** | Tarea #124 en `tareas-y-responsables.md` |

### RF-11 — Indicadores derivados de analítica

#### RF-ANA-002: Indicadores derivados

| Campo | Descripción |
|---|---|
| **Nombre** | Patrones de aprendizaje del grupo. |
| **Descripción** | Mostrar top de ejercicios con más errores y dimensión más débil por grupo. |
| **Actor** | Docente. |
| **Prioridad** | Media |
| **Estado** | Diseñado |
| **Evidencia** | Tarea #131 en `tareas-y-responsables.md` |

### RF-12 — Gestión de actividades

#### RF-CON-001: CRUD de contenidos y ejercicios

| Campo | Descripción |
|---|---|
| **Nombre** | Gestionar actividades de práctica. |
| **Descripción** | El docente debe crear, editar, activar/desactivar y eliminar contenidos y ejercicios. |
| **Actor** | Docente. |
| **Prioridad** | Media |
| **Estado** | Diseñado |
| **Evidencia** | Tarea #132 en `tareas-y-responsables.md` |

### RF-13 — Administración de usuarios y roles

#### RF-ADM-001: Gestión de usuarios

| Campo | Descripción |
|---|---|
| **Nombre** | Administrar usuarios y roles. |
| **Descripción** | El administrador debe crear, editar, desactivar usuarios y cambiar su rol. |
| **Actor** | Administrador. |
| **Prioridad** | Media |
| **Estado** | Diseñado |
| **Evidencia** | Tarea #136 en `tareas-y-responsables.md` |

### RF-14 — Banco de ejercicios por competencia y nivel

#### RF-CON-002: Banco semilla de ejercicios

| Campo | Descripción |
|---|---|
| **Nombre** | Disponer de ejercicios por competencia y nivel. |
| **Descripción** | El sistema debe contar con ejercicios de opción múltiple y numérica, organizados por asignatura, competencia y nivel. |
| **Actor** | Sistema. |
| **Prioridad** | Alta |
| **Estado** | Implementado |
| **Evidencia** | `V202610221010__datos_ejercicios_semilla.sql`, `ejercicios_practica.csv` |

### RF-15 — Historial de resultados

#### RF-PRO-002: Consultar historial

| Campo | Descripción |
|---|---|
| **Nombre** | Historial de diagnósticos e intentos de práctica. |
| **Descripción** | Permitir consultar el historial con filtros por fecha, tipo y competencia; paginado. |
| **Actor** | Estudiante, docente. |
| **Prioridad** | Baja |
| **Estado** | Diseñado |
| **Evidencia** | Tarea #128 en `tareas-y-responsables.md` |

### RF-16 — Lógica de niveles de dificultad

#### RF-PRA-005: Subir y bajar de nivel

| Campo | Descripción |
|---|---|
| **Nombre** | Ajuste de nivel por racha. |
| **Descripción** | Subir de nivel tras N aciertos consecutivos y bajar tras M errores consecutivos; límites 1-3. N y M configurables. |
| **Actor** | Sistema. |
| **Prioridad** | Alta |
| **Estado** | Implementado (backend) |
| **Evidencia** | `NivelService.java`, `application.properties` |

---

## 6.4. Requisitos no funcionales

| ID | Categoría | Descripción | Métrica | Prioridad | Estado |
|---|---|---|---|---|---|
| RNF-01 | Usabilidad | La plataforma debe ser fácil de usar. | ≥ 80 % tareas sin ayuda; SUS ≥ 68. | Alta | Propuesto |
| RNF-02 | Rendimiento | Respuesta rápida de endpoints críticos. | p95 < 1 s con 30 usuarios concurrentes. | Media | Propuesto |
| RNF-03 | Seguridad | Autenticación y autorización robustas. | 100 % de endpoints protegidos con `@PreAuthorize` salvo excepciones documentadas. | Alta | Implementado |
| RNF-04 | Seguridad | Sin duplicación de intentos. | 50 envíos simultáneos producen un solo registro. | Alta | Implementado |
| RNF-05 | Accesibilidad | Cumplimiento WCAG 2.1 AA. | Lighthouse Accessibility ≥ 90 en pantallas principales. | Media | Parcialmente implementado |
| RNF-06 | Mantenibilidad | Código organizado por módulos. | Cada módulo en paquete independiente. | Media | Implementado |
| RNF-07 | Disponibilidad | Backend saludable. | `/actuator/health` responde UP. | Media | Implementado |
| RNF-08 | Escalabilidad | Nuevas competencias sin cambio de código. | Agregar fila en `competencia` sin despliegue. | Media | Implementado |
| RNF-09 | Portabilidad | Ejecución en contenedores. | `compose.yaml` levanta PostgreSQL y backend. | Media | Configurado |
| RNF-10 | Fiabilidad | Datos consistentes ante fallos. | Migraciones atómicas y transacciones de servicio. | Alta | Implementado |

---

## 6.5. Historias de usuario

### HU-AUT-001 — Iniciar sesión

- **Identificador:** HU-AUT-001
- **Título:** Iniciar sesión
- **Rol:** Como usuario registrado
- **Descripción:** quiero iniciar sesión con mi correo y contraseña
- **Beneficio:** para acceder a las funciones de mi rol.
- **Prioridad:** Alta
- **Estado:** Implementada
- **Requisitos relacionados:** RF-AUT-001
- **Criterios de aceptación:**
  - El login devuelve token y datos del usuario.
  - Credenciales inválidas muestran mensaje genérico.
  - El frontend redirige al inicio del rol.

### HU-AUT-002 — Registrarme como estudiante

- **Identificador:** HU-AUT-002
- **Título:** Registro de estudiantes
- **Rol:** Como estudiante no registrado
- **Descripción:** quiero crear una cuenta con mi correo institucional
- **Beneficio:** para usar la plataforma de aprendizaje.
- **Prioridad:** Alta
- **Estado:** Implementada
- **Requisitos relacionados:** RF-AUT-002
- **Criterios de aceptación:**
  - Se valida el formato del correo estudiantil.
  - Se valida la contraseña.
  - Al registrarme vuelvo al login con aviso de éxito.

### HU-DIA-001 — Presentar la prueba diagnóstica

- **Identificador:** HU-DIA-001
- **Título:** Presentar diagnóstico
- **Rol:** Como estudiante
- **Descripción:** quiero presentar la prueba diagnóstica de 24 ítems
- **Beneficio:** para conocer mi nivel en cada competencia.
- **Prioridad:** Alta
- **Estado:** Backend implementado; frontend provisional
- **Requisitos relacionados:** RF-DIA-001, RF-DIA-002, RF-DIA-003
- **Criterios de aceptación:**
  - Puedo iniciar, responder, retomar y finalizar la prueba.
  - El tiempo lo controla el servidor.
  - Al finalizar veo puntaje, categoría y niveles por competencia.

### HU-PRA-001 — Practicar ejercicios adaptativos

- **Identificador:** HU-PRA-001
- **Título:** Practicar ejercicios
- **Rol:** Como estudiante
- **Descripción:** quiero resolver ejercicios seleccionados por el sistema según mi nivel
- **Beneficio:** para reforzar las competencias con dificultad adecuada.
- **Prioridad:** Alta
- **Estado:** Backend implementado; frontend provisional
- **Requisitos relacionados:** RF-PRA-001, RF-PRA-004, RF-PRA-005
- **Criterios de aceptación:**
  - El sistema elige ejercicios sin repetir los ya acertados.
  - Al responder se actualiza mi nivel.
  - Puedo recibir pista o explicación según el intento.

### HU-PRO-001 — Consultar mi progreso

- **Identificador:** HU-PRO-001
- **Título:** Ver mi progreso
- **Rol:** Como estudiante
- **Descripción:** quiero ver mis indicadores de progreso
- **Beneficio:** para saber en qué competencias debo seguir practicando.
- **Prioridad:** Media
- **Estado:** Diseñado
- **Requisitos relacionados:** RF-PRO-001

### HU-ANA-001 — Revisar desempeño de mi grupo

- **Identificador:** HU-ANA-001
- **Título:** Ver desempeño del grupo
- **Rol:** Como docente
- **Descripción:** quiero consultar el desempeño de mis estudiantes
- **Beneficio:** para identificar dificultades de aprendizaje.
- **Prioridad:** Media
- **Estado:** Diseñado
- **Requisitos relacionados:** RF-ANA-001, RF-ANA-002

### HU-ADM-001 — Administrar usuarios

- **Identificador:** HU-ADM-001
- **Título:** Gestionar usuarios
- **Rol:** Como administrador
- **Descripción:** quiero crear, editar y desactivar usuarios
- **Beneficio:** para administrar el acceso a la plataforma.
- **Prioridad:** Media
- **Estado:** Diseñado
- **Requisitos relacionados:** RF-ADM-001

---

## 6.6. Criterios de aceptación en Gherkin

### CA-AUT-001: Login exitoso

```gherkin
Escenario: Iniciar sesión con credenciales válidas
  Dado que el usuario "docente@aprendizaje.test" está registrado y activo
    Y su contraseña es "Docente2026*"
  Cuando envía una petición POST a "/api/auth/login" con esas credenciales
  Entonces la respuesta tiene código 200
    Y el cuerpo contiene un token JWT
    Y los datos del usuario incluyen rol "DOCENTE"
```

### CA-AUT-002: Login con credenciales inválidas

```gherkin
Escenario: Iniciar sesión con contraseña incorrecta
  Dado que el usuario "docente@aprendizaje.test" está registrado
  Cuando envía una petición POST a "/api/auth/login" con contraseña incorrecta
  Entonces la respuesta tiene código 401
    Y el mensaje no revela si el correo existe
```

### CA-AUT-003: Registro de estudiante

```gherkin
Escenario: Registrar un estudiante con datos válidos
  Dado que el correo "u20201234567@usco.edu.co" no está registrado
  Cuando envía una petición POST a "/api/auth/registro" con nombres, apellidos, programa y contraseña válida
  Entonces la respuesta tiene código 201
    Y el usuario creado tiene rol "ESTUDIANTE"
```

### CA-DIA-001: Iniciar diagnóstico

```gherkin
Escenario: Estudiante inicia la prueba activa
  Dado que existe una prueba diagnóstica activa
    Y el estudiante no tiene intentos previos
  Cuando envía POST a "/api/diagnosticos/intentos"
  Entonces la respuesta tiene código 201
    Y el intento contiene 24 ítems
    Y ninguna opción incluye "esCorrecta"
```

### CA-DIA-002: Responder ítem

```gherkin
Escenario: Guardar respuesta de un ítem
  Dado que el estudiante tiene un intento EN_CURSO
  Cuando envía PUT a "/api/diagnosticos/intentos/{id}/respuestas/{itemId}" con una opción válida
  Entonces la respuesta tiene código 200
    Y la respuesta queda registrada
```

### CA-DIA-003: Finalizar y calificar

```gherkin
Escenario: Finalizar la prueba y obtener resultado
  Dado que el estudiante respondió 24 ítems
  Cuando envía POST a "/api/diagnosticos/intentos/{id}/finalizar"
  Entonces la respuesta tiene código 200
    Y el intento está FINALIZADO
    Y el resultado incluye puntaje total, categoría global y niveles por competencia
```

### CA-PRA-001: Obtener siguiente ejercicio

```gherkin
Escenario: Solicitar ejercicio de Cálculo Diferencial
  Dado que el estudiante tiene sesión activa
    Y existen ejercicios activos de "CALCULO_DIFERENCIAL"
  Cuando envía GET a "/api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL"
  Entonces la respuesta tiene código 200
    Y el ejercicio no incluye la respuesta correcta ni la explicación
```

### CA-PRA-002: Retroalimentación en primer fallo

```gherkin
Escenario: Primer intento incorrecto muestra pista
  Dado que el estudiante envía su primer intento de un ejercicio
    Y la respuesta es incorrecta
  Cuando envía POST a "/api/practica/intentos"
  Entonces la respuesta indica "esCorrecta": false
    Y "puedeReintentar": true
    Y el tipo de retroalimentación es "PISTA"
```

### CA-PRA-003: Idempotencia de intentos

```gherkin
Escenario: Doble envío del mismo intento no crea duplicados
  Dado que el estudiante envía un intento con una clave de idempotencia
  Cuando envía el mismo cuerpo con la misma clave dos veces
  Entonces solo se crea un registro en la base de datos
```

---

## 6.7. Casos de uso y flujos

### CU-01: Presentar prueba diagnóstica

- **Actor:** Estudiante
- **Objetivo:** completar la prueba diagnóstica.
- **Precondiciones:** estar autenticado como ESTUDIANTE.
- **Flujo principal:**
  1. El estudiante inicia el intento.
  2. El sistema muestra los 24 ítems.
  3. El estudiante responde los ítems.
  4. El estudiante finaliza.
  5. El sistema califica y muestra el resultado.
- **Flujos alternativos:**
  - Si se acaba el tiempo, el sistema finaliza automáticamente.
  - Si el estudiante ya presentó la prueba, se devuelve el intento existente.
- **Excepciones:**
  - Sin prueba activa → 404.
  - Intento de otro estudiante → 403.

### CU-02: Practicar ejercicios

- **Actor:** Estudiante
- **Objetivo:** resolver ejercicios adaptativos.
- **Precondiciones:** estar autenticado como ESTUDIANTE.
- **Flujo principal:**
  1. El estudiante solicita un ejercicio de una asignatura.
  2. El sistema selecciona el ejercicio.
  3. El estudiante responde.
  4. El sistema devuelve retroalimentación y actualiza el nivel.
  5. El estudiante solicita el siguiente ejercicio.
- **Excepciones:**
  - Sin ejercicios disponibles → 404.
  - Asignatura inválida → 400.

---

## 6.8. Reglas de negocio

| ID | Regla | Tipo | Estado |
|---|---|---|---|
| RN-01 | El correo de estudiante debe tener el formato `u` + 11 dígitos + `@usco.edu.co`. | Confirmada | Implementada |
| RN-02 | La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número. | Confirmada | Implementada |
| RN-03 | Solo una prueba diagnóstica puede estar activa. | Confirmada | Implementada |
| RN-04 | Cada estudiante presenta la prueba una sola vez en el MVP. | Confirmada | Implementada |
| RN-05 | Las preguntas sin responder en el diagnóstico cuentan como incorrectas. | Confirmada | Implementada |
| RN-06 | Los umbrales de categorización son configurables y se aplican al finalizar. | Confirmada | Implementada |
| RN-07 | El motor no repite ejercicios acertados en primer intento mientras haya alternativas. | Confirmada | Implementada |
| RN-08 | Solo el primer intento de cada ejercicio cuenta para el nivel. | Confirmada | Implementada |
| RN-09 | N y M aciertos/errores consecutivos suben/bajan de nivel; límites 1-3. | Confirmada | Implementada |
| RN-10 | Un ejercicio con intentos no se elimina; se desactiva. | Propuesta | Modelada |

---

## 6.9. Matriz de trazabilidad

| Objetivo | Requisito | Historia | Módulo | Caso de prueba | Estado |
|---|---|---|---|---|---|
| Autenticación | RF-AUT-001 | HU-AUT-001 | MOD-AUT | CP-AUT-001 | Implementado |
| Registro | RF-AUT-002 | HU-AUT-002 | MOD-AUT | CP-AUT-002 | Implementado |
| Roles | RF-AUT-003 | — | MOD-AUT | CP-AUT-003 | Implementado |
| Diagnóstico | RF-DIA-001 | HU-DIA-001 | MOD-DIA | CP-DIA-001 | Backend listo |
| Diagnóstico | RF-DIA-002 | HU-DIA-001 | MOD-DIA | CP-DIA-002 | Backend listo |
| Diagnóstico | RF-DIA-003 | HU-DIA-001 | MOD-DIA | CP-DIA-003 | Backend listo |
| Calificación | RF-DIA-004 | HU-DIA-001 | MOD-DIA | CP-DIA-004 | Backend listo |
| Categorización | RF-DIA-005 | HU-DIA-001 | MOD-DIA | CP-DIA-005 | Backend listo |
| Selección | RF-PRA-001 | HU-PRA-001 | MOD-PRA | CP-PRA-001 | Backend listo |
| Interfaz práctica | RF-PRA-002 | HU-PRA-001 | MOD-PRA | CP-PRA-002 | Diseñado |
| Retroalimentación | RF-PRA-003 | HU-PRA-001 | MOD-PRA | CP-PRA-003 | Backend listo |
| Registro intentos | RF-PRA-004 | HU-PRA-001 | MOD-PRA | CP-PRA-004 | Backend listo |
| Niveles | RF-PRA-005 | HU-PRA-001 | MOD-PRA | CP-PRA-005 | Backend listo |
| Progreso | RF-PRO-001 | HU-PRO-001 | MOD-PRO | CP-PRO-001 | Diseñado |
| Historial | RF-PRO-002 | — | MOD-PRO | CP-PRO-002 | Diseñado |
| Analítica | RF-ANA-001 | HU-ANA-001 | MOD-ANA | CP-ANA-001 | Diseñado |
| Analítica | RF-ANA-002 | HU-ANA-001 | MOD-ANA | CP-ANA-002 | Diseñado |
| Contenidos | RF-CON-001 | — | MOD-CON | CP-CON-001 | Diseñado |
| Banco | RF-CON-002 | — | MOD-CON | CP-CON-002 | Implementado |
| Administración | RF-ADM-001 | HU-ADM-001 | MOD-ADM | CP-ADM-001 | Diseñado |

---

## 6.10. Requisitos pendientes y control de cambios

### 6.10.1 Vacíos identificados

- No existen endpoints implementados para progreso, analítica, grupos ni administración.
- Las pantallas funcionales principales del frontend son provisionales.
- El registro por invitación docente está pendiente.
- Las pruebas de rendimiento y usabilidad no han sido ejecutadas.

### 6.10.2 Contradicciones detectadas

- Los archivos `.docx` externos (`PMOI-Documento de requerimientos.docx` y `Hist_usuarios_MOD2.doc.docx`) son plantillas genéricas y no corresponden al proyecto Aprendizaje USCO. No deben usarse como fuente de requisitos.

### 6.10.3 Decisiones por confirmar

- ¿El módulo de práctica adaptativa ya fue fusionado a `develop` o permanece en rama feature? Los archivos existen en el directorio de trabajo, pero no se verificó el estado de git.
- ¿Existen URLs de staging funcionales? No se verificó despliegue.

---

*Fin de la Especificación de Requisitos y Comportamiento Funcional*
