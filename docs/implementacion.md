# Implementación del backend

Qué se ha construido, para qué sirve, qué decisiones se tomaron y cómo probarlo, **historia por historia**. Complementa al [README](README-backend.md) (cómo correr el proyecto y las convenciones) y a [tareas-y-responsables.md](tareas-y-responsables.md) (qué pide cada tarea). La parte visual está en `docs/implementacion.md` del frontend.

Se actualiza con cada historia que entra a `develop`: una sección nueva, o la existente si cambió.

| Historia | Jira | Responsable | Estado |
|---|---|---|---|
| [Inicio de sesión con JWT](#1-inicio-de-sesión-con-jwt-scrum-16) | SCRUM-16 | Nicolás Pérez | En `develop` |
| [Control de acceso por rol](#2-control-de-acceso-por-rol-scrum-12) | SCRUM-12 | Fernando Pérez (integración: Nicolás Pérez) | En `develop` |
| [Pruebas del diagnóstico](#3-pruebas-del-módulo-de-diagnóstico-scrum-27) | SCRUM-27 | Nicolás Pérez | En `develop` (falta la evidencia manual) |
| [Historial de resultados](#5-historial-de-resultados-scrum-41) | SCRUM-41 (168) | Nicolás Pérez | En `develop` |
| [Indicadores de grupo para el docente](#6-indicadores-de-grupo-para-el-docente-scrum-39) | SCRUM-39 (123) | Jorel Vargas (implementación: Juan David Polanco) | En `develop` · ver README §16 |
| [Indicadores derivados de analítica](#7-indicadores-derivados-de-analítica-scrum-42) | SCRUM-42 (131) | Juan David Polanco | En `develop` · ver README §16 |
| [Gestión de contenidos y ejercicios](#8-gestión-de-contenidos-y-ejercicios-scrum-43) | SCRUM-43 (132) | Juan David Polanco | En `develop` · ver README §17 |
| [Rendimiento de servicios críticos](#9-rendimiento-de-servicios-críticos-scrum-44) | SCRUM-44 (135) | Jorel Vargas (implementación: Juan David Polanco) | Ejecutado; RNF-02 cumple |
| Aplicación de la prueba diagnóstica, puntaje y categoría | SCRUM-22, 23, 24 | Jorel Vargas | En `develop` · ver README §12 |
| Motor de práctica adaptativa | SCRUM-28 a 31 | Juan David Polanco | En `develop` · ver README §13 |
| [Retroalimentación inmediata y registro de intentos](#4-retroalimentación-inmediata-y-registro-de-intentos-110-114) | 110, 114 | Juan David Polanco | En `develop` · ver README §13 |
| Grupos docente–estudiante | SCRUM-159 | Fernando Pérez | En `develop` |
| [Administración de usuarios](#10-administración-de-usuarios-scrum-45) | SCRUM-45 (108) | Jorel Vargas (implementación: Nicolás Pérez) | En `develop` |
| [Panel del administrador](#11-panel-del-administrador-scrum-46) | SCRUM-46 | Fernando Pérez (implementación: Nicolás Pérez) | En `develop` · verificado |
| [Vistas de administración de grupos y actividades](#12-vistas-de-administración-de-grupos-y-actividades-scrum-46) | SCRUM-46 | Fernando Pérez (implementación: Nicolás Pérez) | En `develop` |
| [Integración end-to-end de los módulos](#13-integración-end-to-end-de-los-módulos-140) | 140 | Juan David Polanco | Verificado en local; staging pendiente |

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

## 6. Indicadores de grupo para el docente (SCRUM-39)

**Para qué.** Que el docente consulte el desempeño agregado de sus grupos sin mezclar estudiantes de otros docentes ni periodos.

**Cómo funciona.**

1. El servicio valida que el grupo pertenezca al docente autenticado (`GrupoService.obtenerGrupoDelDocente`).
2. Obtiene los estudiantes inscritos en `grupo_estudiante`.
3. Ejecuta consultas batch para las métricas de práctica por estudiante/dimensión y el último diagnóstico de cada estudiante.
4. Aplica los filtros opcionales de competencia y rango de fechas (`desde`/`hasta`) en hora de Colombia.
5. Calcula el resumen, la distribución de categorías y los estudiantes en riesgo.
6. Para el detalle, valida además que el estudiante pertenezca al grupo y devuelve sus dimensiones y su último diagnóstico.

| Archivo | Qué hace |
|---|---|
| `modulos/analitica/controller/AnaliticaGrupoController.java` | Endpoints `GET /api/analitica/grupos/{id}` y `.../estudiantes/{estudianteId}` |
| `modulos/analitica/service/IndicadoresGrupoService.java` | Agregaciones, permisos, filtros y detalle |
| `modulos/analitica/repository/AnaliticaGrupoRepository.java` | Consultas SQL batch de práctica y diagnósticos |
| `modulos/analitica/dto/*` | Resumen, dimensiones, distribución, estudiantes y detalle |
| `modulos/usuario/service/GrupoService.java` | `esDocenteDe` y `obtenerGrupoDelDocente` reutilizables |

**Decisiones.**

- **Sin migración:** el periodo se implementa como rango de fechas; no se agregó `grupo.periodo`.
- **Riesgo:** último diagnóstico `ALTO_RIESGO` o promedio de práctica menor al 50 %. La ausencia de actividad se informa aparte.
- **Promedio por dimensión:** promedio simple de los porcentajes individuales; no pondera por cantidad de intentos.
- **Consultas batch:** se evita una consulta por estudiante.
- **El detalle reutiliza el historial:** la vista del docente usa `<HistorialResultados estudianteId={id} />`.

**Pruebas.** `AnaliticaGrupoControllerTest`: 10 pruebas con Testcontainers (agregaciones, derivados, filtros, rango de fechas, permisos, grupo inexistente, grupo vacío, detalle y validaciones). `.\mvnw.cmd verify` → **262 pruebas, 0 fallos**.

**Cómo probarlo a mano.** Crear un grupo con `POST /api/grupos`, inscribir estudiantes, presentar diagnóstico y práctica, y consultar `GET /api/analitica/grupos/{id}` con el token del docente. Probar otro docente (403), un rango de fechas y una competencia.

**Detalle técnico completo:** [`docs/code/indicadores-grupo-docente.md`](code/indicadores-grupo-docente.md).

---

## 7. Indicadores derivados de analítica (SCRUM-42)

**Para qué.** Completar el dashboard del docente con la dimensión más débil, los ejercicios con más errores y la evolución semanal del grupo.

**Cómo funciona.**

- El resumen existente ahora incluye `dimensionMasDebil`, `topEjerciciosErrores` y `evolucionGeneral`.
- `AnaliticaGrupoRepository` agrega dos consultas batch:
  - top 5 de ejercicios por errores, agrupando por ejercicio y competencia;
  - evolución semanal con `DATE_TRUNC('week', creado_en)`.
- La dimensión más débil se calcula en el servicio a partir de las dimensiones con actividad.
- Los filtros de competencia y fechas se aplican también a estos indicadores.

**Pruebas.** `AnaliticaGrupoControllerTest.calculaDimensionMasDebilTopDeErroresYEvolucion` verifica orden, dimensión y evolución.

**Detalle técnico completo:** [`docs/code/indicadores-grupo-docente.md`](code/indicadores-grupo-docente.md).

---

## 8. Gestión de contenidos y ejercicios (SCRUM-43)

**Para qué.** Que el docente cree, edite, active, desactive y elimine sus propios contenidos y ejercicios, con confirmación antes de borrar.

**Cómo funciona.**

- `ContenidoController` y `EjercicioController` exponen CRUD completo bajo `/api/contenidos` y `/api/ejercicios`.
- Todos exigen rol `DOCENTE` y validan que `creado_por` sea el docente autenticado.
- Los datos semilla son de solo lectura.
- Se validan opciones A–D, una única correcta, respuestas numéricas y tolerancias.
- Un ejercicio con intentos no se elimina: responde 409 y la UI ofrece desactivarlo. Si tiene intentos, solo se editan campos que no afectan la calificación histórica.
- Un contenido con ejercicios tampoco se elimina: responde 409 y se desactiva.
- El motor ya filtra por `ejercicio.activo` y `contenido.activo`.

**Pruebas.** `ActividadesControllerTest`: 12 pruebas (contenidos, edición de contenido, ejercicio múltiple y numérico, validaciones, código duplicado, permisos, eliminación, desactivación, edición con intentos y uso por el motor). Ejecutar `.\mvnw.cmd verify`.

**Detalle técnico completo:** [`docs/code/gestion-actividades-contenidos.md`](code/gestion-actividades-contenidos.md).

---

## 9. Rendimiento de servicios críticos (SCRUM-44)

**Para qué.** Medir `GET /api/practica/siguiente` y la finalización del diagnóstico con 30 usuarios concurrentes.

Script: `pruebas/rendimiento/k6-practica-diagnostico.js`, ejecutado con k6 en Docker. Umbrales: p95 < 1 s y error rate < 1 % por endpoint.

**Resultado:** ejecutado con 30 VUs durante 1 min. p95 de `siguiente` 20,20 ms y p95 de finalización 20,97 ms; 0 % de errores. RNF-02 se cumple. El detalle y el hallazgo de pool de conexiones corregido están en
[`docs/code/rendimiento-servicios-criticos.md`](code/rendimiento-servicios-criticos.md).

## 10. Administración de usuarios (SCRUM-45)

**Para qué.** Que el administrador cree, edite, cambie el rol y desactive cuentas (RF-13).

**Endpoints** (`/api/admin/usuarios`, solo ADMIN):

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/admin/usuarios?buscar&rol&estado` | Lista con búsqueda en nombres, apellidos y correo, y filtros por rol y estado (todos opcionales) | 200 |
| `POST /api/admin/usuarios` | Crea el usuario y devuelve `{ usuario, passwordTemporal }` | 201 · 400 campos · 409 correo repetido · 422 estudiante sin correo de estudiante o sin programa |
| `PUT /api/admin/usuarios/{id}` | Cambia nombres, apellidos, rol y programa (el correo no se cambia) | 200 · 404 · 422 si el administrador se quita su propio rol |
| `PATCH /api/admin/usuarios/{id}/estado` | `{ "estado": "ACTIVO" \| "INACTIVO" }` | 200 · 404 · 422 si el administrador se desactiva a sí mismo |

Sin token responde 401 y con otro rol, 403.

**Archivos:** `modulos/usuario/controller/AdminUsuarioController.java`, `service/AdminUsuarioService.java`, `repository/UsuarioRepository.buscarParaAdmin`, `dto/UsuarioAdminRequest`, `ActualizarUsuarioRequest`, `EstadoUsuarioRequest`, `UsuarioAdminResponse` y `UsuarioCreadoResponse`.

**Decisiones.**

- **Contraseña temporal:** 12 caracteres al azar (`SecureRandom`), sin letras ni números que se confundan (0/O, 1/l/I). Cumple la regla del registro (letra y número). Solo viaja en la respuesta de creación; en la base queda el hash BCrypt.
- **Mismas reglas que el registro para estudiantes:** correo `u` + 11 dígitos + `@usco.edu.co` y programa de la lista. Docentes y administradores no llevan programa.
- **El administrador no puede desactivarse ni quitarse el rol ADMIN:** se quedaría sin acceso a esta pantalla.
- **No se borran usuarios:** se desactivan, y así se conservan sus intentos y resultados.
- **Un usuario desactivado no inicia sesión** (ya lo garantizaba `UsuarioDetailsService`). Un token emitido antes de desactivarlo sigue valiendo hasta que vence (README §11).

**Pruebas.** `AdminUsuarioControllerTest`: 7 pruebas.
- Permisos: 401 sin token y 403 para docente y estudiante en los 4 endpoints.
- Búsqueda y filtros.
- Creación con contraseña temporal que sirve para iniciar sesión.
- Correo repetido (409) y estudiante inválido (422).
- Edición del rol.
- Usuario desactivado que no puede iniciar sesión y vuelve a poder al reactivarlo.
- El administrador no puede desactivarse ni quitarse el rol.

**Cómo probarlo a mano.** Swagger → login con `admin@aprendizaje.test` / `Admin2026*` → **Authorize** → `POST /api/admin/usuarios` con un docente nuevo; luego iniciar sesión con la contraseña temporal que devuelve.

## 11. Panel del administrador (SCRUM-46)

**Para qué.** Que el administrador vea de un vistazo cuántos usuarios hay por rol, cuántos grupos y cuántas actividades activas.

**Endpoint:** `GET /api/admin/resumen` (solo ADMIN) → `{ usuariosPorRol: [{ rol, total, activos }], grupos, contenidosActivos, ejerciciosActivos }`.

**Decisiones.**

- **Grupos:** se cuentan todos, porque la tabla `grupo` no tiene estado activo o inactivo.
- **Ejercicios activos:** los que el motor puede asignar, es decir, el ejercicio activo y su contenido activo (el mismo criterio que `findActivosPorAsignatura`).
- **Consultas derivadas de Spring Data** (`countByRol_NombreAndEstado`, `countByActivoTrue`…): con tres roles son seis conteos pequeños, y no hace falta una consulta a mano.
- En el backlog figura como tarea solo de frontend, pero el administrador no tenía forma de consultar contenidos ni ejercicios (esos endpoints son del docente). Por eso hizo falta este endpoint.
- **Enlaces a las pantallas de gestión:** el panel enlaza a Usuarios, Grupos y Actividades; las dos últimas son vistas de solo lectura (sección 12).

**Pruebas.** En `AdminUsuarioControllerTest`: 403 para otro rol; un estudiante inactivo nuevo suma al total pero no a los activos; un grupo nuevo suma 1; los conteos de actividades coinciden con una consulta SQL directa.

## 12. Vistas de administración de grupos y actividades (SCRUM-46)

**Para qué.** Que el administrador no solo vea los conteos del panel, sino que pueda consultar los grupos de los docentes y el catálogo de contenidos y ejercicios.

**Endpoints.**

| Método y ruta | Qué hace | Respuestas |
|---|---|---|
| `GET /api/grupos` | Lista todos los grupos cuando el token es ADMIN (para el docente, solo los suyos) | 200 · 401 · 403 |
| `GET /api/grupos/{id}/estudiantes` | Estudiantes inscritos en un grupo; ADMIN puede consultar cualquiera | 200 · 401 · 403 · 404 |
| `GET /api/admin/actividades` | Catálogo de contenidos y ejercicios de todos los docentes, sin claves ni explicaciones | 200 · 401 · 403 |

**Decisiones.**

- **Grupos sin endpoint nuevo:** el controlador de grupos ya permitía ADMIN; se reutiliza para la vista de solo lectura. El administrador no crea ni elimina grupos.
- **Actividades sin respuestas correctas:** el DTO `AdminActividadesResponse` omite opciones, respuesta numérica, pista y explicación. El administrador consulta la gestión, pero no recibe las claves.
- **Datos semilla:** los contenidos y ejercicios con `creado_por = null` se muestran como “Semilla”.

**Archivos.** `modulos/contenido/controller/AdminActividadesController.java`, `service/AdminActividadesService.java`, `dto/AdminActividadesResponse.java` y las consultas `findAllParaAdmin` de los repositorios de contenidos y ejercicios.

**Pruebas.** `AdminActividadesControllerTest`: 401 sin token, 403 para DOCENTE y ESTUDIANTE, catálogo completo y ausencia de claves. `GrupoControllerTest` ya cubre el acceso ADMIN a los grupos.

## 13. Integración end-to-end de los módulos (140)

**Para qué.** Comprobar que los módulos comparten usuarios, roles y datos, y que los tres flujos completos funcionan sin intervención manual en la base.

**Prueba automatizada.** `FlujosCompletosE2ETest` usa Testcontainers, MockMvc y login real:
- Administrador: crea docente y estudiante con contraseña temporal, los desactiva y reactiva, y consulta panel y catálogo.
- Docente: crea un grupo, inscribe al estudiante y consulta su analítica e historial.
- Estudiante: presenta los 24 ítems del diagnóstico, resuelve tres ejercicios de práctica y consulta progreso e historial.

**Validación manual.** `docs/pruebas/e2e-sprint-4.md` deja el guion y el resultado local. **Staging queda pendiente de #33**: el criterio de aceptación de #140 se cierra cuando exista el entorno y se repita el mismo recorrido.

---

## Pendientes conocidos

- **Staging (SCRUM-13):** sin él, la verificación manual y las pruebas con usuarios piloto se hacen en local. La integración end-to-end (#140) quedó verificada en local y pendiente de repetirse en staging.
