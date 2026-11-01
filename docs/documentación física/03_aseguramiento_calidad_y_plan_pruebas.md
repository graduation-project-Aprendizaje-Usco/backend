# Aseguramiento de Calidad y Plan de Pruebas

## Aprendizaje USCO

---

## 7.1. Objetivos y alcance de QA

### 7.1.1 Propósito

Este documento define cómo se comprueba que el software cumple sus requisitos funcionales y no funcionales. Incluye la estrategia, el plan, los casos de prueba y el estado actual de la calidad del producto.

### 7.1.2 Alcance de las pruebas

Se incluyen:

- Pruebas unitarias y de integración del backend.
- Pruebas unitarias y de componente del frontend.
- Pruebas de contrato de API.
- Pruebas de esquema de base de datos.
- Pruebas de seguridad básicas (autenticación y autorización).

Se excluyen en esta versión:

- Pruebas de rendimiento formales.
- Pruebas de usabilidad con usuarios piloto.
- Pruebas de penetración.

### 7.1.3 Componentes incluidos

- Backend: autenticación, diagnóstico, práctica adaptativa, esquema de base de datos.
- Frontend: autenticación, enrutamiento, protección por rol, contexto de sesión.

### 7.1.4 Componentes excluidos

- Módulos de progreso, analítica, administración y grupos (no implementados).
- Pantallas provisionales del frontend que solo muestran "En construcción".

### 7.1.5 Riesgos

- Docker Desktop no disponible en el entorno de auditoría impide ejecutar las pruebas de integración del backend.
- Las pantallas provisionales dificultan las pruebas end-to-end del flujo completo.

### 7.1.6 Responsabilidades

| Rol | Responsabilidad |
|---|---|
| Desarrolladores | Escribir pruebas unitarias e integración. |
| Líder técnico | Revisar cobertura y aprobación de PR. |
| CI/CD | Ejecutar lint, pruebas y build en cada PR. |

### 7.1.7 Entornos

- Desarrollo local: Windows 11, Node 24, Java 21, PostgreSQL 17 en Docker.
- CI: GitHub Actions con Ubuntu, Node 24, Java 21 Temurin.

### 7.1.8 Supuestos y restricciones

- Docker Desktop debe estar corriendo para las pruebas de backend.
- El frontend puede probarse sin backend usando `VITE_USE_MOCKS=true`.

### 7.1.9 Referencias

- [Documento Maestro de Ingeniería de Software](./01_documento_maestro_ingenieria_software.md)
- [Especificación de Requisitos y Comportamiento Funcional](./02_especificacion_requisitos_y_historias_usuario.md)

---

## 7.2. Estrategia de pruebas

### 7.2.1 Niveles de prueba

| Nivel | Descripción | Estado actual |
|---|---|---|
| Unitarias | Pruebas de clases y funciones aisladas. | Implementadas en backend y frontend. |
| Integración | Pruebas con base de datos real (Testcontainers). | Implementadas en backend. |
| API | Pruebas de contrato y códigos de respuesta. | Implementadas en backend (MockMvc). |
| UI/Componente | Pruebas de componentes React con Vitest. | Implementadas en frontend. |
| End-to-end | Flujos completos en navegador. | No implementadas. |
| Rendimiento | Carga y tiempos de respuesta. | No ejecutadas. |
| Seguridad | Fuerza bruta, inyección, CORS. | Parciales (autenticación/autorización). |
| Usabilidad | Validación con usuarios. | No ejecutadas. |

### 7.2.2 Enfoque por módulo

- **Autenticación:** pruebas de login, registro, token y roles.
- **Diagnóstico:** pruebas de inicio, respuesta, finalización, calificación y categorización.
- **Práctica:** pruebas de selección, retroalimentación, niveles e idempotencia.
- **Base de datos:** pruebas de migraciones, restricciones y semillas.

---

## 7.3. Plan de pruebas

### 7.3.1 Pruebas unitarias del backend

- **Objetivo:** verificar la lógica de negocio de servicios y utilidades.
- **Técnicas:** JUnit 5, Mockito, AssertJ.
- **Herramientas:** Maven, Surefire.
- **Datos de prueba:** datos en memoria y mocks.
- **Procedimiento:** ejecutar `./mvnw test`.
- **Resultado esperado:** BUILD SUCCESS sin fallos.

### 7.3.2 Pruebas de integración del backend

- **Objetivo:** verificar el comportamiento con PostgreSQL real.
- **Técnicas:** Testcontainers, MockMvc.
- **Herramientas:** `testcontainers-postgresql`, Spring Boot Test.
- **Datos de prueba:** migraciones Flyway y semillas.
- **Procedimiento:** ejecutar `./mvnw verify` con Docker Desktop activo.
- **Resultado esperado:** BUILD SUCCESS.

### 7.3.3 Pruebas del frontend

- **Objetivo:** verificar componentes, enrutamiento y contexto.
- **Técnicas:** Vitest, Testing Library, mocks de API.
- **Herramientas:** Vitest, jsdom, user-event.
- **Datos de prueba:** tokens JWT de prueba y funciones simuladas.
- **Procedimiento:** ejecutar `npm test`.
- **Resultado esperado:** 27 tests aprobados.

### 7.3.4 Pruebas de lint y build

- **Objetivo:** garantizar calidad de código y build de producción.
- **Herramientas:** ESLint, Prettier, Vite.
- **Procedimiento:** `npm run lint`, `npm run build`.
- **Resultado esperado:** sin errores.

---

## 7.4. Catálogo de casos de prueba

### CP-AUT-001: Login con credenciales válidas

| Campo | Valor |
|---|---|
| **ID** | CP-AUT-001 |
| **Nombre** | Login con credenciales válidas |
| **Requisito** | RF-AUT-001 |
| **Objetivo** | Verificar que un usuario activo puede iniciar sesión. |
| **Precondiciones** | Usuario de prueba existe en la base. |
| **Datos de entrada** | `{ correo: "estudiante@aprendizaje.test", password: "Estudiante2026*" }` |
| **Pasos** | 1. Enviar POST a `/api/auth/login`. 2. Verificar respuesta. |
| **Resultado esperado** | Código 200, token JWT válido, datos del usuario. |
| **Resultado obtenido** | No ejecutado en este ejercicio de documentación. |
| **Estado** | Diseñado |
| **Evidencia** | `AutenticacionTest.java` |

### CP-AUT-002: Registro de estudiante

| Campo | Valor |
|---|---|
| **ID** | CP-AUT-002 |
| **Nombre** | Registro con correo institucional válido |
| **Requisito** | RF-AUT-002 |
| **Objetivo** | Verificar la creación de un estudiante. |
| **Precondiciones** | Correo no registrado. |
| **Datos de entrada** | `{ nombres, apellidos, correo: "u20201234567@usco.edu.co", password, programaAcademico }` |
| **Pasos** | 1. Enviar POST a `/api/auth/registro`. 2. Verificar respuesta. |
| **Resultado esperado** | Código 201, usuario con rol ESTUDIANTE. |
| **Resultado obtenido** | No ejecutado en este ejercicio. |
| **Estado** | Diseñado |

### CP-AUT-003: Acceso por rol

| Campo | Valor |
|---|---|
| **ID** | CP-AUT-003 |
| **Nombre** | Estudiante no accede a recurso de docente |
| **Requisito** | RF-AUT-003 |
| **Objetivo** | Verificar la autorización por rol. |
| **Precondiciones** | Token de estudiante válido. |
| **Pasos** | 1. Llamar endpoint restringido a DOCENTE con token de ESTUDIANTE. |
| **Resultado esperado** | Código 403. |
| **Resultado obtenido** | No ejecutado en este ejercicio. |
| **Estado** | Diseñado |
| **Evidencia** | `AccesoPorRolTest.java` |

### CP-DIA-001: Iniciar diagnóstico

| Campo | Valor |
|---|---|
| **ID** | CP-DIA-001 |
| **Nombre** | Iniciar prueba diagnóstica |
| **Requisito** | RF-DIA-001 |
| **Precondiciones** | Prueba activa configurada. |
| **Pasos** | 1. POST `/api/diagnosticos/intentos`. |
| **Resultado esperado** | 201 con 24 ítems. |
| **Estado** | Diseñado |

### CP-DIA-002: Responder ítem

| Campo | Valor |
|---|---|
| **ID** | CP-DIA-002 |
| **Nombre** | Guardar respuesta |
| **Requisito** | RF-DIA-002 |
| **Precondiciones** | Intento EN_CURSO. |
| **Pasos** | 1. PUT respuesta con opción válida. |
| **Resultado esperado** | 200 y respuesta persistida. |
| **Estado** | Diseñado |

### CP-DIA-003: Finalizar diagnóstico

| Campo | Valor |
|---|---|
| **ID** | CP-DIA-003 |
| **Nombre** | Finalizar y calificar |
| **Requisito** | RF-DIA-003 |
| **Precondiciones** | Intento con respuestas. |
| **Pasos** | 1. POST `/api/diagnosticos/intentos/{id}/finalizar`. |
| **Resultado esperado** | Intento FINALIZADO con puntaje y categoría. |
| **Estado** | Diseñado |

### CP-DIA-004: Cálculo de puntaje

| Campo | Valor |
|---|---|
| **ID** | CP-DIA-004 |
| **Nombre** | Calcular puntaje manualmente |
| **Requisito** | RF-DIA-004 |
| **Precondiciones** | Respuestas conocidas. |
| **Pasos** | 1. Calcular manualmente. 2. Comparar con resultado del endpoint. |
| **Resultado esperado** | Coincidencia exacta. |
| **Estado** | Diseñado |
| **Evidencia** | `CalculadoraPuntajeTest.java` |

### CP-DIA-005: Umbrales de categorización

| Campo | Valor |
|---|---|
| **ID** | CP-DIA-005 |
| **Nombre** | Categoría en los bordes |
| **Requisito** | RF-DIA-005 |
| **Precondiciones** | Umbrales configurados en `application.properties`. |
| **Pasos** | 1. Finalizar intentos con puntajes 9, 10, 16, 17. |
| **Resultado esperado** | Categorías correctas según umbrales. |
| **Estado** | Diseñado |
| **Evidencia** | `UmbralesDiagnosticoTest.java` |

### CP-PRA-001: Selección de ejercicio

| Campo | Valor |
|---|---|
| **ID** | CP-PRA-001 |
| **Nombre** | Obtener siguiente ejercicio |
| **Requisito** | RF-PRA-001 |
| **Precondiciones** | Ejercicios semilla cargados. |
| **Pasos** | 1. GET `/api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL`. |
| **Resultado esperado** | 200 con ejercicio sin clave. |
| **Estado** | Diseñado |

### CP-PRA-002: Retroalimentación

| Campo | Valor |
|---|---|
| **ID** | CP-PRA-002 |
| **Nombre** | Pista en primer fallo |
| **Requisito** | RF-PRA-003 |
| **Precondiciones** | Ejercicio creado. |
| **Pasos** | 1. Enviar respuesta incorrecta. |
| **Resultado esperado** | Respuesta con `tipo: PISTA`. |
| **Estado** | Diseñado |

### CP-PRA-003: Idempotencia

| Campo | Valor |
|---|---|
| **ID** | CP-PRA-003 |
| **Nombre** | Doble envío con misma clave |
| **Requisito** | RF-PRA-004 |
| **Precondiciones** | Ejercicio e intento. |
| **Pasos** | 1. Enviar intento dos veces con misma `claveIdempotencia`. |
| **Resultado esperado** | Un solo registro en base de datos. |
| **Estado** | Diseñado |

### CP-PRA-004: Niveles

| Campo | Valor |
|---|---|
| **ID** | CP-PRA-004 |
| **Nombre** | Subir de nivel tras racha |
| **Requisito** | RF-PRA-005 |
| **Precondiciones** | Nivel inicial configurado. |
| **Pasos** | 1. Enviar 3 aciertos consecutivos. |
| **Resultado esperado** | Nivel aumenta en 1 (hasta máximo 3). |
| **Estado** | Diseñado |
| **Evidencia** | `NivelServiceTest.java` |

### CP-PRO-001: Dashboard de progreso

| Campo | Valor |
|---|---|
| **ID** | CP-PRO-001 |
| **Nombre** | Ver progreso del estudiante |
| **Requisito** | RF-PRO-001 |
| **Estado** | Diseñado |

### CP-ANA-001: Dashboard docente

| Campo | Valor |
|---|---|
| **ID** | CP-ANA-001 |
| **Nombre** | Ver desempeño grupal |
| **Requisito** | RF-ANA-001 |
| **Estado** | Diseñado |

### CP-ADM-001: Gestión de usuarios

| Campo | Valor |
|---|---|
| **ID** | CP-ADM-001 |
| **Nombre** | Administrador desactiva usuario |
| **Requisito** | RF-ADM-001 |
| **Estado** | Diseñado |

---

## 7.5. Pruebas por módulo

### 7.5.1 Autenticación

- Login correcto e incorrecto.
- Registro con correo inválido, contraseña débil, correo duplicado.
- Token vencido o alterado.
- Acceso a `/api/auth/me` sin token.

### 7.5.2 Diagnóstico

- Inicio de intento, retomar intento existente.
- Responder ítems, cambiar respuesta.
- Finalizar manualmente y por tiempo.
- Calcular puntaje en casos: todo correcto, todo incorrecto, parcial.
- Categorización en los bordes de umbrales.

### 7.5.3 Práctica

- Selección de ejercicio por asignatura.
- Respuesta correcta e incorrecta.
- Retroalimentación en primer y segundo intento.
- Idempotencia.
- Subida y bajada de nivel.
- Límites de nivel 1 y 3.

### 7.5.4 Base de datos

- Migraciones aplican en base limpia.
- Restricciones de integridad (correo único, una opción correcta por ítem).
- Semilla de 24 ítems y 90 ejercicios.

---

## 7.6. Pruebas no funcionales

### 7.6.1 Rendimiento

- **Procedimiento propuesto:** usar k6 o JMeter para ejecutar 30 usuarios concurrentes durante 5 minutos.
- **Endpoints a medir:** `POST /api/auth/login`, `GET /api/practica/siguiente`, `POST /api/diagnosticos/intentos/{id}/finalizar`.
- **Umbral propuesto:** p95 < 1 s.
- **Estado:** Pendiente.

### 7.6.2 Seguridad

- Verificar que `esCorrecta` no aparece en respuestas de diagnóstico y práctica.
- Verificar que endpoints protegidos requieren `@PreAuthorize`.
- Verificar que no hay secrets en `application.properties`.
- **Estado:** Parcialmente verificado por lectura de código.

### 7.6.3 Accesibilidad

- Ejecutar Lighthouse en `/login`, `/registro`, `/estudiante/diagnostico`.
- Umbral propuesto: puntuación ≥ 90.
- **Estado:** Pendiente.

### 7.6.4 Usabilidad

- Aplicar protocolo con 4-6 tareas a estudiantes y docentes piloto.
- Instrumento SUS.
- Umbral: ≥ 68.
- **Estado:** Pendiente.

---

## 7.7. Gestión de defectos

### 7.7.1 Clasificación

| Severidad | Descripción |
|---|---|
| Crítica | Impide el uso de una funcionalidad principal. |
| Alta | Afecta significativamente la funcionalidad o seguridad. |
| Media | Afecta la usabilidad o requiere workaround. |
| Baja | Problema cosmético o de documentación. |

### 7.7.2 Estados

- Nuevo, En análisis, En corrección, En pruebas, Cerrado, Reabierto.

### 7.7.3 Criterios de cierre

- El defecto no se reproduce.
- Se agregó prueba que lo previene.
- Se actualizó la documentación si aplica.

---

## 7.8. Criterios de entrada, salida y aceptación

### 7.8.1 Criterios de entrada

- Ambiente de pruebas configurado.
- Base de datos limpia o con semillas conocidas.
- Código compila y pasa lint.

### 7.8.2 Criterios de salida

- Todos los casos de prueba del sprint ejecutados.
- Sin defectos críticos ni altos abiertos.
- Cobertura de lógica de negocio ≥ 70 %.

### 7.8.3 Criterios de aceptación del producto

- Flujos de estudiante, docente y admin funcionan en staging.
- CI pasa en backend y frontend.
- SUS ≥ 68 y tareas de usabilidad ≥ 80 %.

---

## 7.9. Automatización e integración continua

### 7.9.1 Backend CI

- Archivo: `aprendizaje-usco-backend/.github/workflows/ci.yml`.
- Ejecuta `./mvnw -B -ntp verify` en cada push/PR a `develop` y `main`.
- Requiere Docker para Testcontainers.
- Estado: Configurado.

### 7.9.2 Frontend CI

- Archivo: `aprendizaje-usco-frontend/.github/workflows/ci.yml`.
- Ejecuta `npm ci`, `npm run lint`, `npm test`, `npm run build`.
- Estado: Configurado y verificado localmente.

### 7.9.3 Oportunidades de automatización

- Agregar pruebas end-to-end con Playwright o Cypress.
- Agregar análisis de cobertura con JaCoCo y cobertura de Vitest.
- Agregar pruebas de rendimiento en CI.

---

## 7.10. Matriz de cobertura

| Requisito | Historia | Caso de prueba | Tipo de prueba | Ejecutado | Resultado | Defectos |
|---|---|---|---|---|---|---|
| RF-AUT-001 | HU-AUT-001 | CP-AUT-001 | Integración | No | — | — |
| RF-AUT-002 | HU-AUT-002 | CP-AUT-002 | Integración | No | — | — |
| RF-AUT-003 | — | CP-AUT-003 | Integración | No | — | — |
| RF-DIA-001 | HU-DIA-001 | CP-DIA-001 | Integración | No | — | — |
| RF-DIA-002 | HU-DIA-001 | CP-DIA-002 | Integración | No | — | — |
| RF-DIA-003 | HU-DIA-001 | CP-DIA-003 | Integración | No | — | — |
| RF-DIA-004 | HU-DIA-001 | CP-DIA-004 | Unitaria | No | — | — |
| RF-DIA-005 | HU-DIA-001 | CP-DIA-005 | Unitaria | No | — | — |
| RF-PRA-001 | HU-PRA-001 | CP-PRA-001 | Integración | No | — | — |
| RF-PRA-002 | HU-PRA-001 | CP-PRA-002 | Componente | No | — | — |
| RF-PRA-003 | HU-PRA-001 | CP-PRA-003 | Integración | No | — | — |
| RF-PRA-004 | HU-PRA-001 | CP-PRA-004 | Unitaria | No | — | — |
| RF-PRA-005 | HU-PRA-001 | CP-PRA-004 | Unitaria | No | — | — |
| RF-PRO-001 | HU-PRO-001 | CP-PRO-001 | — | No | — | — |
| RF-ANA-001 | HU-ANA-001 | CP-ANA-001 | — | No | — | — |
| RF-ADM-001 | HU-ADM-001 | CP-ADM-001 | — | No | — | — |

### Pruebas ejecutadas localmente (frontend)

| Prueba | Resultado | Evidencia |
|---|---|---|
| `npm run lint` | Aprobado | Sin errores. |
| `npm test` | Aprobado | 27 tests en 4 archivos. |
| `npm run build` | Aprobado | Build generado en `dist/` con advertencia de tamaño de chunk. |

### Pruebas ejecutadas localmente (backend)

| Prueba | Resultado | Evidencia |
|---|---|---|
| `./mvnw.cmd compile` | Aprobado | BUILD SUCCESS. |
| `./mvnw.cmd test` | No ejecutado | Docker daemon no está disponible en el entorno de auditoría; Testcontainers requiere Docker. |

---

## 7.11. Informe del estado de calidad

### 7.11.1 Pruebas existentes

- Backend: clases de prueba identificadas para autenticación, seguridad, diagnóstico, práctica, esquema de base de datos y excepciones.
- Frontend: 27 tests distribuidos en `LoginPage.test.jsx`, `RegistroPage.test.jsx`, `ProtectedRoute.test.jsx` y `AuthContext.test.jsx`.

### 7.11.2 Pruebas ejecutadas y verificables

- Frontend: lint, tests y build ejecutados localmente con resultados positivos.
- Backend: compilación exitosa.

### 7.11.3 Pruebas pendientes

- Pruebas de integración del backend con Testcontainers.
- Pruebas end-to-end.
- Pruebas de rendimiento.
- Pruebas de usabilidad con usuarios piloto.
- Pruebas de accesibilidad con Lighthouse.

### 7.11.4 Defectos confirmados

Ninguno observado en este ejercicio de documentación.

### 7.11.5 Riesgos de calidad

- Las pantallas provisionales del frontend no permiten validar flujos completos.
- La dependencia de Docker dificulta la ejecución local de pruebas de backend.

### 7.11.6 Recomendaciones priorizadas

1. Completar las interfaces de diagnóstico y práctica para poder ejecutar pruebas end-to-end.
2. Implementar endpoints de progreso y analítica.
3. Ejecutar el backend CI localmente con Docker Desktop activo.
4. Agregar cobertura de código y pruebas de componentes para las pantallas principales.
5. Programar la validación con usuarios piloto con protocolo y SUS.

---

*Fin del Aseguramiento de Calidad y Plan de Pruebas*
