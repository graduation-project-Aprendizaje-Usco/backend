# Hardening de seguridad por rol (#148)

**Fecha:** 10 de noviembre de 2026.
**Entorno:** local con Testcontainers.
**Estado:** controles implementados y probados. Rotación de secretos de staging pendiente de #33.

## 1. Matriz endpoint × rol

`MatrizSeguridadTest` verifica los códigos de respuesta con tokens reales:

| Endpoint | Sin token | ESTUDIANTE | DOCENTE | ADMIN |
|---|---|---|---|---|
| `GET /api/progreso/me` | 401 | 200 | 403 | 403 |
| `GET /api/diagnosticos/intentos/actual` | 401 | 200/204 | 403 | 403 |
| `GET /api/practica/siguiente` | 401 | 200 | 403 | 403 |
| `GET /api/grupos` | 401 | 403 | 200 | 200 |
| `GET /api/admin/usuarios` | 401 | 403 | 403 | 200 |
| `GET /api/admin/resumen` | 401 | 403 | 403 | 200 |
| `GET /api/admin/actividades` | 401 | 403 | 403 | 200 |

Además, `ProteccionDeEndpointsTest` falla si aparece un endpoint sin `@PreAuthorize` que no esté justificado como público.

## 2. Cabeceras de seguridad

`SecurityHeadersFilter` agrega a las respuestas:

| Cabecera | Valor |
|---|---|
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `Referrer-Policy` | `no-referrer` |
| `Permissions-Policy` | `camera=(), microphone=(), geolocation=()` |
| `Cache-Control` | `no-store` en `/api/**` |

`SecurityHeadersTest` valida las cabeceras en un endpoint público y en el login.

## 3. Datos sensibles

`MatrizSeguridadTest` verifica que las respuestas no contienen:

- `passwordHash` ni `password` en usuarios de administración.
- `esCorrecta` en el diagnóstico y en la práctica.
- `respuestaNumerica` ni `explicacion` en la práctica.
- `respuestaNumerica`, `explicacion` ni `opciones` en el catálogo de actividades del administrador.

## 4. Validación de entradas

- Los DTO de escritura usan `@Valid` y anotaciones de Jakarta Validation.
- Los correos se normalizan a minúsculas antes de guardar o comparar.
- Las invitaciones validan formato de correo estudiantil, duplicados del listado y coincidencia con el token.
- Los intentos de práctica incluyen clave de idempotencia obligatoria.

## 5. Hallazgos y pendientes

| ID | Hallazgo | Estado |
|---|---|---|
| S-01 | Faltaban cabeceras `Referrer-Policy` y `Permissions-Policy` | Corregido |
| S-02 | No había una matriz automatizada endpoint × rol | Corregido |
| S-03 | Respuestas sensibles no se verificaban de forma centralizada | Corregido |
| S-04 | No hay límite de intentos de login (fuerza bruta) | Conocido; fuera del alcance del MVP |
| S-05 | Rotación de secretos en staging | Pendiente de #33 |

## 6. Comandos

```powershell
.\mvnw.cmd "-Dtest=MatrizSeguridadTest,SecurityHeadersTest,ProteccionDeEndpointsTest" test
.\mvnw.cmd verify
```
