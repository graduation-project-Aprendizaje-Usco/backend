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

| Módulo | Responsable principal |
|---|---|
| `security/`, `usuario/` (auth, administración) | Jorel Vargas |
| Esquema, migraciones, `diagnostico/`, indicadores en `progreso/` y `analitica/` | Juan David Polanco |
| Motor adaptativo y niveles en `practica/`, `contenido/`, historial | Jorel Vargas |
| Registro de intentos en `practica/` | Juan David Polanco |
| CI (`.github/workflows/`) | Fernando Pérez |

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

Pendiente (bloque "registro por invitación"): solo podrán registrarse los estudiantes invitados por un docente y que estén en el listado de su curso.

## 11. Limitaciones conocidas del MVP

| Limitación | Impacto | Plan |
|---|---|---|
| El token JWT sigue siendo válido hasta que vence (2 h) aunque el usuario sea desactivado o cambie de rol. Solo `GET /api/auth/me` consulta la base | Un usuario desactivado conserva acceso hasta 2 h. El frontend valida la sesión con `/me` al abrir la app | Revisarlo cuando exista la gestión de usuarios del administrador |
| No hay límite de intentos de inicio de sesión | Expuesto a ataques de fuerza bruta | Sprint de hardening (Sprint 4) |
