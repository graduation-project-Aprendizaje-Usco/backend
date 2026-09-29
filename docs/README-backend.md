# Aprendizaje USCO — Backend

API REST de **Aprendizaje USCO**, plataforma web de diagnóstico, práctica adaptativa y analítica de aprendizaje para Cálculo Diferencial (Proyecto Integrador II, Ingeniería de Software, Universidad Surcolombiana).

| | |
|---|---|
| Stack | Java 21 · Spring Boot 4.1 · Spring Security (JWT) · Spring Data JPA · PostgreSQL 17-apine · Flyway |
| Frontend | [aprendizaje-usco-frontend](https://github.com/aprendizaje-usco/aprendizaje-usco-frontend) |
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
git clone https://github.com/aprendizaje-usco/aprendizaje-usco-backend.git
cd aprendizaje-usco-backend
git checkout develop

# Docker Desktop debe estar abierto
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Al arrancar, Spring Boot levanta el contenedor de PostgreSQL definido en `compose.yaml` y Flyway aplica las migraciones.

| Comprobación | URL | Resultado esperado |
|---|---|---|
| Salud | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| Documentación de la API | http://localhost:8080/swagger-ui.html | Swagger UI |

**Sin Docker:** instala PostgreSQL 17, crea la base `aprendizaje_usco` con usuario `usco` / `usco_dev` y agrega `spring.docker.compose.enabled: false` en tu `application-dev.yml` local (sin subir ese cambio).

## 3. Comandos útiles

| Comando | Qué hace |
|---|---|
| `./mvnw spring-boot:run` | Corre la API en el puerto 8080 (perfil `dev`) |
| `./mvnw test` | Pruebas unitarias y de integración (Testcontainers, requiere Docker) |
| `./mvnw clean package` | Genera el `.jar` en `target/` |
| `./mvnw flyway:info` | Estado de las migraciones (opcional, si se configura el plugin) |

## 4. Configuración

| Archivo | Uso |
|---|---|
| `application.yml` | Configuración común a todos los entornos |
| `application-dev.yml` | Base de datos local |
| `application-test.yml` | Pruebas automatizadas |

Variables de entorno (en staging se definen en el servicio de hosting, **nunca en el repositorio**):

| Variable | Descripción | Valor en desarrollo |
|---|---|---|
| `JWT_SECRET` | Llave para firmar los tokens (mínimo 32 caracteres) | valor por defecto de `application.yml` |
| `CORS_ORIGINS` | Orígenes permitidos para el frontend | `http://localhost:5173` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Conexión a la base en staging | — |

## 5. Estructura

Organización **por módulo funcional**, con capas dentro de cada módulo (RNF-08). Cada integrante trabaja principalmente en su módulo, lo que reduce los conflictos de merge.

```
src/main/java/co/edu/usco/aprendizaje/
├── config/        CORS, OpenAPI, beans generales
├── security/      SecurityConfig, servicio JWT, filtros
├── common/        excepciones, manejador global de errores, utilidades
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
  entity/       entidades JPA

src/main/resources/db/migration/   migraciones Flyway
```

## 6. Convenciones de la API

- Todas las rutas empiezan por `/api` y usan sustantivos en plural: `/api/diagnosticos`, `/api/ejercicios/{id}/intentos`.
- Rutas públicas: solo `/api/auth/**`, `/actuator/health` y Swagger. Todo lo demás exige `Authorization: Bearer <token>`.
- Restricción por rol con `@PreAuthorize("hasRole('DOCENTE')")` en el controlador o servicio. Roles: `ESTUDIANTE`, `DOCENTE`, `ADMIN`.
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

## 7. Base de datos y migraciones

- El esquema **solo** lo cambia Flyway; Hibernate está en `ddl-auto: validate`.
- Nombre de archivo con fecha y hora para evitar choques: `V202610011430__crear_tabla_intento.sql`.
- **Nunca** edites una migración que ya está en `develop`: crea una nueva.
- Datos de prueba (usuarios demo, 24 ítems del diagnóstico) van en migraciones propias, marcadas en el nombre: `V202610021000__datos_items_diagnostico.sql`.

Usuarios de prueba (solo desarrollo y staging):

| Rol | Correo | Contraseña |
|---|---|---|
| Estudiante | _por definir_ | _por definir_ |
| Docente | _por definir_ | _por definir_ |
| Administrador | _por definir_ | _por definir_ |

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
| `Connection refused` al arrancar | Docker Desktop cerrado, o el puerto 5432 ocupado por otro PostgreSQL |
| `Schema-validation: missing table` | Falta la migración de esa entidad, o la migración tiene otro nombre de columna |
| `Validate failed: Migration checksum mismatch` | Se editó una migración ya aplicada. Revierte el cambio y crea una migración nueva |
| 401 en todos los endpoints | Falta el header `Authorization`, o el token expiró |
| Error de CORS desde el frontend | Revisa `CORS_ORIGINS` |
