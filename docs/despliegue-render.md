# Despliegue del backend en Render con Neon PostgreSQL

Esta guía deja el backend preparado para ejecutarse en Render usando una base
PostgreSQL alojada en Neon. No incluye credenciales, dominios ni valores reales:
todo se configura en el panel de Render.

## 1. Configuración

| Archivo | Uso |
|---|---|
| `src/main/resources/application.properties` | Única configuración de ejecución; local y Render usan las mismas propiedades con valores del entorno. |
| `.env` | Solo local; está en `.gitignore` y nunca se sube al repositorio. |

En Render no configures `SPRING_FLYWAY_LOCATIONS` con `db/dev-data`: allí deben
aplicarse solo las migraciones de `src/main/resources/db/migration`.

## 2. Variables de entorno en Render

| Variable | Obligatoria | Uso |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Sí | `jdbc:postgresql://<host>:5432/<base>?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Sí | Usuario de Neon. |
| `SPRING_DATASOURCE_PASSWORD` | Sí | Contraseña de Neon. Déjala solo en Render. |
| `JWT_SECRET` | Sí | Llave HS256 de mínimo 32 caracteres. Genera una aleatoria. |
| `BOOTSTRAP_ADMIN_EMAIL` | No | Correo para crear el administrador inicial si todavía no existe. |
| `BOOTSTRAP_ADMIN_PASSWORD` | No | Contraseña inicial (mínimo 12 caracteres; máximo 72 bytes UTF-8). Déjala solo en Render. Configúrala junto con el correo. |
| `BOOTSTRAP_ADMIN_NAMES` / `BOOTSTRAP_ADMIN_SURNAMES` | No | Nombre y apellido; por defecto `Administrador` / `Sistema`. |
| `JWT_EXPIRATION_MINUTES` | No | Duración del token. Por defecto `120`. |
| `FRONTEND_URL` | Sí | Origen exacto del frontend en Vercel, **sin barra final**. Se usa para CORS, invitaciones y activación de cuentas. |
| `CORS_ORIGINS` | No | Orígenes CORS separados por coma. Si se omite, se usa `FRONTEND_URL`. |
| `SPRING_FLYWAY_LOCATIONS` | No | Déjala sin definir (por defecto solo `classpath:db/migration`). |
| `SPRING_DOCKER_COMPOSE_ENABLED` | Sí | Define `false`; Render no debe iniciar Docker Compose. |
| `PORT` | No | Render la asigna automáticamente. La aplicación ya la lee; **no la definas a mano**. |
| `MAIL_*` | Para crear usuarios | SMTP para invitaciones y configuración de contraseña. Si no se configura, las invitaciones de grupo se escriben en el log, pero el alta administrativa se rechaza y no crea la cuenta. |

Notas importantes:

- `FRONTEND_URL` debe ser el origen exacto, por ejemplo `https://<tu-dominio-de-vercel>`.
  No agregues `/` al final ni rutas como `/login`.
- Usa la misma convención `SPRING_DATASOURCE_*` que en local, pero define en Render
  la URL de Neon con `sslmode=require` y las credenciales de Neon.
- No escribas estos valores en el repositorio.

Genera `JWT_SECRET` con un valor aleatorio:

```bash
openssl rand -base64 48
```

## 3. Preparar Neon

1. Crea el proyecto y la base de datos en Neon.
2. Copia los datos de conexión: host, puerto (`5432`), base, usuario y contraseña.
   Si usas el endpoint con pooler de Neon, copia el host del pooler.
3. No hace falta crear tablas a mano: Flyway aplica `V1` a `V8` al arrancar.
4. Neon exige TLS. Incluye `sslmode=require` en `SPRING_DATASOURCE_URL`; no lo
   desactives.
5. Usa una base vacía (o una que ya tenga el historial de Flyway). No apuntes a
   una base con tablas creadas por fuera de Flyway sin revisar el historial.

## 4. Crear el servicio en Render

### Opción A: Docker (recomendada)

El repositorio incluye un `Dockerfile` que compila con Maven y ejecuta el `.jar`
sobre Java 21. En Render:

1. Crea un **Web Service** desde el repositorio del backend.
2. Selecciona el entorno Docker. Render usará el `Dockerfile`.
3. Define las variables de la sección 2.
4. Configura el health check en `/actuator/health`.
5. Despliega.

### Opción B: build nativo de Java

1. Build command: `./mvnw clean package -DskipTests`.
2. Start command: `java -jar target/*.jar`.
3. Define el resto de variables de la sección 2.
4. Health check: `/actuator/health`.

El build debe usar `-DskipTests`: las pruebas de integración usan Testcontainers
y necesitan un Docker daemon que no está disponible durante el build de Render.

## 5. Configuración efectiva en Render

- Conecta a Neon con la URL JDBC y credenciales definidas en Render.
- Escucha en `server.port=${PORT:8080}` y en `0.0.0.0`.
- Mantiene `spring.flyway.enabled=true` con `clean-disabled=true`: Flyway solo
  avanza con migraciones nuevas, nunca borra datos.
- Mantiene `spring.jpa.hibernate.ddl-auto=validate`: Hibernate no crea, altera ni
  elimina tablas; solo valida que las entidades coincidan con el esquema.
- Desactiva Docker Compose (`SPRING_DOCKER_COMPOSE_ENABLED=false`).
- Si se configuran `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`, crea
  la cuenta inicial solo si el correo no existe; no modifica cuentas existentes.
- Configura CORS con `FRONTEND_URL`.
- Configura los enlaces de invitación con `${FRONTEND_URL}/registro`.
- No carga `db/dev-data`.

## 6. Verificación después del despliegue

1. `GET https://<backend-de-render>/actuator/health` debe responder `UP`.
2. En los logs debe verse que Flyway aplicó las migraciones sin errores.
3. Desde el dominio de Vercel, comprueba que una petición a `/api/...` no falla
   por CORS. El origen debe coincidir exactamente con `FRONTEND_URL`.
4. Abre un enlace de invitación generado por el backend y confirma que usa el
   dominio de `FRONTEND_URL`.
5. Inicia sesión con un usuario real y recorre un flujo básico.

## 7. Seguridad y operación

- Los secretos (`SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`,
  `BOOTSTRAP_ADMIN_PASSWORD`, credenciales SMTP) viven solo en
  Render. No los subas al repositorio.
- Rotar `JWT_SECRET` invalida los tokens emitidos: los usuarios deben volver a
  iniciar sesión.
- No cambies `ddl-auto` a `update`, `create` ni `drop`: el esquema se versiona
  únicamente con Flyway.
- Para corregir datos o esquema, crea una migración nueva; no edites una
  migración que ya se aplicó en Neon.
- Si cambias el dominio del frontend en Vercel, actualiza `FRONTEND_URL` y
  vuelve a desplegar el backend.
