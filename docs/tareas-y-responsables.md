# Tareas y responsables — Aprendizaje USCO

Qué hay que hacer en cada tarea del backlog del MVP (backlog v3, importado a Jira), con su descripción, sus criterios de aceptación y su responsable. **Las fechas y el estado de cada tarea se consultan en Jira**; este archivo es la referencia de alcance desde el repositorio.

- Los números (`#`) son los del backlog v3; sirven para ubicar la tarjeta en Jira.
- Cada subtarea tiene el mismo responsable que su tarea, salvo que se indique otra cosa.
- Una tarea está terminada cuando cumple **todos** sus criterios de aceptación y los de sus subtareas.
- Si una asignación o un alcance cambia en Jira, actualicen también este archivo en el mismo PR en que se trabaje la tarea.

Fecha de entrega del MVP: jueves 12 de noviembre de 2026 (sustentación el 15 de noviembre).

## Contenido

- [Equipo](#equipo)
- [Sprints](#sprints)
- [Épicas](#épicas)
- [Sprint 1](#sprint-1)
- [Sprint 2](#sprint-2)
- [Sprint 3](#sprint-3)
- [Sprint 4](#sprint-4)

## Equipo

| Integrante | Tareas e historias asignadas |
|---|---|
| Jorel Vargas | 18 |
| Juan David Polanco | 11 |
| Nicolás Pérez | 10 |
| Fernando Pérez | 10 |

El conteo de Jorel incluye la tarea nueva #150 (registro por invitación), que aún hay que crear en Jira.

## Sprints

| Sprint | Periodo | Objetivo |
|---|---|---|
| Sprint 1 | 28 sep – 8 oct 2026 | Dejar lista la infraestructura del proyecto (repositorio, entornos, base de datos, autenticación) e iniciar el módulo de diagnóstico. |
| Sprint 2 | 9 oct – 20 oct 2026 | Completar el módulo de diagnóstico (aplicación, calificación y categorización), iniciar el motor de práctica adaptativa y definir el protocolo de usabilidad. |
| Sprint 3 | 21 oct – 1 nov 2026 | Terminar la práctica adaptativa con retroalimentación inmediata, construir los tableros de seguimiento y analítica, e iniciar la gestión de contenidos. |
| Sprint 4 | 2 nov – 12 nov 2026 | Cerrar administración, integrar todos los módulos, ejecutar pruebas de seguridad, concurrencia y validación con usuarios, y empaquetar la entrega del MVP. |

## Épicas

| Épica | Nombre |
|---|---|
| EPIC-1 | Configuración base, infraestructura y autenticación |
| EPIC-2 | Módulo de diagnóstico |
| EPIC-3 | Práctica adaptativa y retroalimentación |
| EPIC-4 | Seguimiento del progreso y analítica de aprendizaje |
| EPIC-5 | Gestión de contenidos y administración |
| EPIC-6 | Integración, pruebas y validación del MVP |
| EPIC-7 | Calidad: usabilidad, rendimiento y seguridad |

## Sprint 1

28 sep – 8 oct 2026. Dejar lista la infraestructura del proyecto (repositorio, entornos, base de datos, autenticación) e iniciar el módulo de diagnóstico.

| # | Tarea | Tipo | Épica | Responsable |
|---|---|---|---|---|
| [1](#1) | Crear repositorio y flujo de trabajo Git | Tarea | EPIC-1 | Jorel Vargas |
| [2](#2) | Documentación base de los repositorios | Tarea | EPIC-1 | Jorel Vargas |
| [3](#3) | Configurar proyecto backend (Spring Boot) | Tarea | EPIC-1 | Jorel Vargas |
| [9](#9) | Configurar proyecto frontend (React) | Tarea | EPIC-1 | Jorel Vargas |
| [14](#14) | Diseñar y crear el esquema inicial de base de datos | Tarea | EPIC-1 | Jorel Vargas |
| [20](#20) | Implementar registro e inicio de sesión de usuarios | Historia | EPIC-1 | Nicolás Pérez |
| [28](#28) | Implementar control de acceso por rol | Historia | EPIC-1 | Fernando Pérez |
| [33](#33) | Configurar entorno de despliegue de prueba (staging) | Tarea | EPIC-1 | Jorel Vargas |
| [38](#38) | Configurar integración continua básica | Tarea | EPIC-1 | Jorel Vargas |
| [43](#43) | Modelar entidades del diagnóstico | Tarea | EPIC-2 | Jorel Vargas |
| [47](#47) | Migrar el banco de ítems de la prueba diagnóstica | Tarea | EPIC-2 | Jorel Vargas |

<a id="1"></a>

### #1 · Crear repositorio y flujo de trabajo Git

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** DevOps

**Descripción.** Organización de GitHub, repositorios aprendizaje-usco-backend y aprendizaje-usco-frontend, ramas main y develop, develop como rama por defecto, rulesets (PR obligatorio, 1 aprobación, sin force push) e invitación del equipo.

**Criterios de aceptación**

- Ambos repositorios existen en la organización con main y develop.
- No se puede hacer push directo a main ni a develop.
- Los cuatro integrantes tienen permiso de escritura.

<a id="2"></a>

### #2 · Documentación base de los repositorios

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** Docs

**Descripción.** README.md de backend y frontend (instalación, comandos, variables de entorno, estructura, responsables, problemas frecuentes), CONTRIBUTING.md común y plantilla de pull request en .github/.

**Criterios de aceptación**

- Un integrante nuevo puede levantar ambos proyectos siguiendo solo los README.
- La plantilla de PR aparece al abrir un pull request.

<a id="3"></a>

### #3 · Configurar proyecto backend (Spring Boot)

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** Backend

**Descripción.** Proyecto Spring Boot 4.1 / Java 21 generado con Initializr, organizado por módulo funcional con capas internas (controller, service, repository, dto, entity), una configuración de ejecución externalizada por entorno y perfil `test` para pruebas.

**Criterios de aceptación**

- ./mvnw spring-boot:run levanta la API y /actuator/health responde UP.
- Swagger UI carga en /swagger-ui.html.
- Cualquier error de la API responde con el formato JSON estándar.

**Subtareas**

- **#4 Generar el proyecto con Initializr y sus dependencias.** Spring Web, Data JPA, PostgreSQL, Flyway, Security, OAuth2 Resource Server, Validation, Lombok, DevTools, Actuator, Docker Compose, Testcontainers + springdoc 3.x.
  - Criterio: Compila y la prueba generada pasa.
- **#5 Configurar propiedades, compose.yaml y application.yml.** application.yml con configuración común y valores externalizados, application-test.yml, PostgreSQL 17 en compose.yaml, ddl-auto: validate.
  - Criterio: La app conecta a PostgreSQL local con la configuración de `.env`.
- **#6 Crear estructura de paquetes y SecurityConfig mínima.** Paquetes config, security, common, usuario, diagnostico, practica, contenido, progreso, analitica. SecurityConfig con rutas públicas (/api/auth/\*\*, health, Swagger).
  - Criterio: Health y Swagger accesibles sin token; el resto devuelve 401.
- **#7 Manejador global de errores y formato estándar.** En common/: clase ErrorResponse (timestamp, status, message, path), @RestControllerAdvice que traduce MethodArgumentNotValidException (400, con los campos inválidos), EntityNotFound/ResourceNotFound propia (404), DataIntegrityViolation (409) y Exception genérica (500 sin exponer la traza). Excepciones propias: RecursoNoEncontradoException, ConflictoException, ReglaNegocioException.
  - Criterio: Un DTO inválido devuelve 400 con la lista de campos y mensajes.
  - Criterio: Un id inexistente devuelve 404 con el formato estándar.
  - Criterio: Un error inesperado devuelve 500 sin stack trace en la respuesta.
- **#8 Configurar CORS y seguridad Bearer en Swagger.** CorsConfigurationSource leyendo app.cors.allowed-origins (métodos GET, POST, PUT, PATCH, DELETE; header Authorization). En OpenAPI, esquema de seguridad bearerAuth (JWT) para poder probar endpoints protegidos con el botón Authorize.
  - Criterio: El frontend en localhost:5173 consume la API sin errores de CORS.
  - Criterio: Desde Swagger se puede pegar un token y llamar un endpoint protegido.

<a id="9"></a>

### #9 · Configurar proyecto frontend (React)

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** Frontend

> Nota de coordinación: Jorel creó el esqueleto; Nicolás completa enrutador, layouts y tema.

**Descripción.** Proyecto Vite 8 + React en JavaScript con react-router, axios, MUI, recharts, KaTeX, react-hook-form y zod; estructura por features; enrutador con todas las rutas del README; layouts por rol y tema institucional.

**Criterios de aceptación**

- npm run dev, lint y build sin errores.
- Todas las rutas del README existen (aunque sea con página provisional) y cada rol ve su propio menú.
- La interfaz usa los colores USCO desde el tema, no colores sueltos.

**Subtareas**

- **#10 Crear proyecto con Vite, librerías, estructura, .env y cliente axios.** Plantilla react, dependencias de la guía, carpetas api/ routes/ context/ layouts/ features/ components/ theme/ utils/, VITE_API_URL, api/client.js con interceptor de token, Prettier.
  - Criterio: El proyecto corre en localhost:5173.
- **#11 Tema de MUI con los colores USCO.** theme/theme.js: palette.primary con el rojo/vino institucional y palette.secondary tomados del prototipo HTML; tipografía, border radius y breakpoints; CssBaseline. Verificar contraste de texto sobre primary (mínimo 4.5:1, WCAG AA) con un verificador de contraste.
  - Criterio: ThemeProvider envuelve la app en main.jsx.
  - Criterio: Botones, AppBar y enlaces toman el color del tema.
  - Criterio: El contraste texto/fondo de los colores principales cumple 4.5:1.
- **#12 Enrutador con rutas públicas, protegidas y páginas provisionales.** routes/AppRouter.jsx con las rutas del README (/login, /estudiante/\*, /docente/\*, /admin, /no-autorizado, 404). Cada ruta protegida envuelta en ProtectedRoute con su rol. Páginas provisionales (título + "en construcción") por pantalla, para que cada integrante reemplace la suya. Página NoAutorizado y NotFound. "/" redirige según el rol o a /login.
  - Criterio: Navegar a una ruta sin sesión lleva a /login.
  - Criterio: Un ESTUDIANTE que entra a /docente ve /no-autorizado.
  - Criterio: Una URL inexistente muestra la página 404.
- **#13 Layouts por rol con navegación responsive.** layouts/EstudianteLayout, DocenteLayout y AdminLayout: AppBar con nombre del usuario y cerrar sesión, menú lateral con las opciones del rol (Drawer permanente en escritorio, temporal en móvil) y \<Outlet/\> para el contenido. Accesibilidad: landmarks (header, nav, main), enlace "Saltar al contenido", foco visible, menú operable con teclado.
  - Criterio: Cada rol ve solo sus opciones de menú.
  - Criterio: En 375 px de ancho el menú se abre como cajón y no hay scroll horizontal.
  - Criterio: Todo el menú se recorre con Tab y se activa con Enter.

<a id="14"></a>

### #14 · Diseñar y crear el esquema inicial de base de datos

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** Backend, BD

**Descripción.** Modelo físico y migración Flyway de las tablas base (rol, usuario, competencia), a partir del modelo conceptual del capítulo 4, más sus entidades JPA y los datos semilla necesarios para desarrollar.

**Criterios de aceptación**

- Al arrancar en limpio, Flyway crea las tablas y la app arranca con ddl-auto: validate sin errores.
- Existen los 3 roles, las 3 competencias y un usuario de prueba por rol.
- La prueba de integración del esquema pasa en CI.

**Subtareas**

- **#15 Definir el modelo físico de rol, usuario y competencia.** Documentar en la tarjeta: rol(id, nombre UNIQUE: ESTUDIANTE, DOCENTE, ADMIN); usuario(id, nombres, apellidos, correo UNIQUE, password_hash, rol_id FK, programa_academico NULL, estado ACTIVO/INACTIVO, creado_en, actualizado_en); competencia(id, codigo UNIQUE: ALGEBRAICA, LOGICA, FUNCIONAL, nombre, descripcion). Tipos: BIGINT GENERATED ALWAYS AS IDENTITY, VARCHAR con longitudes, TIMESTAMPTZ. Convención: tablas y columnas en snake_case singular.
  - Criterio: El modelo está en la tarjeta y lo revisó Jorel antes de escribir la migración.
- **#16 Escribir la migración V1 (rol, usuario, competencia).** db/migration/V\<yyyyMMddHHmm\>__esquema_inicial.sql con CREATE TABLE, PK, FK usuario.rol_id → rol.id, UNIQUE en correo y códigos, CHECK en estado, índice en usuario.correo.
  - Criterio: La migración corre en una base vacía sin errores.
  - Criterio: Insertar dos usuarios con el mismo correo falla.
- **#17 Entidades JPA y repositorios de rol, usuario y competencia.** Rol, Usuario (@ManyToOne a Rol), Competencia en sus módulos; RolRepository, UsuarioRepository (findByCorreo, existsByCorreo), CompetenciaRepository (findByCodigo). Enum EstadoUsuario.
  - Criterio: La app arranca con ddl-auto: validate (el mapeo coincide con la migración).
- **#18 Datos semilla: roles, competencias y usuarios de prueba.** Migración de datos con los 3 roles y 3 competencias (se requieren en todos los entornos). Usuarios de prueba (estudiante, docente, admin) con contraseña en BCrypt en `db/dev-data`, que solo se agrega a `spring.flyway.locations` en `.env` local. Completar la tabla de usuarios de prueba del README.
  - Criterio: Con la configuración local se puede iniciar sesión con los 3 usuarios de prueba.
  - Criterio: En pruebas y producción no se cargan usuarios de prueba.
- **#19 Prueba de integración del esquema.** Prueba con Testcontainers que arranca PostgreSQL, aplica las migraciones y verifica guardar/leer un usuario con su rol.
  - Criterio: ./mvnw test pasa localmente y en CI.

<a id="20"></a>

### #20 · Implementar registro e inicio de sesión de usuarios

**Responsable:** Nicolás Pérez · **Tipo:** Historia · **Épica:** EPIC-1 · **Área:** Backend, Frontend, Seguridad · **Requisito:** RF-01

**Descripción.** Como usuario quiero iniciar sesión con mi correo y contraseña para acceder a las funciones de mi rol. Autenticación stateless con JWT firmado (HS256) emitido por el backend con Spring Security + OAuth2 Resource Server (Nimbus). Registro público solo para estudiantes; docentes y administradores los crea el administrador (confirmar con el equipo).

**Criterios de aceptación**

- POST /api/auth/login con credenciales válidas devuelve 200 con token, expiración y datos del usuario (id, nombre, rol).
- Credenciales inválidas o usuario INACTIVO devuelven 401 con mensaje genérico (no revela si el correo existe).
- Las contraseñas se guardan con BCrypt; nunca viajan de vuelta en ninguna respuesta.
- Desde el frontend, el login redirige al inicio del rol correspondiente.

**Subtareas**

- **#21 Publicar el contrato de /api/auth en la tarjeta.** Primera cosa del día para desbloquear a Fernando. POST /api/auth/login  body {"correo","password"} → 200 {"token","tipo":"Bearer","expiraEn","usuario":{"id","nombres","rol"}}; 401 {formato estándar}. POST /api/auth/registro (estudiante) → 201. GET /api/auth/me (token) → 200 usuario. Claims del token: sub (id), correo, rol, iat, exp.
  - Criterio: Fernando confirma que puede simular las respuestas con ese contrato.
- **#22 PasswordEncoder y UserDetailsService por correo.** Bean BCryptPasswordEncoder; UserDetailsService que busca por correo y rechaza usuarios INACTIVOS; AuthenticationManager (DaoAuthenticationProvider).
  - Criterio: Una contraseña correcta autentica y una incorrecta lanza BadCredentials.
- **#23 Servicio JWT: emisión y validación del token.** JwtEncoder/JwtDecoder de Nimbus con clave simétrica desde app.jwt.secret (mínimo 32 caracteres), expiración desde app.jwt.expiration-minutes, claims sub, correo, rol. Configurar oauth2ResourceServer().jwt() en SecurityConfig.
  - Criterio: Un token emitido es aceptado; uno alterado o vencido responde 401.
- **#24 Endpoints /api/auth/login, /registro y /me.** AuthController + AuthService con DTO validados (LoginRequest, RegistroRequest, AuthResponse, UsuarioResponse). Registro asigna rol ESTUDIANTE y valida correo único (409 si existe).
  - Criterio: Los tres endpoints cumplen el contrato publicado y aparecen en Swagger.
- **#25 Pruebas del login.** Pruebas de servicio y de controlador (MockMvc): login correcto, contraseña errada, usuario inactivo, registro con correo repetido, /me sin token.
  - Criterio: Las 5 pruebas pasan en CI.
- **#26 Cliente authApi con datos simulados.** api/authApi.js con login(), registro() y me(). Mientras el endpoint no esté en develop, responde con datos simulados (usuarios de prueba por rol y un token falso con la misma forma del contrato) activados con VITE_USE_MOCKS=true.
  - Criterio: Cambiar VITE_USE_MOCKS a false usa el backend real sin tocar componentes.
- **#27 Pantalla de inicio de sesión.** features/auth/LoginPage.jsx según el prototipo: correo y contraseña con react-hook-form + zod (correo válido, contraseña requerida), botón con estado de carga, mensaje de error del servidor, mostrar/ocultar contraseña. Accesible: labels visibles, errores asociados con aria-describedby, foco al primer error, envío con Enter. Al iniciar sesión redirige a /estudiante, /docente o /admin según el rol.
  - Criterio: Con credenciales de prueba entra al inicio del rol correcto.
  - Criterio: Con credenciales inválidas muestra el error sin recargar la página.
  - Criterio: Se puede completar solo con teclado.
  - Criterio: Se ve bien en 375 px de ancho.

<a id="28"></a>

### #28 · Implementar control de acceso por rol

**Responsable:** Fernando Pérez · **Tipo:** Historia · **Épica:** EPIC-1 · **Área:** Backend, Frontend, Seguridad · **Requisito:** RF-01, RNF-03

**Descripción.** Como plataforma debo restringir endpoints (backend) y vistas (frontend) según el rol ESTUDIANTE, DOCENTE o ADMIN. El backend es la barrera real; el frontend solo evita navegar a lo que no corresponde.

**Criterios de aceptación**

- Un token de ESTUDIANTE que llama un endpoint de DOCENTE recibe 403 con el formato estándar.
- Una petición sin token recibe 401.
- En el frontend, cada rol solo puede navegar a sus rutas y al vencer el token vuelve al login.

**Subtareas**

- **#29 Autorización por rol en el backend.** JwtAuthenticationConverter que convierte el claim "rol" en ROLE_\<rol\>; @EnableMethodSecurity y @PreAuthorize en controladores; AuthenticationEntryPoint (401) y AccessDeniedHandler (403) que responden con el formato estándar.
  - Criterio: Endpoint de prueba /api/ping/{rol} responde 200 al rol correcto y 403 a los demás.
- **#30 AuthContext: sesión, rol y cierre de sesión.** context/AuthContext.jsx con user, rol, login(), logout(), isAuthenticated. Lee el token con jwt-decode al cargar la app, descarta tokens vencidos y lo guarda en localStorage.
  - Criterio: Al recargar la página la sesión se mantiene si el token sigue vigente.
  - Criterio: logout limpia el token y lleva a /login.
- **#31 ProtectedRoute e interceptor de 401.** Conectar ProtectedRoute con AuthContext (sin sesión → /login guardando la ruta de origen; rol distinto → /no-autorizado). Interceptor de respuesta en api/client.js: ante 401 hace logout y redirige al login.
  - Criterio: Tras iniciar sesión, vuelve a la ruta que el usuario intentaba abrir.
  - Criterio: Un 401 del backend cierra la sesión automáticamente.
- **#32 Pruebas de acceso por rol.** Pruebas MockMvc de 401 y 403 sobre endpoints de cada rol, reutilizables en la tarea de hardening del Sprint 4.
  - Criterio: Pasan en CI.

<a id="33"></a>

### #33 · Configurar entorno de despliegue de prueba (staging)

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** DevOps

**Descripción.** Backend, base de datos y frontend publicados en una URL accesible para el equipo y los usuarios piloto, con variables de entorno separadas y despliegue desde develop.

**Criterios de aceptación**

- El equipo abre la URL de staging e inicia sesión con un usuario de prueba.
- Un merge a develop se refleja en staging (automático o con un paso documentado).
- Ningún secreto está en el repositorio.

**Subtareas**

- **#34 Elegir la plataforma de hosting.** Comparar 2 o 3 opciones gratuitas o con crédito estudiantil para Spring Boot + PostgreSQL y para un sitio estático (límites, suspensión por inactividad, región). Registrar la decisión en la tarjeta.
  - Criterio: Decisión escrita con el motivo.
- **#35 Desplegar backend y PostgreSQL.** Servicio del backend (Dockerfile o build nativo de la plataforma), base PostgreSQL gestionada y variables SPRING_DATASOURCE_*, JWT_SECRET, CORS_ORIGINS.
  - Criterio: /actuator/health de staging responde UP.
- **#36 Desplegar frontend.** Build de Vite con VITE_API_URL de staging; regla de reescritura para que las rutas de React Router no den 404 al recargar.
  - Criterio: Recargar /estudiante en staging no da 404.
- **#37 Documentar URLs y proceso de despliegue.** Sección "Despliegue" en ambos README: URLs, variables, cómo desplegar y quién tiene acceso a la plataforma.
  - Criterio: Cualquier integrante puede redesplegar siguiendo el README.

<a id="38"></a>

### #38 · Configurar integración continua básica

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-1 · **Área:** DevOps, QA

**Descripción.** GitHub Actions que compila y prueba cada PR hacia develop y main en ambos repositorios, y que se exige como check obligatorio antes de fusionar.

**Criterios de aceptación**

- Un PR con una prueba fallando no se puede fusionar.
- El pipeline tarda menos de 10 minutos.

**Subtareas**

- **#39 Workflow del backend.** .github/workflows/ci.yml: Temurin 21, caché de Maven, ./mvnw verify (Testcontainers funciona en los runners de GitHub).
  - Criterio: Corre en cada PR y muestra el resultado en el PR.
- **#40 Workflow del frontend.** Node 24, npm ci, npm run lint y npm run build.
  - Criterio: Corre en cada PR.
- **#41 Checks obligatorios en los rulesets.** Agregar "Require status checks to pass" con los jobs de CI en main y develop de ambos repos (requiere permisos de admin: coordinar con Jorel).
  - Criterio: El botón de merge queda bloqueado si CI falla.
- **#42 Insignia de estado en los README.** Badge del workflow en ambos README.
  - Criterio: El badge refleja el estado de develop.

<a id="43"></a>

### #43 · Modelar entidades del diagnóstico

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-2 · **Área:** Backend, BD

**Descripción.** Tablas y entidades de la prueba diagnóstica, sus ítems y opciones, los intentos de cada estudiante, sus respuestas y el resultado por dimensión, consistentes con el modelo conceptual del capítulo 4.

**Criterios de aceptación**

- La migración corre sin errores y la app arranca con ddl-auto: validate.
- Un estudiante no puede tener dos intentos EN_CURSO de la misma prueba (restricción en BD).
- No se puede registrar dos respuestas para el mismo ítem en el mismo intento.

**Subtareas**

- **#44 Migración de tablas del diagnóstico.** prueba_diagnostica(id, nombre, version, tiempo_limite_min, activa); item(id, prueba_id, numero, enunciado, competencia_id, activo); opcion(id, item_id, letra, texto, es_correcta); intento_diagnostico(id, usuario_id, prueba_id, estado EN_CURSO/FINALIZADO, iniciado_en, finalizado_en, puntaje_total, categoria); respuesta_diagnostico(id, intento_id, item_id, opcion_id, respondida_en, UNIQUE(intento_id, item_id)); resultado_dimension(id, intento_id, competencia_id, aciertos, total, porcentaje). Índice único parcial para un solo intento EN_CURSO por usuario y prueba.
  - Criterio: Las restricciones del criterio de aceptación están en la migración.
- **#45 Entidades JPA y repositorios del diagnóstico.** Entidades y repositorios en diagnostico/entity y diagnostico/repository; enums EstadoIntento y CategoriaDesempeno.
  - Criterio: La app arranca con validate y hay una prueba de repositorio básica.
- **#46 Revisión cruzada del modelo.** Jorel revisa el modelo antes de fusionar, porque el motor adaptativo y los indicadores dependen de él.
  - Criterio: PR aprobado por Jorel.

<a id="47"></a>

### #47 · Migrar el banco de ítems de la prueba diagnóstica

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-2 · **Área:** BD, Contenido

**Descripción.** Cargar los 24 ítems de la Prueba Diagnóstica de Competencias Matemáticas (enunciado, opciones, clave y dimensión) a la base de datos desde un CSV revisado.

**Criterios de aceptación**

- La base contiene exactamente 24 ítems activos, cada uno con una sola opción correcta y una dimensión.
- Las expresiones matemáticas se ven correctamente con KaTeX.
- Los ítems 8 y 17 tienen su clave revisada, y los ítems 1 y 15 una nota sobre su efecto techo (decisiones derivadas del cap. 3).

**Subtareas**

- **#48 Transcribir los 24 ítems a CSV.** docs/datos/items_diagnostico.csv (UTF-8) con columnas: numero, dimension (ALGEBRAICA/LOGICA/FUNCIONAL), enunciado, opcion_a, opcion_b, opcion_c, opcion_d, respuesta_correcta (A-D), observaciones. Fórmulas en notación LaTeX entre $…$ (ej. $x^2-5x+6=0$). Tomar el texto exacto del formulario aplicado.
  - Criterio: 24 filas, sin celdas vacías obligatorias, abre bien en Excel y en el editor.
- **#49 Validar el CSV.** Revisión por un segundo integrante: ortografía, LaTeX que renderiza, una sola clave por ítem, dimensión asignada según el instrumento, claves de los ítems 8 y 17 revisadas contra la fuente.
  - Criterio: Checklist de revisión marcado en la tarjeta.
- **#50 Migración de datos de los ítems.** Convertir el CSV en una migración Flyway de datos (INSERT de prueba_diagnostica versión 1, item y opcion). Mantener el CSV como fuente en el repo.
  - Criterio: En una base limpia quedan cargados los 24 ítems con sus 4 opciones.

## Sprint 2

9 oct – 20 oct 2026. Completar el módulo de diagnóstico (aplicación, calificación y categorización), iniciar el motor de práctica adaptativa y definir el protocolo de usabilidad.

| # | Tarea | Tipo | Épica | Responsable |
|---|---|---|---|---|
| [51](#51) | Desarrollar endpoint de aplicación de la prueba diagnóstica | Historia | EPIC-2 | Jorel Vargas |
| [57](#57) | Implementar cálculo automático del puntaje | Historia | EPIC-2 | Jorel Vargas |
| [61](#61) | Implementar asignación de categoría de desempeño | Historia | EPIC-2 | Jorel Vargas |
| [66](#66) | Construir interfaz de aplicación de la prueba diagnóstica | Historia | EPIC-2 | Nicolás Pérez |
| [73](#73) | Construir pantalla de resultado del diagnóstico | Tarea | EPIC-2 | Nicolás Pérez |
| [77](#77) | Componente MathText para notación matemática | Tarea | EPIC-2 | Nicolás Pérez |
| [78](#78) | Pruebas unitarias e integración del módulo de diagnóstico | Tarea | EPIC-2 | Nicolás Pérez |
| [82](#82) | Diseñar el banco de ejercicios por competencia y nivel | Tarea | EPIC-3 | Juan David Polanco |
| [87](#87) | Modelar entidades de ejercicios, intentos y retroalimentación | Tarea | EPIC-3 | Juan David Polanco |
| [90](#90) | Implementar el motor de selección adaptativa de ejercicios | Historia | EPIC-3 | Juan David Polanco |
| [95](#95) | Implementar la lógica de niveles de dificultad | Tarea | EPIC-3 | Juan David Polanco |
| [99](#99) | Modelar grupos y asociación docente–estudiante | Tarea | EPIC-4 | Fernando Pérez |
| [103](#103) | Definir el protocolo y umbral de usabilidad | Tarea | EPIC-7 | Fernando Pérez |

<a id="51"></a>

### #51 · Desarrollar endpoint de aplicación de la prueba diagnóstica

**Responsable:** Jorel Vargas · **Tipo:** Historia · **Épica:** EPIC-2 · **Área:** Backend · **Requisito:** RF-02

**Descripción.** Como estudiante quiero presentar la prueba diagnóstica, con mis respuestas guardadas a medida que avanzo, y poder retomarla si se interrumpe mi sesión.

**Criterios de aceptación**

- Un estudiante puede iniciar, responder, retomar y finalizar la prueba.
- La respuesta correcta nunca viaja al frontend antes de finalizar.
- Responder dos veces el mismo ítem actualiza la respuesta (no la duplica).
- Solo el rol ESTUDIANTE puede usar estos endpoints.

**Subtareas**

- **#52 Contrato de los endpoints del diagnóstico.** POST /api/diagnosticos/intentos (inicia o devuelve el EN_CURSO), GET /api/diagnosticos/intentos/{id} (ítems sin clave + respuestas ya dadas + tiempo restante), PUT /api/diagnosticos/intentos/{id}/respuestas/{itemId} {opcionId}, POST /api/diagnosticos/intentos/{id}/finalizar. Publicarlo en la tarjeta el primer día del sprint.
  - Criterio: Nicolás confirma el contrato.
- **#53 Iniciar o retomar intento.** Servicio que crea el intento EN_CURSO o devuelve el existente; valida que el intento sea del usuario autenticado.
  - Criterio: Un segundo POST no crea otro intento.
- **#54 Entregar ítems sin clave.** DTO ItemDiagnosticoResponse (numero, enunciado, opciones sin es_correcta, dimensión) en el orden definido.
  - Criterio: Ninguna respuesta del endpoint contiene es_correcta.
- **#55 Registrar y actualizar respuestas.** Upsert por (intento, ítem) con marca de tiempo; rechaza intentos FINALIZADOS (409) o de otro usuario (403).
  - Criterio: Pruebas de upsert, intento finalizado y usuario ajeno.
- **#56 Finalizar intento y control de tiempo.** Finaliza manualmente o al vencer tiempo_limite_min; al finalizar dispara el cálculo de puntaje.
  - Criterio: Un intento vencido no acepta respuestas.

<a id="57"></a>

### #57 · Implementar cálculo automático del puntaje

**Responsable:** Jorel Vargas · **Tipo:** Historia · **Épica:** EPIC-2 · **Área:** Backend · **Requisito:** RF-03

**Descripción.** Al finalizar la prueba, calcular el puntaje total (0 a 24) y el puntaje por dimensión (algebraica, lógica, funcional) y guardarlos.

**Criterios de aceptación**

- El puntaje total y por dimensión coincide con un cálculo manual en al menos 3 casos de prueba.
- Los ítems sin responder cuentan como incorrectos.
- El resultado queda guardado en intento_diagnostico y resultado_dimension.

**Subtareas**

- **#58 Servicio de calificación.** CalificacionService: compara cada respuesta con la opción correcta (1/0) y agrega por competencia.
  - Criterio: Casos: todo correcto, todo incorrecto, parcial con ítems vacíos.
- **#59 Persistir resultados por dimensión.** Guardar puntaje_total y filas de resultado_dimension en la misma transacción que la finalización.
  - Criterio: Si falla el cálculo no queda un intento FINALIZADO sin resultado.
- **#60 Pruebas del cálculo.** Pruebas unitarias con los casos del criterio.
  - Criterio: Pasan en CI.

<a id="61"></a>

### #61 · Implementar asignación de categoría de desempeño

**Responsable:** Jorel Vargas · **Tipo:** Historia · **Épica:** EPIC-2 · **Área:** Backend · **Requisito:** RF-04

**Descripción.** Clasificar al estudiante (bajo / medio / alto) en total y por dimensión según umbrales acordados, para orientar la práctica adaptativa.

**Criterios de aceptación**

- La categoría se asigna automáticamente al finalizar.
- Los umbrales se cambian por configuración, sin tocar código.
- Los umbrales usados están documentados en el informe.

**Subtareas**

- **#62 Acordar los umbrales con el equipo.** Proponer cortes (ej. por porcentaje de aciertos) usando la distribución de los 185 resultados del diagnóstico aplicado; validar con la docente si es posible.
  - Criterio: Umbrales escritos en la tarjeta con su justificación.
- **#63 Reglas configurables de categorización.** Umbrales en application.yml (app.diagnostico.umbrales) o en tabla; CategorizacionService puro y probado.
  - Criterio: Cambiar un umbral cambia la categoría sin recompilar lógica.
- **#64 Exponer la categoría en el resultado.** Incluir categoría total y por dimensión en GET /api/diagnosticos/intentos/{id}/resultado.
  - Criterio: El endpoint devuelve puntajes y categorías.
- **#65 Pruebas de categorización.** Casos en los bordes de cada umbral.
  - Criterio: Pasan en CI.

<a id="66"></a>

### #66 · Construir interfaz de aplicación de la prueba diagnóstica

**Responsable:** Nicolás Pérez · **Tipo:** Historia · **Épica:** EPIC-2 · **Área:** Frontend · **Requisito:** RF-02

**Descripción.** Como estudiante quiero responder la prueba ítem por ítem, ver mi avance y el tiempo restante, sin perder respuestas si se cae la conexión.

**Criterios de aceptación**

- Se muestran los 24 ítems uno a uno con su notación matemática correcta.
- Cada respuesta se guarda al seleccionarla (indicador "guardado").
- Al recargar la página se retoma en el ítem donde iba.
- Se puede responder solo con teclado y funciona en móvil.

**Subtareas**

- **#67 Cliente diagnosticoApi.** api/diagnosticoApi.js con iniciar, obtener, responder, finalizar y resultado (con datos simulados mientras tanto).
  - Criterio: Mismo contrato que el backend.
- **#68 Pantalla de instrucciones e inicio.** Explica número de ítems, tiempo y dimensiones; botón Iniciar o Continuar según el estado.
  - Criterio: Si hay un intento EN_CURSO muestra "Continuar".
- **#69 Componente de ítem con opciones accesibles.** Enunciado con MathText, opciones como RadioGroup de MUI (labels asociados, navegación con flechas), estado guardando/guardado/error.
  - Criterio: Lector de pantalla anuncia enunciado y opciones.
- **#70 Temporizador y barra de progreso.** Tiempo restante (sincronizado con el servidor al cargar), aviso a los 5 minutos, progreso x/24 con aria-valuenow.
  - Criterio: Al llegar a 0 se finaliza la prueba.
- **#71 Navegación entre ítems y reanudación.** Anterior/Siguiente, mapa de ítems respondidos, reanudar en el primer ítem sin responder, reintento si falla el guardado.
  - Criterio: Al recargar se conserva el avance.
- **#72 Confirmación de finalizar.** Diálogo que avisa cuántos ítems quedan sin responder antes de finalizar.
  - Criterio: No se puede finalizar por error con un solo clic.

<a id="73"></a>

### #73 · Construir pantalla de resultado del diagnóstico

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-2 · **Área:** Frontend · **Requisito:** RF-02, RF-04

**Descripción.** Vista donde el estudiante consulta su puntaje total, su categoría y las dimensiones en las que necesita refuerzo, con acceso directo a la práctica.

**Criterios de aceptación**

- Muestra puntaje total, categoría y porcentaje por dimensión.
- El gráfico tiene alternativa en texto o tabla para lectores de pantalla.
- El botón "Empezar a practicar" lleva a la práctica.

**Subtareas**

- **#74 Resumen de puntaje y categoría.** Tarjetas con puntaje x/24 y categoría con color e icono (no solo color).
  - Criterio: Legible en móvil.
- **#75 Gráfico por dimensión.** Barras con recharts por dimensión + tabla accesible equivalente.
  - Criterio: Coincide con los datos del endpoint.
- **#76 Recomendaciones y llamado a practicar.** Texto según las dimensiones con categoría baja y botón a /estudiante/practica.
  - Criterio: Aparecen las dimensiones a reforzar.

<a id="77"></a>

### #77 · Componente MathText para notación matemática

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-2 · **Área:** Frontend · **Requisito:** RF-06, RNF-05

**Descripción.** Componente reutilizable (components/MathText.jsx) que renderiza texto mixto con expresiones LaTeX entre $…$ usando KaTeX, para ítems, ejercicios y retroalimentación.

**Criterios de aceptación**

- Renderiza fracciones, potencias, raíces y funciones de los 24 ítems sin errores.
- Una expresión mal escrita muestra el texto original en lugar de romper la página.
- Genera salida accesible (MathML de KaTeX) para lectores de pantalla.

<a id="78"></a>

### #78 · Pruebas unitarias e integración del módulo de diagnóstico

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-2 · **Área:** QA

**Descripción.** Pruebas automatizadas del flujo completo del diagnóstico y verificación manual de sus criterios de aceptación.

**Criterios de aceptación**

- Cobertura de las clases de servicio del diagnóstico ≥ 70 %.
- CA-02, CA-03 y CA-04 verificados y marcados en la matriz.

**Subtareas**

- **#79 Prueba de integración del flujo completo.** Testcontainers + MockMvc: login estudiante → iniciar → responder 24 → finalizar → resultado.
  - Criterio: Pasa en CI.
- **#80 Casos borde.** Intento vencido, intento ajeno, respuesta a ítem inexistente, finalizar dos veces.
  - Criterio: Cada caso devuelve el código esperado.
- **#81 Verificación manual de CA-02 a CA-04.** Ejecutar en staging con el usuario de prueba y dejar evidencia (capturas).
  - Criterio: Evidencia adjunta en la tarjeta.

<a id="82"></a>

### #82 · Diseñar el banco de ejercicios por competencia y nivel

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-3 · **Área:** Contenido · **Requisito:** RF-14

**Descripción.** Catálogo de ejercicios de práctica por dimensión (algebraica, lógica, funcional) y nivel (1 básico, 2 intermedio, 3 avanzado), con su retroalimentación, listo para cargarse a la base.

**Criterios de aceptación**

- Al menos 5 ejercicios por combinación dimensión × nivel (45 en total) para que el motor tenga variedad.
- Cada ejercicio tiene respuesta correcta, pista y explicación.
- Los criterios de nivel están documentados.

**Subtareas**

- **#83 Plantilla CSV de ejercicios.** Columnas: codigo, dimension, nivel, tipo (OPCION_MULTIPLE/NUMERICA), enunciado, opciones, respuesta, pista, explicacion, error_comun.
  - Criterio: Plantilla revisada por Juan David (coincide con el modelo).
- **#84 Criterios de dificultad.** Definir qué hace a un ejercicio nivel 1, 2 o 3 (pasos, conceptos previos) con ejemplos.
  - Criterio: Documento corto en docs/.
- **#85 Redactar y cargar los ejercicios.** Completar el CSV (se pueden adaptar de las guías de los docentes y del curso).
  - Criterio: 45 ejercicios o más en el CSV.
- **#86 Revisión con un docente.** Enviar una muestra al docente para validar nivel y corrección.
  - Criterio: Observaciones incorporadas.

<a id="87"></a>

### #87 · Modelar entidades de ejercicios, intentos y retroalimentación

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-3 · **Área:** Backend, BD

**Descripción.** Tablas y entidades de contenido, ejercicio, opción, intento de práctica, retroalimentación y nivel actual del estudiante por competencia.

**Criterios de aceptación**

- Migración aplicada y app arranca con validate.
- Cada intento queda ligado a estudiante, ejercicio, fecha, resultado y tiempo empleado.

**Subtareas**

- **#88 Migración de tablas de práctica.** contenido(id, competencia_id, titulo, descripcion, activo, creado_por); ejercicio(id, contenido_id, competencia_id, nivel 1-3, tipo, enunciado, respuesta_numerica NULL, pista, explicacion, activo); opcion_ejercicio; intento_practica(id, usuario_id, ejercicio_id, respuesta, es_correcta, tiempo_seg, creado_en); nivel_estudiante(usuario_id, competencia_id, nivel, racha, actualizado_en, PK compuesta).
  - Criterio: Restricciones y índices por usuario_id y creado_en.
- **#89 Entidades, repositorios y carga del CSV de ejercicios.** Entidades en practica/ y contenido/; migración de datos desde el CSV de Fernando.
  - Criterio: Los ejercicios del CSV quedan cargados.

<a id="90"></a>

### #90 · Implementar el motor de selección adaptativa de ejercicios

**Responsable:** Juan David Polanco · **Tipo:** Historia · **Épica:** EPIC-3 · **Área:** Backend · **Requisito:** RF-05

**Descripción.** Algoritmo basado en reglas (sin IA, según el alcance) que elige el siguiente ejercicio según el resultado del diagnóstico y los aciertos/errores recientes, priorizando las competencias con más dificultad.

**Criterios de aceptación**

- Un estudiante con categoría baja en una dimensión recibe mayoritariamente ejercicios de esa dimensión.
- No se repite un ejercicio ya acertado mientras haya otros disponibles.
- Las reglas están documentadas para el capítulo 6 del informe.

**Subtareas**

- **#91 Documentar las reglas del motor.** Pesos por dimensión según categoría, ventana de últimos N intentos, criterios de repetición y desempate.
  - Criterio: Documento revisado por el equipo.
- **#92 Implementar SeleccionEjercicioService.** Servicio puro y probado que recibe estado del estudiante y devuelve el siguiente ejercicio.
  - Criterio: Determinista con una semilla para pruebas.
- **#93 Endpoint GET /api/practica/siguiente.** Devuelve el ejercicio sin la respuesta, con su dimensión y nivel.
  - Criterio: Solo ESTUDIANTE; no expone la respuesta.
- **#94 Pruebas del motor.** Escenarios: estudiante nuevo, débil en una dimensión, que domina todo, sin ejercicios disponibles.
  - Criterio: Pasan en CI.

<a id="95"></a>

### #95 · Implementar la lógica de niveles de dificultad

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-3 · **Área:** Backend · **Requisito:** RF-16

**Descripción.** Reglas que suben o bajan el nivel del estudiante en cada competencia según su desempeño reciente, y que el motor usa para elegir ejercicios.

**Criterios de aceptación**

- Tras N aciertos seguidos sube de nivel y tras M errores baja (N y M configurables).
- El nivel nunca sale del rango 1–3.
- El nivel inicial se deriva de la categoría del diagnóstico.

**Subtareas**

- **#96 Nivel inicial desde el diagnóstico.** Al finalizar el diagnóstico, inicializar nivel_estudiante por competencia.
  - Criterio: Bajo → 1, medio → 2, alto → 3 (o lo que se acuerde).
- **#97 Reglas de subida y bajada.** Actualizar nivel y racha tras cada intento de práctica.
  - Criterio: Configurable en application.yml.
- **#98 Pruebas de niveles.** Casos de límite 1 y 3 y rachas.
  - Criterio: Pasan en CI.

<a id="99"></a>

### #99 · Modelar grupos y asociación docente–estudiante

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-4 · **Área:** Backend, BD · **Requisito:** RF-10, RNF-04

**Descripción.** RF-10 exige que el docente vea "sus estudiantes asociados", pero el modelo conceptual no tiene esa relación. Se agrega grupo (curso de Cálculo Diferencial en un periodo) con su docente y sus estudiantes.

**Criterios de aceptación**

- Un docente solo puede consultar datos de estudiantes de sus grupos (probado con un docente ajeno → 403).
- El administrador o el docente puede inscribir estudiantes a un grupo (por correo o código de grupo).

**Subtareas**

- **#100 Migración grupo y grupo_estudiante.** grupo(id, nombre, periodo, docente_id FK usuario, codigo_inscripcion UNIQUE, activo); grupo_estudiante(grupo_id, estudiante_id, PK compuesta).
  - Criterio: Migración aplicada.
- **#101 Endpoints de grupos.** CRUD de grupo (docente/admin) e inscripción de estudiante por código.
  - Criterio: Documentados en Swagger.
- **#102 Regla de acceso por grupo.** Método reutilizable esDocenteDe(estudianteId) que usarán los servicios de indicadores y el historial.
  - Criterio: Prueba con docente ajeno → 403.

<a id="103"></a>

### #103 · Definir el protocolo y umbral de usabilidad

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-7 · **Área:** QA, Docs · **Requisito:** RNF-01

**Descripción.** Procedimiento de medición de usabilidad para la validación piloto: tareas, participantes, momento de aplicación, instrumento SUS y umbral de aceptación de RNF-01.

**Criterios de aceptación**

- Protocolo escrito con 4–6 tareas (ej. iniciar sesión, presentar diagnóstico, resolver 3 ejercicios, consultar progreso; docente: consultar grupo).
- Umbral definido (ej. ≥ 80 % de tareas completadas sin ayuda y SUS ≥ 68).
- Se reutiliza en el capítulo 5 del informe.

## Sprint 3

21 oct – 1 nov 2026. Terminar la práctica adaptativa con retroalimentación inmediata, construir los tableros de seguimiento y analítica, e iniciar la gestión de contenidos.

| # | Tarea | Tipo | Épica | Responsable |
|---|---|---|---|---|
| [104](#104) | Construir interfaz de ejercicios interactivos | Historia | EPIC-3 | Fernando Pérez |
| [110](#110) | Implementar retroalimentación inmediata | Historia | EPIC-3 | Juan David Polanco |
| [114](#114) | Registrar intentos con trazabilidad completa | Tarea | EPIC-3 | Juan David Polanco |
| [117](#117) | Diseñar los indicadores de progreso del estudiante | Tarea | EPIC-4 | Fernando Pérez |
| [118](#118) | Implementar el servicio de cálculo de indicadores del estudiante | Tarea | EPIC-4 | Fernando Pérez |
| [119](#119) | Construir dashboard de progreso del estudiante | Historia | EPIC-4 | Fernando Pérez |
| [123](#123) | Implementar el servicio de indicadores de grupo para el docente | Tarea | EPIC-4 | Jorel Vargas |
| [124](#124) | Construir dashboard de desempeño para el docente | Historia | EPIC-4 | Nicolás Pérez |
| [128](#128) | Implementar historial de resultados | Tarea | EPIC-4 | Nicolás Pérez |
| [131](#131) | Implementar indicadores derivados de analítica de aprendizaje | Tarea | EPIC-4 | Juan David Polanco |
| [132](#132) | Implementar gestión de actividades y contenidos por el docente | Historia | EPIC-5 | Juan David Polanco |
| [135](#135) | Ejecutar pruebas de rendimiento de servicios críticos | Tarea | EPIC-7 | Jorel Vargas |

<a id="104"></a>

### #104 · Construir interfaz de ejercicios interactivos

**Responsable:** Fernando Pérez · **Tipo:** Historia · **Épica:** EPIC-3 · **Área:** Frontend · **Requisito:** RF-06

**Descripción.** Como estudiante quiero resolver ejercicios uno tras otro, enviar mi respuesta y saber de inmediato si acerté.

**Criterios de aceptación**

- Muestra el ejercicio asignado por el motor con su dimensión y nivel.
- Soporta opción múltiple y respuesta numérica.
- Tras enviar muestra correcto/incorrecto y permite pasar al siguiente.
- Accesible por teclado y usable en móvil.

**Subtareas**

- **#105 Cliente practicaApi.** siguiente(), responder(), historial().
  - Criterio: Coincide con el contrato.
- **#106 Pantalla de ejercicio.** MathText, opción múltiple o campo numérico validado, botón Enviar con estado de carga.
  - Criterio: Ambos tipos funcionan.
- **#107 Resultado del intento y siguiente ejercicio.** Estado correcto/incorrecto (icono + texto, no solo color) y botón Siguiente.
  - Criterio: El flujo no requiere recargar.
- **#108 Indicadores de sesión de práctica.** Racha, nivel actual por dimensión y ejercicios resueltos en la sesión.
  - Criterio: Se actualiza tras cada intento.
- **#109 Revisión de accesibilidad de la práctica.** Foco tras enviar, anuncios aria-live del resultado, contraste.
  - Criterio: Sin errores críticos en Lighthouse Accessibility.

<a id="110"></a>

### #110 · Implementar retroalimentación inmediata

**Responsable:** Juan David Polanco · **Tipo:** Historia · **Épica:** EPIC-3 · **Área:** Frontend, Backend · **Requisito:** RF-07

> Nota de coordinación: Juan David apoya la parte de backend.

**Descripción.** Después de cada respuesta, mostrar una explicación, pista o error común que ayude a entender el procedimiento, no solo si acertó.

**Criterios de aceptación**

- Respuesta incorrecta: se muestra pista y, al segundo intento fallido, la explicación completa.
- Respuesta correcta: se muestra la explicación breve.
- La retroalimentación llega en la misma respuesta del endpoint de intento.

**Subtareas**

- **#111 Incluir la retroalimentación en la respuesta del intento.** POST /api/practica/intentos devuelve es_correcta, pista o explicación según el número de intento.
  - Criterio: Prueba de las dos ramas.
- **#112 Panel de retroalimentación.** Componente con pista, explicación (MathText) y error común, anunciado con aria-live.
  - Criterio: Se ve bajo el ejercicio sin tapar el enunciado.
- **#113 Pistas progresivas.** Botón "Ver pista" antes de responder y flujo de segundo intento.
  - Criterio: El segundo intento queda registrado.

<a id="114"></a>

### #114 · Registrar intentos con trazabilidad completa

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-3 · **Área:** Backend · **Requisito:** RF-08, RNF-10

**Descripción.** Persistir cada intento con estudiante, ejercicio, resultado, fecha y tiempo empleado, evitando duplicados por doble clic o reintentos de red.

**Criterios de aceptación**

- Cada intento queda consultable con los 5 datos (CA-08).
- Un doble envío idéntico en menos de 2 s no crea dos registros.

**Subtareas**

- **#115 Servicio de registro de intentos.** Guardar intento, actualizar nivel_estudiante en la misma transacción.
  - Criterio: Transaccional.
- **#116 Protección contra envíos duplicados.** Clave de idempotencia enviada por el frontend (UUID por intento) con restricción única.
  - Criterio: Prueba de doble envío.

<a id="117"></a>

### #117 · Diseñar los indicadores de progreso del estudiante

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-4 · **Área:** Contenido, Docs · **Requisito:** RF-09

**Descripción.** Definir las métricas de progreso: porcentaje de aciertos por dimensión, intentos, ejercicios completados, nivel actual, variación frente al diagnóstico y tendencia semanal.

**Criterios de aceptación**

- Cada indicador tiene fórmula, fuente de datos y visualización propuesta en la tarjeta.
- Juan David confirma que se pueden calcular con el modelo.

<a id="118"></a>

### #118 · Implementar el servicio de cálculo de indicadores del estudiante

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-4 · **Área:** Backend · **Requisito:** RF-09

**Descripción.** Consultas y agregaciones sobre los intentos que alimentan el dashboard del estudiante.

**Criterios de aceptación**

- GET /api/progreso/me devuelve todos los indicadores definidos en \< 1 s con 1.000 intentos.
- Resultados verificados contra un cálculo manual.

<a id="119"></a>

### #119 · Construir dashboard de progreso del estudiante

**Responsable:** Fernando Pérez · **Tipo:** Historia · **Épica:** EPIC-4 · **Área:** Frontend · **Requisito:** RF-09

**Descripción.** Inicio del estudiante: resumen del diagnóstico, nivel y aciertos por dimensión, actividades completadas y tendencia, con accesos a diagnóstico y práctica.

**Criterios de aceptación**

- Muestra los indicadores definidos con datos reales.
- Estado vacío claro si aún no presentó el diagnóstico.
- Cada gráfico tiene alternativa textual.

**Subtareas**

- **#120 Tarjetas de resumen y estados vacíos.** Diagnóstico, nivel por dimensión, ejercicios completados.
  - Criterio: Sin diagnóstico → invita a presentarlo.
- **#121 Gráficos de aciertos y tendencia.** recharts: barras por dimensión y línea semanal.
  - Criterio: Tooltips y tabla accesible.
- **#122 Diseño responsive.** Grid de MUI de 1 a 3 columnas.
  - Criterio: Correcto en 375 px y 1440 px.

<a id="123"></a>

### #123 · Implementar el servicio de indicadores de grupo para el docente

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-4 · **Área:** Backend · **Requisito:** RF-10

**Descripción.** Agregaciones por grupo: promedio por dimensión, distribución de categorías, estudiantes en riesgo y detalle por estudiante, solo para los grupos del docente.

**Criterios de aceptación**

- Un docente solo ve sus grupos (403 en otros).
- Filtros por competencia y periodo funcionan.

<a id="124"></a>

### #124 · Construir dashboard de desempeño para el docente

**Responsable:** Nicolás Pérez · **Tipo:** Historia · **Épica:** EPIC-4 · **Área:** Frontend · **Requisito:** RF-10

**Descripción.** Panel donde el docente consulta el desempeño de sus grupos y de cada estudiante, con filtros por competencia y periodo.

**Criterios de aceptación**

- Selector de grupo, resumen por dimensión, tabla de estudiantes ordenable y detalle por estudiante.
- Filtros por competencia y periodo.

**Subtareas**

- **#125 Resumen del grupo.** Tarjetas y gráfico por dimensión; distribución de categorías.
  - Criterio: Coincide con el endpoint.
- **#126 Tabla de estudiantes y detalle.** DataGrid/Table ordenable, búsqueda, enlace al detalle del estudiante.
  - Criterio: Ordena por puntaje y categoría.
- **#127 Filtros por competencia y periodo.** Filtros sincronizados con la URL.
  - Criterio: Al recargar se conservan.

<a id="128"></a>

### #128 · Implementar historial de resultados

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-4 · **Área:** Backend, Frontend · **Requisito:** RF-15

**Descripción.** Endpoint y vista del historial de diagnósticos e intentos de práctica, con filtros por fecha, tipo de actividad y competencia; el docente lo ve para sus estudiantes.

**Criterios de aceptación**

- Paginado (20 por página).
- Estudiante ve solo lo suyo; docente solo de sus grupos.

**Subtareas**

- **#129 Endpoint de historial paginado.** GET /api/historial?tipo&competencia&desde&hasta&page.
  - Criterio: Filtros y paginación probados.
- **#130 Vista de historial.** Tabla con filtros en /estudiante/progreso y en el detalle del estudiante del docente.
  - Criterio: Reutiliza el mismo componente.

<a id="131"></a>

### #131 · Implementar indicadores derivados de analítica de aprendizaje

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-4 · **Área:** Backend · **Requisito:** RF-11

**Descripción.** Patrones agregados del grupo: dimensiones con mayor dificultad, ejercicios con más errores y evolución general del curso.

**Criterios de aceptación**

- Top 5 de ejercicios con más errores y dimensión más débil por grupo.
- Visibles en el dashboard docente.

<a id="132"></a>

### #132 · Implementar gestión de actividades y contenidos por el docente

**Responsable:** Juan David Polanco · **Tipo:** Historia · **Épica:** EPIC-5 · **Área:** Backend, Frontend · **Requisito:** RF-12, RF-14

**Descripción.** Como docente quiero crear, editar, activar/desactivar y eliminar las actividades de práctica de mis estudiantes.

**Criterios de aceptación**

- CRUD completo con validación y confirmación antes de eliminar.
- Un ejercicio con intentos no se borra: se desactiva.
- El motor solo usa ejercicios activos.

**Subtareas**

- **#133 API CRUD de contenidos y ejercicios.** Endpoints /api/contenidos y /api/ejercicios para DOCENTE.
  - Criterio: Validación y 403 para otros roles.
- **#134 Pantalla de gestión de actividades.** Lista con filtros, formulario (react-hook-form + zod, vista previa con MathText), activar/desactivar.
  - Criterio: Crear un ejercicio y verlo en la práctica.

<a id="135"></a>

### #135 · Ejecutar pruebas de rendimiento de servicios críticos

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-7 · **Área:** QA, Backend · **Requisito:** RNF-02

**Descripción.** Medir el tiempo de respuesta de /api/practica/siguiente y de la finalización del diagnóstico bajo carga simulada y fijar el umbral de RNF-02.

**Criterios de aceptación**

- Script de carga (k6 o JMeter) en el repo.
- p95 \< 1 s con 30 usuarios concurrentes (ajustar tras la primera medición) y resultado registrado para el cap. 7.

## Sprint 4

2 nov – 12 nov 2026. Cerrar administración, integrar todos los módulos, ejecutar pruebas de seguridad, concurrencia y validación con usuarios, y empaquetar la entrega del MVP.

| # | Tarea | Tipo | Épica | Responsable |
|---|---|---|---|---|
| [136](#136) | Implementar administración de usuarios y roles | Historia | EPIC-5 | Jorel Vargas |
| [139](#139) | Construir panel de administración general | Tarea | EPIC-5 | Fernando Pérez |
| [140](#140) | Integración end-to-end de todos los módulos | Tarea | EPIC-6 | Juan David Polanco |
| [141](#141) | Ejecutar pruebas funcionales frente a los 16 criterios de aceptación | Tarea | EPIC-6 | Fernando Pérez |
| [142](#142) | Corregir incidencias detectadas | Tarea | EPIC-6 | Juan David Polanco |
| [143](#143) | Preparar y ejecutar la validación con usuarios piloto | Historia | EPIC-6 | Juan David Polanco |
| [144](#144) | Aplicar y tabular la encuesta de usabilidad (SUS) | Tarea | EPIC-6 | Nicolás Pérez |
| [145](#145) | Aplicar ajustes finales de usabilidad y accesibilidad | Tarea | EPIC-6 | Nicolás Pérez |
| [146](#146) | Consolidar documentación técnica de la entrega | Tarea | EPIC-6 | Nicolás Pérez |
| [147](#147) | Empaquetar y entregar el MVP | Tarea | EPIC-6 | Jorel Vargas |
| [148](#148) | Hardening de seguridad por rol | Tarea | EPIC-7 | Jorel Vargas |
| [149](#149) | Ejecutar prueba de concurrencia | Tarea | EPIC-7 | Fernando Pérez |
| [150](#150) | Implementar registro de estudiantes por invitación _(nueva; crear en Jira)_ | Historia | EPIC-5 | Jorel Vargas |

<a id="136"></a>

### #136 · Implementar administración de usuarios y roles

**Responsable:** Jorel Vargas · **Tipo:** Historia · **Épica:** EPIC-5 · **Área:** Backend, Frontend · **Requisito:** RF-13

**Descripción.** Como administrador quiero crear, editar, desactivar usuarios y cambiar su rol.

**Criterios de aceptación**

- CRUD de usuarios con búsqueda y filtros por rol y estado.
- Un usuario desactivado no puede iniciar sesión.
- El administrador no puede desactivarse a sí mismo.

**Subtareas**

- **#137 API de administración de usuarios.** /api/admin/usuarios solo ADMIN, contraseña temporal al crear.
  - Criterio: Pruebas de permisos.
- **#138 Pantalla de usuarios.** Tabla, formulario y diálogo de confirmación.
  - Criterio: Flujo completo en staging.

<a id="139"></a>

### #139 · Construir panel de administración general

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-5 · **Área:** Frontend · **Requisito:** RF-13

**Descripción.** Vista consolidada del administrador: totales de usuarios por rol, grupos activos y actividades activas, con accesos a cada gestión.

**Criterios de aceptación**

- Muestra conteos reales y enlaces funcionales.

<a id="140"></a>

### #140 · Integración end-to-end de todos los módulos

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** Backend, Frontend

**Descripción.** Ensamblar todos los módulos en develop y staging, verificando que compartan usuarios, roles y datos, y recorrer los flujos completos por rol.

**Criterios de aceptación**

- Los 3 flujos completos (estudiante, docente, admin) funcionan en staging sin intervención manual en la BD.

<a id="141"></a>

### #141 · Ejecutar pruebas funcionales frente a los 16 criterios de aceptación

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** QA · **Requisito:** CA-01 a CA-16

**Descripción.** Ejecutar y documentar CA-01 a CA-16 con evidencia, y actualizar la matriz de trazabilidad (CP-01 a CP-08).

**Criterios de aceptación**

- Cada CA con resultado Cumple / No cumple y evidencia.
- Defectos registrados en Jira como Bug.

<a id="142"></a>

### #142 · Corregir incidencias detectadas

**Responsable:** Juan David Polanco · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** Backend, Frontend

**Descripción.** Priorizar y corregir los defectos de integración y pruebas; cada uno lo corrige el responsable del módulo y se vuelve a verificar su CA.

**Criterios de aceptación**

- Sin defectos de prioridad Alta abiertos al 11 nov.

<a id="143"></a>

### #143 · Preparar y ejecutar la validación con usuarios piloto

**Responsable:** Juan David Polanco · **Tipo:** Historia · **Épica:** EPIC-6 · **Área:** QA

**Descripción.** Aplicar el protocolo de usabilidad con estudiantes y docentes piloto sobre staging.

**Criterios de aceptación**

- Sesiones realizadas con los participantes definidos en el protocolo.
- Observaciones consolidadas en una tabla.
- Consentimiento informado y datos anonimizados (Ley 1581).

<a id="144"></a>

### #144 · Aplicar y tabular la encuesta de usabilidad (SUS)

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** QA · **Requisito:** RNF-01

**Descripción.** Aplicar SUS a los participantes y calcular el puntaje frente al umbral del protocolo.

**Criterios de aceptación**

- Puntaje SUS calculado y comparado con el umbral de RNF-01.

<a id="145"></a>

### #145 · Aplicar ajustes finales de usabilidad y accesibilidad

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** Frontend, QA · **Requisito:** RNF-05

**Descripción.** Corregir hallazgos del piloto y verificar WCAG 2.1 AA en las pantallas principales.

**Criterios de aceptación**

- Lighthouse Accessibility ≥ 90 en login, diagnóstico, práctica y dashboards.
- Todas las pantallas principales operables por teclado.

<a id="146"></a>

### #146 · Consolidar documentación técnica de la entrega

**Responsable:** Nicolás Pérez · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** Docs

**Descripción.** Manual técnico, diagrama de despliegue y guía de instalación con la arquitectura realmente implementada (insumo del capítulo 6 del informe).

**Criterios de aceptación**

- README actualizados, diagrama de despliegue as-built y modelo de datos final exportado.

<a id="147"></a>

### #147 · Empaquetar y entregar el MVP

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-6 · **Área:** DevOps

**Descripción.** Checklist final, tag v1.0.0, datos y cuentas para la demostración de la sustentación.

**Criterios de aceptación**

- Tag v1.0.0 en main de ambos repos.
- Staging con datos de demostración y guion de demo probado.

<a id="148"></a>

### #148 · Hardening de seguridad por rol

**Responsable:** Jorel Vargas · **Tipo:** Tarea · **Épica:** EPIC-7 · **Área:** Seguridad, Backend · **Requisito:** RNF-03

**Descripción.** Revisar todos los endpoints: autenticación, autorización por rol y por grupo, validación de entradas, exposición de datos sensibles y cabeceras de seguridad.

**Criterios de aceptación**

- Matriz endpoint × rol probada automáticamente.
- Ninguna respuesta expone password_hash ni claves de respuesta.
- Secretos de staging rotados y fuera del repo.

<a id="149"></a>

### #149 · Ejecutar prueba de concurrencia

**Responsable:** Fernando Pérez · **Tipo:** Tarea · **Épica:** EPIC-7 · **Área:** QA, Backend · **Requisito:** RNF-04, RNF-10

**Descripción.** Verificar bajo envíos simultáneos que no se dupliquen intentos ni respuestas del diagnóstico.

**Criterios de aceptación**

- 50 envíos simultáneos del mismo intento producen un solo registro.

<a id="150"></a>

### #150 · Implementar registro de estudiantes por invitación

**Responsable:** Jorel Vargas · **Tipo:** Historia · **Épica:** EPIC-5 · **Área:** Backend, Frontend, Seguridad · **Requisito:** RF-01, RF-13

> Tarea nueva (antes "bloque 2b"). Crear en Jira como Historia de la EPIC-5, en el Sprint 4, bloqueada por #99 y #137.

**Descripción.** Como docente quiero invitar por correo a los estudiantes del listado de mi grupo, para que solo ellos puedan crear su cuenta. Hoy cualquiera con un correo de estudiante (u + 11 dígitos + @usco.edu.co) puede registrarse; la plataforma debe ser cerrada: el administrador crea los docentes y cada docente invita a los estudiantes de su grupo. Depende de #99 (grupos: el listado se guarda en grupo_estudiante, no en tablas nuevas) y de #137 (el administrador crea a los docentes). Antes de empezar, el equipo decide: servicio de correo (y si la universidad permite enviar desde @usco.edu.co), formato real del listado que reciben los docentes, cuántos días dura una invitación y si se puede reenviar, y qué pasa si un estudiante está en dos grupos.

**Criterios de aceptación**

- Sin una invitación válida, POST /api/auth/registro responde con error y no crea la cuenta.
- La invitación es de un solo uso, vence y solo sirve para el correo al que se envió.
- El docente carga el listado de su grupo y ve quién ya se registró.

**Subtareas**

- **#151 Elegir y configurar el servicio de correo.** Comparar opciones (SMTP de una cuenta del semillero u otro servicio), configurar las credenciales como variables de entorno y documentarlas en el README.
  - Criterio: Un correo de prueba llega desde el entorno de staging.
  - Criterio: Ninguna credencial queda en el repositorio.
- **#152 Carga del listado del grupo.** Endpoint para que el docente suba el listado de su grupo (CSV exportado de Excel con correo y nombres); valida el formato del correo de estudiante y registra cada fila en grupo_estudiante como INVITADO.
  - Criterio: Las filas inválidas se reportan sin cargar las demás a medias.
  - Criterio: Un docente solo puede cargar listados en sus grupos (403 en otros).
- **#153 Invitaciones con token de un solo uso.** Tabla invitacion (correo, grupo, hash del token, vencimiento, usada_en) y envío del enlace /registro?token=… por correo; reenvío desde el docente.
  - Criterio: El token no se guarda en claro.
  - Criterio: Una invitación vencida o usada no sirve.
- **#154 Registro que exige invitación.** POST /api/auth/registro recibe el token, verifica que el correo coincida con el de la invitación, crea el estudiante, lo marca REGISTRADO en el grupo y consume la invitación en la misma transacción.
  - Criterio: Registro sin token, con token ajeno o con otro correo → error.
  - Criterio: Registro con invitación válida → 201 y el estudiante queda en su grupo.
- **#155 Pantallas de listado, invitaciones y registro.** Docente: carga del listado y estado de cada estudiante (invitado/registrado) con opción de reenviar. Estudiante: la pantalla de registro lee el token del enlace y precarga el correo.
  - Criterio: Flujo completo en staging, de la carga del listado al primer inicio de sesión.
- **#156 Pruebas del registro por invitación.** Pruebas de integración de los casos de invitación vencida, usada, ajena y correo distinto, y del acceso del docente a grupos ajenos.
  - Criterio: Pasan en CI.
