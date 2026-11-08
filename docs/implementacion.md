# Implementación del backend

Qué se ha construido, para qué sirve, qué decisiones se tomaron y cómo probarlo, **historia por historia**. Complementa al [README](README-backend.md) (cómo correr el proyecto y las convenciones) y a [tareas-y-responsables.md](tareas-y-responsables.md) (qué pide cada tarea). La parte visual está en `docs/implementacion.md` del frontend.

Se actualiza con cada historia que entra a `develop`: una sección nueva, o la existente si cambió.

| Historia | Jira | Responsable | Estado |
|---|---|---|---|
| [Inicio de sesión con JWT](#1-inicio-de-sesión-con-jwt-scrum-16) | SCRUM-16 | Nicolás Pérez | En `develop` |
| [Control de acceso por rol](#2-control-de-acceso-por-rol-scrum-12) | SCRUM-12 | Fernando Pérez (integración: Nicolás Pérez) | En `develop` |
| [Pruebas del diagnóstico](#3-pruebas-del-módulo-de-diagnóstico-scrum-27) | SCRUM-27 | Nicolás Pérez | En `develop` (falta la evidencia manual) |
| [Historial de resultados](#5-historial-de-resultados-scrum-41) | SCRUM-41 (168) | Nicolás Pérez | En `develop` |
| Aplicación de la prueba diagnóstica, puntaje y categoría | SCRUM-22, 23, 24 | Jorel Vargas | En `develop` · ver README §12 |
| Motor de práctica adaptativa | SCRUM-28 a 31 | Juan David Polanco | En `develop` · ver README §13 |
| [Retroalimentación inmediata y registro de intentos](#4-retroalimentación-inmediata-y-registro-de-intentos-110-114) | 110, 114 | Juan David Polanco | En `develop` · ver README §13 |
| Grupos docente–estudiante | SCRUM-159 | Fernando Pérez | En `develop` |

---

## 1. Inicio de sesión con JWT (SCRUM-16)

**Para qué.** Que cada persona entre con su correo y contraseña, y que el backend sepa en cada petición quién es y qué rol tiene, sin guardar sesiones en el servidor.

**Cómo funciona.**

1. `POST /api/auth/login` recibe correo y contraseña. `AuthenticationManager` busca al usuario por correo (`UsuarioDetailsService`) y compara la contraseña con su hash BCrypt.
2. Si coinciden, `JwtService` firma un token (HS256) con `sub` (id), `correo`, `rol`, `iat` y `exp`, y lo devuelve junto con los datos del usuario.
3. El frontend manda ese token en cada petición (`Authorization: Bearer …`). Spring Security lo valida con `JwtDecoder` (firma y vencimiento) antes de llegar al controlador.

**Endpoints** (contrato publicado en SCRUM-81):

| Método y ruta | Acceso | Respuestas |
|---|---|---|
| `POST /api/auth/login` | Público | 200 `{ token, tipo, expiraEn, usuario }` · 400 campos inválidos · 401 «Correo o contraseña incorrectos» |
| `POST /api/auth/registro` | Público | 201 usuario creado (rol ESTUDIANTE) · 400 por campo · 409 correo repetido |
| `GET /api/auth/me` | Con token | 200 usuario del token · 401 sin token, token inválido o usuario inactivo |

**Archivos principales.**

| Archivo | Qué hace |
|---|---|
| `security/UsuarioDetailsService.java` | Busca por correo normalizado; rechaza inactivos igual que a un correo inexistente |
| `security/UsuarioAutenticado.java` | El usuario durante el login (id, correo, nombres, rol); borra el hash al terminar |
| `security/JwtService.java` · `JwtConfig.java` · `JwtProperties.java` | Firma y validación del token; llave y duración desde `app.jwt.*` |
| `security/SecurityConfig.java` | Rutas públicas, validación del token, 401/403 en el formato estándar |
| `modulos/usuario/controller/AuthController.java` · `service/AuthService.java` · `dto/*` | Los tres endpoints y sus DTO |

**Decisiones.**

- **Mensaje genérico en el 401.** Correo inexistente, contraseña errada y usuario inactivo responden lo mismo, para no revelar qué correos están registrados.
- **`expiraEn` son segundos restantes** (ej. 7199), no una fecha. El frontend igual lee `exp` del token.
- **El login y el registro ignoran el header `Authorization`.** Si el navegador guardó un token vencido, el estudiante igual puede volver a entrar (si no, Spring rechazaría el login con 401).
- **Solo `POST /login` y `POST /registro` son públicos**, no todo `/api/auth/**`: así `/me` exige token.
- **El correo se limpia antes de validar** (espacios al copiar y pegar) y se guarda en minúsculas.
- **Llave del token:** `JWT_SECRET`, mínimo 32 caracteres. Solo el perfil `dev` trae una llave por defecto; en staging o producción, sin la variable la app no arranca (revisión de Jorel).
- **Reglas del registro** (revisión de Jorel): correo de estudiante `u` + 11 dígitos + `@usco.edu.co`, contraseña de 8 a 72 caracteres con letra y número, programa académico de la lista cerrada `ProgramaAcademico`.

**Pruebas.** `AutenticacionTest` (contraseña y usuario inactivo), `JwtTest` (token válido, alterado, vencido, mal formado), `AuthServiceTest` y `AuthControllerTest` (login, registro y `/me` por HTTP).

**Cómo probarlo a mano.** Swagger (`/swagger-ui.html`) → `POST /api/auth/login` con `estudiante@aprendizaje.test` / `Estudiante2026*` → copiar el `token` → **Authorize** → `GET /api/auth/me` debe responder 200.

---

## 2. Control de acceso por rol (SCRUM-12)

**Para qué.** Que cada endpoint sea solo para el rol que le corresponde. El backend es la barrera real; el frontend solo evita navegar a lo que no toca.

**Cómo funciona.** El claim `rol` del token se convierte en la autoridad `ROLE_<rol>` (`JwtRoleConverter`, que descarta cualquier rol que no sea ESTUDIANTE, DOCENTE o ADMIN). Los controladores la exigen con `@PreAuthorize("hasRole('DOCENTE')")`. Sin token responde **401**; con un rol sin permiso, **403**, ambos en el formato estándar de error (`RespuestasErrorSeguridad`).

**Para usarlo en un endpoint nuevo:**

```java
@PreAuthorize("hasRole('ESTUDIANTE')")
@GetMapping("/mio")
public Algo mio(@AuthenticationPrincipal Jwt jwt) {
    Long idEstudiante = Long.valueOf(jwt.getSubject()); // el id sale del token, nunca del cliente
    …
}
```

**Historia de la integración.** La rama `scrum_12` traía su propio `JwtDecoder` y una llave en Base64 que chocaban con el login (la app no arrancaba al unirlas). Se integró en `feature/control-acceso` sobre el login: se conservaron `JwtRoleConverter` y `PingController`, y se descartó lo duplicado.

**Pruebas.** `AccesoPorRolTest`: cada rol entra solo a su `/api/ping/{rol}` (200) y recibe 403 en los demás; sin token, 401; un rol desconocido no da permisos. `PingController` solo existe con los perfiles `dev` y `test` (en staging responde 404).

---

## 3. Pruebas del módulo de diagnóstico (SCRUM-27)

El módulo lo construyó Jorel con 63 pruebas (casos borde, puntaje, categorías, tiempo, concurrencia). Esta tarea agregó lo que faltaba y verificó los criterios.

| Subtarea | Qué se hizo | Estado |
|---|---|---|
| SCRUM-143 Flujo completo | `DiagnosticoControllerTest.flujoCompletoDesdeElLoginHastaElResultado`: login real → iniciar → retomar → responder 24 → finalizar → resultado (20/24, NIVEL_ADECUADO) | Hecha |
| SCRUM-144 Casos borde | Cubiertos por las pruebas de Jorel (intento vencido, ajeno, ítem inexistente, finalizar dos veces) | Hecha |
| SCRUM-145 Verificación manual de CA-02 a CA-04 | Ejecutada en local (staging aún no existe). PF-03, PF-04, PF-05 y PV-01 aprobadas; PV-01 tras corregir el frontend. PU-01, PU-07 y PU-12 necesitan estudiantes reales | Falta adjuntar capturas y marcar la matriz |

**Cobertura** de `modulos/diagnostico/service` (criterio ≥ 70 %): **98,8 % de líneas, 92,5 % de ramas**. Se mide sin tocar el `pom.xml`:

```bash
./mvnw org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.13:report
```

El reporte queda en `target/site/jacoco/index.html`.

---

## 4. Retroalimentación inmediata y registro de intentos (110, 114)

**Para qué.** Que cada respuesta a un ejercicio devuelva, en la misma petición, si fue correcta y qué hacer para entender el procedimiento; y que el intento quede guardado sin duplicarse si el navegador lo reenvía.

**Cómo funciona.**

1. `POST /api/practica/intentos` valida el ejercicio y la respuesta, bloquea la fila del usuario y busca si ya existe un intento con esa `claveIdempotencia`.
2. Si existe, devuelve el mismo resultado con **200**. Si no, calcula `numeroIntento`, guarda el intento y, solo en el primer intento, actualiza el nivel y la racha del estudiante en la misma transacción.
3. `FeedbackService` elige el texto según el número de intento y el resultado: primer fallo → pista + error común; segundo fallo → explicación completa + error común; acierto → explicación. Todo viaja en `retroalimentacion` dentro de la respuesta del intento.

| Situación | `retroalimentacion.tipo` | `texto` | `errorComun` |
|---|---|---|---|
| Primer acierto | `EXPLICACION` | explicación del ejercicio | — |
| Primer fallo | `PISTA` | pista | sí |
| Segundo acierto | `EXPLICACION` | explicación del ejercicio | — |
| Segundo fallo | `EXPLICACION` | explicación completa | sí |

**Decisiones.**

- **Trazabilidad completa (CA-08):** cada fila de `intento_practica` guarda `usuario_id`, `ejercicio_id`, `es_correcta`, `creado_en` y `tiempo_seg`; también `pista_usada`, `numero_intento` y el snapshot de competencia y nivel.
- **Idempotencia también bajo concurrencia:** la restricción `UNIQUE (usuario_id, clave_idempotencia)` impide duplicados y el bloqueo pesimista del usuario serializa los envíos simultáneos. Un doble clic o un reintento de red recibe el mismo `intentoId`, no un error.
- **Transacción única:** si falla la actualización de nivel, el intento no queda registrado (`PracticaTransaccionTest`).
- **El segundo intento no cuenta para el nivel**, pero sí se persiste y se puede consultar.

**Pruebas.** `FeedbackServiceTest` (las tres ramas de retroalimentación), `PracticaControllerTest` (pista, explicación, segundo fallo, segundo intento y trazabilidad de los cinco datos), `PracticaIdempotenciaConcurrenteTest` (8 envíos simultáneos con la misma clave → un solo intento) y `PracticaTransaccionTest` (rollback si falla el nivel).

**Cómo probarlo a mano.** Con el perfil `dev`, iniciar sesión como estudiante, pedir `GET /api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL`, enviar una opción incorrecta a `POST /api/practica/intentos` y revisar `retroalimentacion` (debe ser `PISTA`). Repetir el mismo cuerpo con otra `claveIdempotencia` y otra opción incorrecta: debe llegar `EXPLICACION` y `puedeReintentar: false`.

**Detalle técnico completo:** [`docs/code/retroalimentacion-e-idempotencia.md`](code/retroalimentacion-e-idempotencia.md).

---

## 5. Historial de resultados (SCRUM-41)

**Para qué.** Que el estudiante vea todo lo que ha hecho (diagnóstico y ejercicios de práctica) en una sola lista, y que el docente consulte el de los estudiantes de sus grupos (RF-15).

**Endpoint:** `GET /api/historial` (roles ESTUDIANTE y DOCENTE), del más reciente al más antiguo, **20 por página**.

| Parámetro | Para qué |
|---|---|
| `tipo` | `DIAGNOSTICO` o `PRACTICA` (opcional) |
| `competencia` | `ALGEBRAICA`, `LOGICA` o `FUNCIONAL` (opcional) |
| `desde`, `hasta` | `AAAA-MM-DD`, ambos días incluidos, en hora de Colombia (opcionales) |
| `page` | Página desde 0 |
| `estudianteId` | Obligatorio para el docente; el estudiante no lo necesita |

Respuesta: `{ contenido, pagina, tamano, totalElementos, totalPaginas }`. Cada fila trae `tipo`, `id`, `fecha`, `actividad` y la competencia, más:

- **DIAGNOSTICO:** `aciertos`, `total` y `categoria`. Con filtro de competencia, los aciertos de esa competencia (ej. 5 de 8).
- **PRACTICA:** `correcto`, `nivel`, `tiempoSeg` y `pistaUsada`.

| Caso | Respuesta |
|---|---|
| Estudiante pide su historial | 200 |
| Estudiante pide el de otro (`estudianteId` ajeno) | 403 |
| Docente con un estudiante de sus grupos | 200 |
| Docente con un estudiante que no es de sus grupos | 403 |
| Docente sin `estudianteId`, o `desde` posterior a `hasta` | 422 |
| `tipo`, fecha o `page` con formato inválido | 400 |
| Administrador o sin token | 403 / 401 |

**Archivos:** `modulos/progreso/controller/HistorialController.java`, `service/HistorialService.java`, `repository/HistorialRepository.java`, `dto/HistorialItemResponse.java`, `dto/PaginaResponse.java`, `model/TipoActividad.java`.

**Decisiones.**

- **Consulta SQL con `UNION ALL`** sobre `intento_diagnostico` e `intento_practica`: son dos tablas que JPA no relaciona. Los filtros se agregan solo si llegan, y siempre como parámetros (nunca concatenados).
- **Se apoya en tablas de otros módulos:** los intentos de práctica de Juan David (que ya guardan los datos de SCRUM-35) y los grupos de Fernando (SCRUM-159). Si cambian esas tablas, hay que ajustar `HistorialRepository`.
- **Las fechas del filtro se interpretan en hora de Colombia** (`America/Bogota`).

**Pruebas.** `HistorialControllerTest`: 11 pruebas (orden y contenido, aislamiento entre estudiantes, filtros por tipo, competencia y fechas, paginación de 20, permisos de estudiante, docente y administrador).

**Cómo probarlo a mano.** Swagger → `GET /api/historial` con el token de un estudiante que haya presentado el diagnóstico o practicado. Para el docente, crear un grupo (`POST /api/grupos`), inscribir al estudiante (`POST /api/grupos/{id}/estudiantes` con su correo) y consultar con `estudianteId`.

---

## Pendientes conocidos

- **Staging (SCRUM-13):** sin él, la verificación manual y las pruebas con usuarios piloto se hacen en local.
- **Indicadores de grupo (SCRUM-39):** los necesita el dashboard del docente (SCRUM-40).
