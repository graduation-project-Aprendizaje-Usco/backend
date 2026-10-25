# Aprendizaje USCO — Backend

[![CI](https://github.com/aprendizaje-usco-project/aprendizaje-usco-backend/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/aprendizaje-usco-project/aprendizaje-usco-backend/actions/workflows/ci.yml?query=branch%3Adevelop)

API REST de **Aprendizaje USCO**, plataforma web de diagnóstico, práctica adaptativa y analítica de aprendizaje para Cálculo Diferencial (Proyecto Integrador II, Ingeniería de Software, Universidad Surcolombiana).

| | |
|---|---|
| Stack | Java 21 · Spring Boot 4.1 · Spring Security (JWT) · Spring Data JPA · PostgreSQL 17 · Flyway |
| Frontend | [aprendizaje-usco-frontend](https://github.com/aprendizaje-usco-project/aprendizaje-usco-frontend) |
| Tablero | Jira, proyecto `AU` |
| Convenciones | [CONTRIBUTING.md](CONTRIBUTING.md) |

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
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Al arrancar con el perfil `dev`, Spring Boot levanta el contenedor de PostgreSQL definido en `compose.yaml` (publicado en el puerto 5433 de tu equipo), Flyway aplica las migraciones y carga los usuarios de prueba.

| Comprobación | URL | Resultado esperado |
|---|---|---|
| Salud | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| Documentación de la API | http://localhost:8080/swagger-ui.html | Swagger UI |

**Sin Docker:** instala PostgreSQL 17, crea la base `aprendizaje_usco` con usuario `usco` / `usco_dev` (puerto 5432, como en `.env.template`) y agrega la línea `spring.docker.compose.enabled=false` a tu `.env`. El `.env` nunca se sube al repositorio.

## 3. Comandos útiles

| Comando | Qué hace |
|---|---|
| `./mvnw spring-boot:run` | Corre la API en el puerto 8080 (perfil `dev`) |
| `./mvnw test` | Pruebas unitarias y de integración (Testcontainers, requiere Docker). Es lo mismo que corre CI en cada PR |
| `./mvnw clean package` | Genera el `.jar` en `target/` |
| `./mvnw flyway:info` | Estado de las migraciones (opcional, si se configura el plugin) |

## 4. Configuración

| Archivo | Uso |
|---|---|
| `src/main/resources/application.properties` | Configuración común a todos los entornos. **Sin secretos ni llaves por defecto** |
| `src/main/resources/application-dev.properties` | Solo desarrollo local: usuarios de prueba, SQL en consola y una llave JWT de desarrollo |
| `src/test/resources/config/application.properties` | Solo pruebas automáticas: llave JWT de pruebas (se suma al `application.properties` principal) |
| `.env` (no se sube; copia de `.env.template`) | Tus valores locales de base de datos y pgAdmin |

Variables de entorno (en staging se definen en el servicio de hosting, **nunca en el repositorio**):

| Variable | Descripción | Valor en desarrollo |
|---|---|---|
| `JWT_SECRET` | Llave para firmar los tokens (mínimo 32 caracteres). **Obligatoria fuera del perfil `dev`**: sin ella la app no arranca | la pone `application-dev.properties` |
| `JWT_EXPIRATION_MINUTES` | Duración del token en minutos | `120` |
| `CORS_ORIGINS` | Orígenes permitidos para el frontend | `http://localhost:5173` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Conexión a la base en staging | — |

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
src/main/resources/db/dev-data/    datos de prueba (solo perfil dev)
```

## 6. Convenciones de la API

- Todas las rutas empiezan por `/api` y usan sustantivos en plural: `/api/diagnosticos`, `/api/ejercicios/{id}/intentos`.
- Rutas públicas: solo `POST /api/auth/login`, `POST /api/auth/registro`, `/actuator/health` y Swagger. Todo lo demás (incluido `GET /api/auth/me`) exige `Authorization: Bearer <token>`.
- Restricción por rol con `@PreAuthorize("hasRole('DOCENTE')")` en el controlador (método o clase). Roles: `ESTUDIANTE`, `DOCENTE`, `ADMIN`.
- **Todo endpoint nuevo lleva `@PreAuthorize`.** La regla general de seguridad es solo "con sesión", así que sin la anotación cualquier rol entraría. `ProteccionDeEndpointsTest` falla si falta; los públicos a propósito se agregan a su lista de excepciones.
- `GET /api/ping/{rol}` es un endpoint de prueba del acceso por rol: solo existe con los perfiles `dev` y `test`.
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
- Las migraciones iniciales son `V1` y `V2`. Las nuevas llevan fecha y hora en la versión para evitar choques entre ramas: `V202610011430__crear_tabla_intento.sql`.
- **Nunca** edites una migración que ya está en `develop`: crea una nueva.
- Datos que necesitan todos los entornos (roles, competencias, los 24 ítems del diagnóstico) van en migraciones versionadas de `db/migration`.
- Datos de prueba que no deben llegar a producción (usuarios demo) van en `db/dev-data` como migración repetible `R__…sql`, que solo se carga con el perfil `dev`.

Usuarios de prueba (solo perfil `dev`):

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
| `Connection refused` al arrancar | Docker Desktop cerrado. Sin Docker: PostgreSQL local apagado o `spring.docker.compose.enabled=false` faltante en `.env` |
| `Falta la variable de entorno JWT_SECRET` al arrancar | Arrancaste sin el perfil `dev` (ej. `java -jar`). Usa `./mvnw spring-boot:run` o define `JWT_SECRET` |
| `Schema-validation: missing table` | Falta la migración de esa entidad, o la migración tiene otro nombre de columna |
| `Validate failed: Migration checksum mismatch` | Se editó una migración ya aplicada. Revierte el cambio y crea una migración nueva |
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

Pendiente (tarea #150 "Implementar registro de estudiantes por invitación", ver [tareas-y-responsables.md](tareas-y-responsables.md)): solo podrán registrarse los estudiantes invitados por un docente y que estén en el listado de su grupo.

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
- La respuesta de `siguiente` nunca incluye `esCorrecta`, `respuestaNumerica` ni `explicacion`.

El contrato completo, las reglas del motor, los niveles, las pruebas manuales y el registro de archivos están en `docs/code/motor-practica-adaptativa.md`.

### Banco semilla

- 90 ejercicios: 45 de `CALCULO_DIFERENCIAL` y 45 de `CALCULO_INTEGRAL`.
- 5 por asignatura × competencia × nivel.
- Fuente: `docs/datos/ejercicios_practica.csv`.
- Migración: `V202610221010__datos_ejercicios_semilla.sql`.
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

Los códigos se guardan en mayúsculas y son únicos. Solo pueden inscribirse cuentas activas con rol `ESTUDIANTE`; repetir una inscripción devuelve 409. Swagger describe las solicitudes, respuestas y restricciones de acceso en la configuración existente.
