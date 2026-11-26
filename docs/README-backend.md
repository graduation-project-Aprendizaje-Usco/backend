# Aprendizaje USCO — Backend

[![CI](https://github.com/aprendizaje-usco-project/aprendizaje-usco-backend/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/aprendizaje-usco-project/aprendizaje-usco-backend/actions/workflows/ci.yml?query=branch%3Adevelop)

API REST de **Aprendizaje USCO**, plataforma web de diagnóstico, práctica adaptativa y analítica de aprendizaje para Cálculo Diferencial (Proyecto Integrador II, Ingeniería de Software, Universidad Surcolombiana).

| | |
|---|---|
| Stack | Java 21 · Spring Boot 4.1 · Spring Security (JWT) · Spring Data JPA · PostgreSQL 17 · Flyway |
| Frontend | [aprendizaje-usco-frontend](https://github.com/aprendizaje-usco-project/aprendizaje-usco-frontend) |
| Tablero | Jira, proyecto `SCRUM` |
| Convenciones | [CONTRIBUTING.md](CONTRIBUTING.md) |
| Qué se ha implementado | [implementacion.md](implementacion.md) (historia por historia: qué hace, decisiones, cómo probarlo) |
| Manual técnico | [manual-tecnico.md](manual-tecnico.md) (arquitectura, despliegue, instalación completa, endpoints, seguridad) |
| Despliegue en Render + Neon | [despliegue-render.md](despliegue-render.md) (variables y pasos) |
| Modelo de datos | [base-de-datos/modelo-fisico.md](base-de-datos/modelo-fisico.md) (diagrama completo, tablas y restricciones) |

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK (Eclipse Temurin) | 21 |
| Docker Desktop | actual (levanta PostgreSQL y lo usan las pruebas) |
| Git | 2.40+ |

No hace falta instalar Maven: el proyecto trae el wrapper (`mvnw`).

## 2. Cómo correrlo en local

```bash
git clone https://github.com/aprendizaje-usco-project/aprendizaje-usco-backend.git
cd aprendizaje-usco-backend
git checkout develop

cp .env.template .env           # Windows: copy .env.template .env  → y llena los valores

# Docker Desktop debe estar abierto

# Opción 1: todo en Docker (backend + PostgreSQL + pgAdmin)
docker compose up -d --build

# Opción 2: backend local y solo base + pgAdmin en Docker
docker compose up -d postgres pgadmin
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

La aplicación usa una sola configuración de ejecución y toma sus valores de `.env` o del entorno. En local conecta al PostgreSQL de `compose.yaml` (puerto 5433), aplica las migraciones de Flyway y carga los usuarios de prueba indicados por `SPRING_FLYWAY_LOCATIONS`. En la opción 1 el backend queda en `http://localhost:8080`; en la opción 2 lo levanta Maven y no debe ejecutarse al mismo tiempo que el contenedor del backend porque ambos usan el puerto 8080.

PostgreSQL guarda sus datos en `postgres_data/`. Para detener los servicios sin borrar los datos usa `docker compose stop`.

| Comprobación | URL | Resultado esperado |
|---|---|---|
| Salud | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| Documentación de la API | http://localhost:8080/swagger-ui.html | Swagger UI |
| pgAdmin (ver la base desde el navegador) | http://localhost:5050 | Ingreso con `PGADMIN_DEFAULT_EMAIL` y `PGADMIN_DEFAULT_PASSWORD` de tu `.env` |

**Sin Docker:** instala PostgreSQL 17, crea la base `aprendizaje_usco` con el usuario y la contraseña que definas en tu `.env` (puerto 5432), cambia `SPRING_DATASOURCE_URL` a `jdbc:postgresql://localhost:5432/aprendizaje_usco` y define `SPRING_DOCKER_COMPOSE_ENABLED=false`. El `.env` nunca se sube al repositorio.

## 3. Comandos útiles

| Comando | Qué hace |
|---|---|
| `./mvnw spring-boot:run` | Corre la API en el puerto 8080 con la configuración de `.env` |
| `./mvnw test` | Pruebas unitarias y de integración (Testcontainers, requiere Docker). Es lo mismo que corre CI en cada PR |
| `./mvnw -B -ntp verify` | Compila, ejecuta las pruebas y verifica/empaqueta el backend |
| `./mvnw clean package` | Genera el `.jar` en `target/` |
| `./mvnw flyway:info` | Estado de las migraciones (opcional, si se configura el plugin) |

Verificación registrada el **4 de noviembre de 2026**: `./mvnw -B -ntp verify` terminó con `BUILD SUCCESS` (229 pruebas, 0 fallos y 0 errores).

## 4. Configuración

| Archivo | Uso |
|---|---|
| `src/main/resources/application.properties` | Única configuración de ejecución para local y despliegues; las diferencias se definen con variables |
| `src/test/resources/config/application.properties` | Configuración aislada de pruebas automáticas; puede activar el perfil `test` |
| `.env` (no se sube; copia de `.env.template`) | Tus valores locales de base de datos y pgAdmin |

Variables de entorno (en el hosting se definen en su panel, **nunca en el repositorio**):

| Variable | Descripción | Local / desarrollo | Render |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Conexión JDBC a PostgreSQL | `.env` / Docker Compose | URL de Neon con `?sslmode=require`; credenciales obligatorias |
| `SPRING_FLYWAY_LOCATIONS` | Migraciones Flyway adicionales | En `.env` incluye `classpath:db/dev-data` para usuarios demo | `classpath:db/migration` (sin usuarios demo) |
| `SPRING_DOCKER_COMPOSE_ENABLED` | Permite a Spring Boot iniciar Compose al ejecutar localmente con Maven | `true` | `false` |
| `JWT_SECRET` | Llave para firmar los tokens (mínimo 32 caracteres), obligatoria en todos los entornos | Se define en `.env` | Obligatoria y aleatoria |
| `BOOTSTRAP_ADMIN_EMAIL` / `BOOTSTRAP_ADMIN_PASSWORD` | Credenciales del administrador inicial; configúralas juntas para crear la cuenta si no existe | Opcionales en `.env` | Opcionales en el panel de Render |
| `BOOTSTRAP_ADMIN_NAMES` / `BOOTSTRAP_ADMIN_SURNAMES` | Nombre y apellido de la cuenta inicial | `Administrador` / `Sistema` por defecto | Opcionales |
| `JWT_EXPIRATION_MINUTES` | Duración del token en minutos | `120` | Opcional (`120`) |
| `CORS_ORIGINS` | Orígenes permitidos para el frontend (separados por coma) | `http://localhost:5173` | Origen(es) público(s); si se omite, usa `FRONTEND_URL` |
| `FRONTEND_URL` | Origen del frontend para CORS y para armar `/registro?token=…` | `http://localhost:5173` | Dominio del frontend, sin barra final |
| `PORT` | Puerto HTTP del servidor | `8080` | La asigna Render; no definir a mano |
| `RESEND_API_KEY` | Clave Bearer para enviar correos mediante HTTPS con Resend | Vacía; necesaria para enviar | Obligatoria para enviar |
| `MAIL_FROM` | Remitente verificado de Resend | `Aprendizaje USCO <noreply@aprendizaje-usco.tech>` | Opcional; mismo valor predeterminado |

### Administrador inicial

Al configurar `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`, al iniciar la
aplicación se crea esa cuenta con el rol `ADMIN` si aún no existe. Flyway aplica
primero las migraciones, que crean el rol; el runner guarda la contraseña con el
mismo BCrypt usado por el login. En cada reinicio verifica el correo y deja intacta
una cuenta `ADMIN` ya existente. Si el correo ya pertenece a otro rol, el arranque
falla en lugar de elevar sus privilegios. Si ambas variables están vacías, no se
crea ninguna cuenta; si solo se configura una, el arranque falla con un error claro.

La contraseña debe tener al menos 12 caracteres, incluir una letra y un número,
y ocupar como máximo 72 bytes UTF-8. Mantén estas credenciales solo en `.env` o
en el panel de Render. Si se conservan allí, el administrador se podrá recrear
después de reinicializar la base de datos.

**Despliegue en Render:** usa las mismas propiedades con valores del panel de Render,
conecta a Neon con SSL
(`sslmode=require`), escucha en el `PORT` de Render y mantiene el esquema con
Flyway (`clean-disabled=true`, Hibernate `ddl-auto=validate`). Guía completa y
variables: [despliegue-render.md](despliegue-render.md).

Propiedades de la prueba diagnóstica (en `application.properties`, no son secretos):

| Propiedad | Descripción | Valor |
|---|---|---|
| `app.diagnostico.gracia-segundos` | Tolerancia para aceptar una respuesta que llega justo después de que se acaba el tiempo | `5` |
| `app.diagnostico.barrido.habilitado` | Activa el barrido que finaliza por tiempo los intentos abandonados. Las pruebas automáticas lo apagan | `true` |
| `app.diagnostico.barrido.intervalo-ms` | Cada cuánto corre el barrido | `60000` (1 min) |
| `app.diagnostico.umbrales.global.*` | Cortes de la categoría global: `escala`, `maximo-alto-riesgo`, `maximo-riesgo-moderado`. Si no son coherentes, la app no arranca | `24`, `9`, `16` |
| `app.diagnostico.umbrales.dimension.*` | Cortes del nivel por competencia (mismas tres propiedades) | `8`, `3`, `6` |

Propiedades de la práctica adaptativa:

| Propiedad | Descripción | Valor |
|---|---|---|
| `app.practica.asignaturas` | Códigos de asignatura aceptados por el motor | `CALCULO_DIFERENCIAL,CALCULO_INTEGRAL` |
| `app.practica.niveles.aciertos-para-subir` | Aciertos consecutivos para subir de nivel | `3` |
| `app.practica.niveles.errores-para-bajar` | Errores consecutivos para bajar de nivel | `3` |
| `app.practica.niveles.nivel-inicial-sin-diagnostico` | Nivel cuando no hay diagnóstico | `1` |

## 5. Estructura

Organización **por módulo funcional**, con capas dentro de cada módulo (RNF-08). Cada integrante trabaja principalmente en su módulo, lo que reduce los conflictos de merge.

```
src/main/java/co/edu/usco/aprendizaje_usco/
├── config/        CORS, OpenAPI, beans generales
├── security/      SecurityConfig, servicio JWT, conversión de roles, respuestas 401/403
├── common/        excepciones, manejador global de errores, utilidades
└── modulos/
    ├── usuario/       RF-01, RF-13
    ├── diagnostico/   RF-02, RF-03, RF-04
    ├── practica/      RF-05 a RF-08, RF-16
    ├── contenido/     RF-12, RF-14
    ├── progreso/      RF-09, RF-15
    └── analitica/     RF-10, RF-11

Dentro de cada módulo:
  controller/   endpoints REST (solo reciben, validan y delegan)
  service/      lógica de negocio
  repository/   interfaces Spring Data JPA
  dto/          objetos de entrada y salida de la API (nunca exponer entidades)
  model/        entidades JPA y enums
  validacion/   anotaciones de validación propias (si el módulo las necesita)

src/main/resources/db/migration/   migraciones Flyway (todos los entornos)
src/main/resources/db/dev-data/    datos de prueba (solo si SPRING_FLYWAY_LOCATIONS los incluye)
```

## 6. Convenciones de la API

- Todas las rutas empiezan por `/api` y usan sustantivos en plural: `/api/diagnosticos`, `/api/ejercicios/{id}/intentos`.
- Rutas públicas: solo `POST /api/auth/login`, `POST /api/auth/registro`, `/actuator/health` y Swagger. Todo lo demás (incluido `GET /api/auth/me`) exige `Authorization: Bearer <token>`.
- Restricción por rol con `@PreAuthorize("hasRole('DOCENTE')")` en el controlador (método o clase). Roles: `ESTUDIANTE`, `DOCENTE`, `ADMIN`.
- **Todo endpoint nuevo lleva `@PreAuthorize`.** La regla general de seguridad es solo "con sesión", así que sin la anotación cualquier rol entraría. `ProteccionDeEndpointsTest` falla si falta; los públicos a propósito se agregan a su lista de excepciones.
- `GET /api/ping/{rol}` es un endpoint de prueba del acceso por rol: solo existe con el perfil `test`.
- Los controladores reciben y devuelven **DTO**, nunca entidades.
- Errores con un único formato, generado por el manejador global en `common/`:

```json
{
  "timestamp": "2026-10-01T14:30:00Z",
  "status": 404,
  "message": "Ejercicio no encontrado",
  "path": "/api/ejercicios/99"
}
```

| Código | Cuándo |
|---|---|
| 200 / 201 / 204 | Consulta, creación, borrado correctos |
| 400 | Validación fallida del DTO |
| 401 | Sin token o token inválido |
| 403 | Token válido, rol sin permiso |
| 404 | Recurso inexistente |
| 409 | Conflicto (ej. intento duplicado, RNF-04) |
| 422 | Datos válidos que una regla de negocio no permite |

## 7. Base de datos y migraciones

- El esquema **solo** lo cambia Flyway; Hibernate está en `ddl-auto: validate`.
- Las migraciones siguen la numeración consecutiva `V1`, `V2`, … `V7`. La siguiente es `V8__descripcion.sql`; no se salta ni se reutiliza un número. Qué crea cada una y el diagrama completo: [modelo-fisico.md](base-de-datos/modelo-fisico.md).
- **Nunca** edites ni renombres una migración que ya está en `develop`: si necesitas cambiar el esquema, crea la siguiente versión.
- Si tu base local quedó con los nombres anteriores (`V202610221000`, `V202610221010` o `V202610242250`), Flyway no arranca. Como el SQL de esos archivos no cambió, se ajusta sin borrar datos (en Git Bash; desde PowerShell, pega las tres líneas `UPDATE` en el Query Tool de pgAdmin):

  ```bash
  docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' <<'SQL'
  UPDATE flyway_schema_history SET version = '5', script = 'V5__esquema_practica.sql' WHERE version = '202610221000';
  UPDATE flyway_schema_history SET version = '6', script = 'V6__datos_ejercicios_semilla.sql' WHERE version = '202610221010';
  UPDATE flyway_schema_history SET version = '7', script = 'V7__crear_grupos_docentes_estudiantes.sql' WHERE version = '202610242250';
  SQL
  ```
- Datos que necesitan todos los entornos (roles, competencias, los 24 ítems del diagnóstico) van en migraciones versionadas de `db/migration`.
- Datos de prueba que no deben llegar a producción (usuarios demo) van en `db/dev-data` como migración repetible `R__…sql`. En local se carga porque `.env` incluye esa ubicación; en despliegues usa solo `classpath:db/migration`.

Usuarios de prueba (solo en bases locales que incluyan `db/dev-data`):

| Rol | Correo | Contraseña |
|---|---|---|
| Estudiante | estudiante@aprendizaje.test | Estudiante2026* |
| Docente | docente@aprendizaje.test | Docente2026* |
| Administrador | admin@aprendizaje.test | Admin2026* |

## 8. Responsables por módulo

Quién responde por cada parte del backend. El detalle tarea por tarea está en [tareas-y-responsables.md](tareas-y-responsables.md); fechas y estado, en Jira.

| Módulo | Responsable principal |
|---|---|
| `security/`, `usuario/` (autenticación y administración de usuarios), `config/`, `common/` | Jorel Vargas |
| Esquema inicial y migraciones base | Jorel Vargas |
| `diagnostico/` (modelo, banco de ítems, aplicación de la prueba, puntaje y categoría) | Jorel Vargas |
| `practica/` y `contenido/` (banco de ejercicios, motor adaptativo, niveles, retroalimentación, intentos) | Juan David Polanco |
| Grupos (asociación docente–estudiante) e indicadores del estudiante en `progreso/` | Fernando Pérez |
| Indicadores de grupo para el docente en `analitica/` | Jorel Vargas |
| Indicadores derivados de analítica | Juan David Polanco |
| Historial de resultados | Nicolás Pérez |
| CI (`.github/workflows/`) y staging | Jorel Vargas |

## 9. Problemas frecuentes

| Síntoma | Causa probable / solución |
|---|---|
| `Connection refused` al arrancar | Docker Desktop cerrado o PostgreSQL local apagado; revisa `SPRING_DATASOURCE_URL` y `SPRING_DOCKER_COMPOSE_ENABLED` |
| `Falta la variable de entorno JWT_SECRET` al arrancar | Define una llave de al menos 32 caracteres en `.env` o en las variables del despliegue |
| `Schema-validation: missing table` | Falta la migración de esa entidad, o la migración tiene otro nombre de columna |
| `Validate failed: Migration checksum mismatch` | Se editó una migración ya aplicada. Revierte el cambio y crea una migración nueva |
| `Detected applied migration not resolved locally` al arrancar | Tu base local aplicó migraciones con nombres que después cambiaron (pasó con las de práctica: `V202610221000…` → `V5`). Si solo tienes datos de prueba, borra la base local y deja que se cree de nuevo: `docker compose down` y luego borra la carpeta `postgres_data/` |
| Las pruebas fallan con `relation "…" already exists` | Quedaron migraciones viejas compiladas en `target/`. Ejecuta `./mvnw clean test` |
| 401 en todos los endpoints | Falta el header `Authorization`, o el token expiró |
| Error de CORS desde el frontend | Revisa `CORS_ORIGINS` |

## 10. Registro de estudiantes

`POST /api/auth/registro` solo crea estudiantes y exige (las mismas reglas valida el formulario del frontend):

| Campo | Regla |
|---|---|
| `correo` | Correo institucional de estudiante: `u` + 11 dígitos + `@usco.edu.co` (ej. `u20201234567@usco.edu.co`). Se guarda en minúsculas |
| `password` | 8 a 72 caracteres, con al menos una letra y un número |
| `programaAcademico` | Obligatorio, uno de: Ingeniería Agrícola, Ingeniería Agroindustrial, Ingeniería Civil, Ingeniería de Petróleos, Ingeniería de Software, Ingeniería Electrónica, Licenciatura en Matemáticas (enum `ProgramaAcademico`) |
| `nombres`, `apellidos` | Obligatorios, máximo 100 caracteres |

El registro está **cerrado por invitación** (#150): el cuerpo incluye `tokenInvitacion`, el token debe estar vigente, no usado y corresponder al mismo correo. Si el token falta o no coincide, responde 422 y no crea la cuenta. El docente carga el listado desde `/docente/estudiantes`.

## 11. Limitaciones conocidas del MVP

| Limitación | Impacto | Plan |
|---|---|---|
| El token JWT sigue siendo válido hasta que vence (2 h) aunque el usuario sea desactivado o cambie de rol. Solo `GET /api/auth/me` consulta la base | Un usuario desactivado conserva acceso hasta 2 h. El frontend valida la sesión con `/me` al abrir la app | Revisarlo cuando exista la gestión de usuarios del administrador |
| No hay límite de intentos de inicio de sesión | Expuesto a ataques de fuerza bruta | Sprint de hardening (Sprint 4) |
| Cada estudiante presenta la prueba diagnóstica una sola vez; no hay forma de habilitarle un segundo intento | Si un estudiante tuvo un problema técnico, hoy solo se arregla con SQL | Endpoint de docente/administrador para reabrir o anular un intento, cuando exista la vista del docente |
| El resultado del diagnóstico solo lo ve el estudiante | El docente aún no puede consultar los resultados de su grupo | Módulo `analitica/` (RF-10, RF-11), después del registro por invitación (cursos y listados) |

## 12. Prueba diagnóstica (API)

Solo para el rol `ESTUDIANTE` (los demás roles reciben 403). El estudiante es siempre el dueño del token: la API nunca recibe un id de usuario.

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `POST /api/diagnosticos/intentos` | Inicia la prueba activa. Si el estudiante ya tiene intento (en curso o finalizado), lo devuelve: **cada prueba se presenta una vez** | 201 + `Location` (nuevo) · 200 (ya existía) · 404 (no hay prueba activa) |
| `GET /api/diagnosticos/intentos/actual` | Mi intento en la prueba activa | 200 · 204 (aún no la inicia) |
| `GET /api/diagnosticos/intentos/{id}` | Consulta un intento | 200 · 403 (es de otro) · 404 |
| `PUT /api/diagnosticos/intentos/{id}/respuestas/{itemId}` | Guarda o cambia la respuesta de una pregunta. Cuerpo: `{"opcionId": 34}` | 200 · 400 (falta `opcionId`) · 403 · 404 (la pregunta no es de la prueba) · 409 (ya finalizado) · 422 (se acabó el tiempo, o la opción no es de esa pregunta) |
| `POST /api/diagnosticos/intentos/{id}/finalizar` | Entrega y califica. Si ya estaba finalizado devuelve el mismo resultado | 200 · 403 · 404 |
| `GET /api/diagnosticos/intentos/{id}/resultado` | Solo el resultado (puntaje, categoría y niveles por competencia) | 200 · 403 · 404 · 409 (aún no ha finalizado) |

Respuesta de un intento (`IntentoResponse`, resumida):

```json
{
  "id": 7,
  "estado": "EN_CURSO",
  "prueba": "Prueba Diagnóstica de Competencias Matemáticas",
  "iniciadoEn": "2026-10-18T14:00:00Z",
  "venceEn": "2026-10-18T14:36:00Z",
  "segundosRestantes": 2100,
  "totalItems": 24,
  "totalRespondidas": 1,
  "respuestas": [ { "itemId": 1, "opcionId": 2, "respondidaEn": "2026-10-18T14:01:10Z" } ],
  "resultado": null,
  "items": [
    { "id": 1, "numero": 1, "enunciado": "Si $3x + 5 = 20$, entonces $x$ es:",
      "imagen": null, "imagenAlt": null,
      "dimension": { "codigo": "ALGEBRAICA", "nombre": "Competencia algebraica" },
      "opciones": [ { "id": 1, "letra": "A", "texto": "$3$" }, "…" ] }
  ]
}
```

Mientras la prueba está en curso, `items` trae **siempre las 24 preguntas**, respondidas o no: así el frontend puede pintar la prueba completa al retomarla. Lo que ya respondió el estudiante está en `respuestas` (pregunta → opción elegida) y el avance en `totalRespondidas`. `items` va de último en el JSON porque es la parte larga.

Cuando `estado` es `FINALIZADO`, `items` va vacío y `resultado` trae (lo mismo que devuelve `GET …/{id}/resultado`):

```json
{
  "puntaje": 12, "total": 24,
  "categoria": "RIESGO_MODERADO", "categoriaDescripcion": "Riesgo moderado",
  "finalizadoPorTiempo": false, "finalizadoEn": "2026-10-18T14:30:12Z",
  "dimensiones": [
    { "codigo": "ALGEBRAICA", "nombre": "Competencia algebraica", "aciertos": 8, "total": 8,
      "porcentaje": 100.00, "nivel": "ALTO", "nivelDescripcion": "Nivel alto (adecuado)" }
  ]
}
```

Reglas:

- **Las claves nunca salen de la API** (las opciones no traen `esCorrecta`).
- **Las opciones se barajan por intento**: la `letra` de cada opción es la posición mostrada (A–D), no la letra almacenada. El orden es estable para el mismo intento y distinto entre estudiantes; la calificación sigue usando el `id` de la opción.
- **El tiempo lo controla el servidor**: 36 minutos (1,5 min por pregunta). `segundosRestantes` es para el cronómetro del frontend. Un intento vencido se finaliza "por tiempo" en cuanto el estudiante lo consulta o responde, y si no vuelve, el barrido programado lo cierra en menos de un minuto. La fecha de fin de un intento vencido es la de vencimiento.
- Pregunta sin responder = incorrecta. Escalas del instrumento v2.0:

| Global (0–24) | Categoría | | Por competencia (0–8) | Nivel |
|---|---|---|---|---|
| 0–9 | `ALTO_RIESGO` — Alto riesgo académico | | 0–3 | `BAJO` — Nivel bajo (alto riesgo) |
| 10–16 | `RIESGO_MODERADO` — Riesgo moderado | | 4–6 | `MEDIO` — Nivel medio (riesgo moderado) |
| 17–24 | `NIVEL_ADECUADO` — Nivel adecuado | | 7–8 | `ALTO` — Nivel alto (adecuado) |

- Los cortes **no están en el código**: salen de `app.diagnostico.umbrales.*` (sección 4). Un cambio solo afecta a los intentos que se finalicen después, porque la categoría se guarda al finalizar.
- Si algún día se desactiva un ítem (`item.activo = false`), los cortes se aplican en proporción al nuevo total (9/24 y 16/24; 3/8 y 6/8).
- El banco de ítems revisable está en `docs/datos/items_diagnostico.csv` (separado por `;`, abre en Excel), con observaciones por ítem. `EsquemaDiagnosticoTest` verifica que la migración y el CSV digan lo mismo: si corriges un ítem, cambia los dos.
- Para cambiar el instrumento: una migración nueva que inserte otra `prueba_diagnostica` con `activa = TRUE` y desactive la anterior (solo puede haber una activa). Nunca editar `V4`.
- Las gráficas de los ítems 17 a 19 están en el frontend (`public/img/diagnostico/`); la base solo guarda la ruta y el texto alternativo.

## 13. Práctica adaptativa (API)

Solo para `ESTUDIANTE`. El estudiante se toma del token; la API nunca recibe un `usuarioId`.

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/practica/siguiente?asignatura=...` | Selecciona el siguiente ejercicio de la asignatura. `asignatura` es obligatoria y validada (`CALCULO_DIFERENCIAL` o `CALCULO_INTEGRAL`) | 200 · 400 (asignatura ausente o inválida) · 403 · 404 (sin ejercicios) |
| `POST /api/practica/intentos` | Registra una respuesta y devuelve el resultado con retroalimentación | 201 (nuevo) · 200 (idempotente) · 400 · 403 · 404 · 409 · 422 |

Cuerpo del intento:

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

Reglas:

- El servidor deriva `numeroIntento`; no lo envía el cliente.
- Solo el primer intento actualiza la racha y el nivel.
- El segundo intento es pedagógico y no cuenta como dominio.
- Un primer fallo devuelve pista y error común; el segundo fallo devuelve la explicación completa.
- Cada intento guarda los cinco datos de trazabilidad de CA-08: estudiante, ejercicio, resultado, fecha y tiempo empleado (además de `pistaUsada` y la clave de idempotencia).
- La clave de idempotencia es única por usuario. Antes de crearla se bloquea la fila del usuario, así que dos envíos simultáneos del mismo intento se atienden en serie: el primero responde 201 y los demás 200 con el mismo `intentoId`, sin duplicar registros.
- La respuesta de `siguiente` nunca incluye `esCorrecta`, `respuestaNumerica` ni `explicacion`.

El contrato completo, las reglas del motor, los niveles, las pruebas manuales y el registro de archivos están en `docs/code/motor-practica-adaptativa.md`. La retroalimentación, la trazabilidad y la idempotencia se detallan en `docs/code/retroalimentacion-e-idempotencia.md`.

### Banco semilla

- 90 ejercicios: 45 de `CALCULO_DIFERENCIAL` y 45 de `CALCULO_INTEGRAL`.
- 5 por asignatura × competencia × nivel.
- Fuente: `docs/datos/ejercicios_practica.csv`.
- Migración: `V6__datos_ejercicios_semilla.sql`.
- Los contenidos docentes nuevos se crean por la aplicación; no se agregan a Flyway.
- La administración docente es la historia #132 y no forma parte de las tareas 82, 87, 90 y 95.

### Niveles

- Nivel inicial: 1 sin diagnóstico; BAJO → 1, MEDIO → 2, ALTO → 3.
- N=3 y M=3 configurables.
- Inicialización por evento después de finalizar el diagnóstico, con respaldo perezoso.
- El historial de cambios vive en `historial_nivel`.

Ver `docs/code/motor-practica-adaptativa.md`.

## 14. Grupos académicos (API)

Los grupos se gestionan con el docente autenticado como responsable; el servidor obtiene su identidad del `sub` del JWT y no acepta un identificador de docente desde el cliente. `ADMIN` puede consultar cualquier grupo e inscribir estudiantes, mientras cada docente solo puede consultar e inscribir en sus propios grupos. Los estudiantes no tienen acceso a estos endpoints.

| Método y ruta | Qué hace | Roles | Respuestas |
|---|---|---|---|
| `POST /api/grupos` | Crea un grupo. Cuerpo: `{"codigo":"CALCULO-2026-1","nombre":"Cálculo Diferencial - Grupo 01"}` | `DOCENTE` | 201 · 400 · 401 · 403 · 409 |
| `GET /api/grupos` | Lista los grupos propios; `ADMIN` lista todos | `DOCENTE`, `ADMIN` | 200 · 401 · 403 |
| `GET /api/grupos/{grupoId}/estudiantes` | Consulta los estudiantes de un grupo autorizado | `DOCENTE` responsable, `ADMIN` | 200 · 401 · 403 (grupo ajeno) · 404 |
| `POST /api/grupos/{grupoId}/estudiantes` | Inscribe por correo. Cuerpo: `{"correo":"u20201234567@usco.edu.co"}` | `DOCENTE` responsable, `ADMIN` | 201 · 400 · 401 · 403 · 404 · 409 · 422 |

Un grupo tiene un docente responsable y se relaciona con sus estudiantes inscritos mediante `grupo_estudiante`; la clave compuesta impide duplicar la misma inscripción. Los códigos se guardan en mayúsculas y son únicos. Solo pueden inscribirse cuentas activas con rol `ESTUDIANTE`; repetir una inscripción devuelve 409. Un docente no puede consultar ni inscribir estudiantes en un grupo de otro docente: esas operaciones responden 403. `ADMIN` puede consultar cualquier grupo e inscribir en cualquiera. Los estudiantes no pueden gestionar grupos. Swagger describe las solicitudes, respuestas y restricciones de acceso.

## 15. Mi progreso (API)

`GET /api/progreso/me` devuelve los indicadores del estudiante autenticado y requiere un JWT con rol `ESTUDIANTE` (`Authorization: Bearer <token>`). El usuario se identifica a partir del token; no se recibe un `usuarioId`. Sin autenticación responde 401 y con un rol distinto, 403.

La respuesta se compone de los indicadores generales y tres listas/datos de resumen:

```json
{
  "totalIntentos": 8,
  "ejerciciosCompletados": 5,
  "nivelGeneral": "INTERMEDIO",
  "porcentajeGeneral": 65.00,
  "evaluacionParcial": true,
  "dimensiones": [
    {
      "competenciaId": 1,
      "codigo": "ALGEBRAICA",
      "nombre": "Competencia algebraica",
      "intentosEvaluados": 4,
      "intentosCorrectos": 3,
      "porcentajeAciertos": 75.00,
      "nivel": "INTERMEDIO",
      "porcentajeDiagnostico": 60.00,
      "variacionPuntosPorcentuales": 15.00,
      "estadoVariacion": "MEJORA",
      "sinActividad": false
    }
  ],
  "tendenciaSemanal": [
    {
      "anio": 2026,
      "semana": 44,
      "fechaInicio": "2026-10-26",
      "etiqueta": "Semana 44 (26 oct)",
      "intentosEvaluados": 4,
      "intentosCorrectos": 3,
      "porcentajeAciertos": 75.00
    }
  ]
}
```

El ejemplo muestra la forma del DTO, no datos fijos del sistema. `dimensiones` contiene las competencias del catálogo. Sus porcentajes se calculan como aciertos sobre intentos de práctica evaluados de esa dimensión; los intentos correctos cuentan una vez en `ejerciciosCompletados`, que representa ejercicios distintos acertados en el primer o segundo intento. `totalIntentos` cuenta los registros de intentos.

Los niveles de dominio por dimensión y general usan el porcentaje de aciertos: `INICIAL` (<40 %), `BASICO` (40 a <60 %), `INTERMEDIO` (60 a <80 %) y `AVANZADO` (80–100 %). Para consolidar el nivel y porcentaje general se requieren **al menos tres intentos evaluados por dimensión**. El porcentaje general es el promedio simple de los porcentajes de las dimensiones que alcanzaron el umbral; no pondera cada dimensión por su cantidad de intentos. Si hay dimensiones consolidadas pero no todas alcanzaron el umbral, `evaluacionParcial` es `true`. Si ninguna alcanzó tres intentos, el nivel es `SIN_EVALUAR` y el porcentaje general es `null`.

Para cada dimensión, `porcentajeDiagnostico` corresponde al último diagnóstico finalizado. `variacionPuntosPorcentuales` es la diferencia entre el porcentaje de práctica y el diagnóstico, y `estadoVariacion` puede ser `MEJORA`, `RETROCESO`, `SIN_CAMBIO`, `DIAGNOSTICO_PENDIENTE` o `PRACTICA_PENDIENTE`. La variación, los porcentajes y el nivel quedan nulos cuando no se pueden calcular; `sinActividad` señala que no hay intentos de práctica evaluados para esa dimensión. Con cero intentos globales, los contadores son cero, el nivel general es `SIN_EVALUAR`, el porcentaje general es nulo y la tendencia semanal es una lista vacía. La tendencia solo incluye semanas con intentos; no agrega semanas sin actividad. Cada registro semanal informa año, semana ISO, fecha de inicio, etiqueta, intentos, aciertos y porcentaje.

## 16. Analítica de grupos (API)

Solo el docente dueño del grupo puede consultar sus indicadores. El servidor obtiene el docente del `sub` del JWT y valida la pertenencia del grupo; un docente ajeno recibe 403 y un grupo inexistente, 404. `ADMIN` y `ESTUDIANTE` reciben 403.

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/analitica/grupos/{grupoId}?competencia=&desde=&hasta=` | Resumen del grupo: total de estudiantes, estudiantes con actividad, promedio general, promedio por dimensión, distribución de categorías, estudiantes en riesgo y tabla de estudiantes | 200 · 400 (competencia inválida) · 401 · 403 · 404 · 422 (`desde` posterior a `hasta`) |
| `GET /api/analitica/grupos/{grupoId}/estudiantes/{estudianteId}?competencia=&desde=&hasta=` | Detalle de un estudiante del grupo: práctica por dimensión y último diagnóstico | 200 · 400 · 401 · 403 · 404 (estudiante fuera del grupo) · 422 |

- **Periodo:** `desde` y `hasta` son fechas `AAAA-MM-DD`, ambas incluidas y en hora de Colombia. Si se omiten, se usa todo el historial. Los filtros se aplican a `intento_practica.creado_en` y a `intento_diagnostico.finalizado_en`.
- **Competencia:** si se envía, solo se calculan las métricas de esa dimensión; la distribución de categorías sigue siendo global.
- **Promedio por dimensión:** promedio simple de los porcentajes individuales de los estudiantes con actividad en esa dimensión.
- **Categorías:** última categoría de diagnóstico finalizado por estudiante (`ALTO_RIESGO`, `RIESGO_MODERADO`, `NIVEL_ADECUADO`) o `SIN_DIAGNOSTICO`.
- **Estudiante en riesgo:** último diagnóstico `ALTO_RIESGO` o promedio de práctica menor al 50 %. Un estudiante sin actividad se informa con `sinActividad: true` y no se marca en riesgo por ese solo hecho.
- **Detalle:** incluye las dimensiones del estudiante y el último diagnóstico con sus resultados por competencia. La pantalla reutiliza `GET /api/historial?estudianteId=…` para el historial.
- **Indicadores derivados:** el resumen agrega `dimensionMasDebil`, `topEjerciciosErrores` (5 ejercicios con más errores) y `evolucionGeneral` (promedio semanal del grupo). Se calculan con los mismos filtros de competencia y fechas.

El contrato y las decisiones de implementación están en [`docs/code/indicadores-grupo-docente.md`](code/indicadores-grupo-docente.md).

## 17. Gestión de contenidos y ejercicios (API)

Solo para `DOCENTE`. Cada docente administra los contenidos y ejercicios que creó (`creado_por`); los datos semilla son de solo lectura. La identidad sale del JWT.

### Contenidos

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/contenidos?asignatura=&competencia=&activo=&q=` | Lista los contenidos propios con su número de ejercicios | 200 · 400 · 401 · 403 |
| `POST /api/contenidos` | Crea un contenido. Cuerpo: competencia, asignatura, tema, título y descripción | 201 · 400 · 401 · 403 · 409 |
| `PUT /api/contenidos/{id}` | Edita tema, título y descripción | 200 · 400 · 401 · 403 · 404 |
| `PATCH /api/contenidos/{id}/estado` | Activa o desactiva. Cuerpo: `{"activo":false}` | 200 · 400 · 401 · 403 · 404 |
| `DELETE /api/contenidos/{id}` | Elimina un contenido sin ejercicios; si tiene, responde 409 y se desactiva | 204 · 401 · 403 · 404 · 409 |

La competencia y la asignatura de un contenido no se cambian después de crearlo para no romper la coherencia de sus ejercicios.

### Ejercicios

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/ejercicios?contenidoId=&competencia=&nivel=&tipo=&activo=&q=` | Lista los ejercicios propios | 200 · 400 · 401 · 403 |
| `POST /api/ejercicios` | Crea un ejercicio en un contenido propio | 201 · 400 · 401 · 403 · 404 · 409 · 422 |
| `PUT /api/ejercicios/{id}` | Edita un ejercicio propio | 200 · 400 · 401 · 403 · 404 · 409 · 422 |
| `PATCH /api/ejercicios/{id}/estado` | Activa o desactiva. Cuerpo: `{"activo":false}` | 200 · 400 · 401 · 403 · 404 |
| `DELETE /api/ejercicios/{id}` | Elimina un ejercicio sin intentos; si tiene, responde 409 y se desactiva | 204 · 401 · 403 · 404 · 409 |

Reglas de validación:

- `OPCION_MULTIPLE`: exactamente cuatro opciones A–D y exactamente una correcta; sin respuesta numérica.
- `NUMERICA`: respuesta y tolerancia obligatorias, tolerancia mayor que cero; sin opciones.
- Código único; si se omite al crear, se genera uno.
- Si un ejercicio ya tiene intentos, solo se pueden cambiar enunciado, nivel, pista, explicación y error común. Tipo, opciones, respuesta o contenido quedan bloqueados.
- El motor de práctica ya filtra ejercicios y contenidos activos; un ejercicio desactivado no aparece en `GET /api/practica/siguiente`.

El detalle técnico está en [`docs/code/gestion-actividades-contenidos.md`](code/gestion-actividades-contenidos.md).

## 18. Administración de usuarios y panel (API)

Solo para `ADMIN`.

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/admin/usuarios?buscar=&rol=&estado=` | Lista usuarios con búsqueda y filtros opcionales | 200 · 401 · 403 |
| `POST /api/admin/usuarios` | Crea el usuario y envía un enlace de un solo uso para configurar la contraseña | 201 · 400 · 401 · 403 · 409 · 422 · 503 |
| `POST /api/admin/usuarios/{id}/reenviar-activacion` | Reenvía el enlace mientras la cuenta siga pendiente | 204 · 401 · 403 · 404 · 409 |
| `POST /api/auth/activar-cuenta` | Configura la contraseña usando el token enviado por correo | 204 · 400 · 404 · 422 |
| `PUT /api/admin/usuarios/{id}` | Edita nombres, apellidos, rol y programa; no cambia el correo | 200 · 400 · 401 · 403 · 404 · 422 |
| `PATCH /api/admin/usuarios/{id}/estado` | Activa o desactiva. Cuerpo: `{"estado":"INACTIVO"}` | 200 · 400 · 401 · 403 · 404 · 422 |
| `GET /api/admin/resumen` | Conteos de usuarios por rol, grupos, contenidos y ejercicios activos | 200 · 401 · 403 |
| `GET /api/admin/actividades` | Catálogo de contenidos y ejercicios de todos los docentes, sin claves ni explicaciones | 200 · 401 · 403 |

El administrador no puede desactivarse ni quitarse el rol ADMIN. Un usuario INACTIVO no puede iniciar sesión. Los grupos y sus estudiantes se consultan con `GET /api/grupos` y `GET /api/grupos/{id}/estudiantes`, que admiten ADMIN.

## 19. Invitaciones de estudiantes (API)

Solo `DOCENTE` dueño del grupo o `ADMIN`.

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/grupos/{grupoId}/invitaciones` | Lista invitaciones con estado `INVITADO`, `REGISTRADO` o `VENCIDA` | 200 · 401 · 403 · 404 |
| `POST /api/grupos/{grupoId}/invitaciones` | Carga un listado `{estudiantes:[{correo,nombres,apellidos}]}`, valida todo y envía los enlaces | 201 · 400 · 401 · 403 · 409 · 422 · 503 correo no enviado |
| `POST /api/grupos/{grupoId}/invitaciones/{id}/reenviar` | Genera un token nuevo y reenvía el correo | 200 · 401 · 403 · 404 · 409 · 503 correo no enviado |
| `GET /api/auth/invitacion?token=…` | Público: datos para precargar el registro | 200 · 404 · 422 |

Las invitaciones vencen a los 7 días, son de un solo uso y se guardan hasheadas (SHA-256). Al registrarse se crea el usuario y la fila de `grupo_estudiante` en la misma transacción. Un estudiante puede pertenecer a varios grupos.

### Correo transaccional con Resend

Todos los correos se envían por HTTPS con `POST https://api.resend.com/emails`.
Configura `RESEND_API_KEY` y, si necesitas cambiar el remitente, `MAIL_FROM`.
La API Key no se registra ni se incluye en el frontend. El dominio del remitente
debe estar verificado en Resend.

Los mensajes incluyen una versión HTML con la identidad visual USCO y una
versión de texto plano. Las plantillas están en
`src/main/resources/templates/email`; el logo se sirve desde el `FRONTEND_URL`
configurado para la aplicación.

Si falta la clave, Resend rechaza la petición o no hay conexión, los endpoints
responden 503 y no simulan un envío exitoso. Las invitaciones grupales quedan
guardadas para poder reenviarlas desde el panel; si un lote falla parcialmente,
puede haber correos previos ya entregados.
