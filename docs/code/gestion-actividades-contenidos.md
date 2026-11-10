# Gestión de contenidos y ejercicios para el docente

**Proyecto:** Aprendizaje USCO<br>
**Épica:** EPIC-5 — Gestión de contenidos y administración<br>
**Tarea:** 132 — Implementar gestión de actividades y contenidos por el docente<br>
**Responsable:** Juan David Polanco<br>
**Rama:** `feature/SCRUM-39-indicadores-grupo-docente`<br>
**Fecha:** noviembre de 2026<br>
**Estado:** implementado y verificado en la rama; pendiente de PR y merge a `develop`

---

## 1. Alcance

- El docente administra **solo sus contenidos y ejercicios** (`creado_por`).
- Los datos semilla (`creado_por = NULL`) son de solo lectura.
- CRUD con validación, activación/desactivación y confirmación de borrado.
- Un ejercicio con intentos no se elimina: se desactiva.
- Un contenido con ejercicios no se elimina: se desactiva.
- El motor de práctica solo usa ejercicios y contenidos activos.

---

## 2. Endpoints

### Contenidos

| Método y ruta | Uso |
|---|---|
| `GET /api/contenidos` | Lista los contenidos propios; filtros `asignatura`, `competencia`, `activo`, `q` |
| `POST /api/contenidos` | Crea un contenido |
| `PUT /api/contenidos/{id}` | Edita tema, título y descripción |
| `PATCH /api/contenidos/{id}/estado` | Activa o desactiva |
| `DELETE /api/contenidos/{id}` | Elimina si no tiene ejercicios |

### Ejercicios

| Método y ruta | Uso |
|---|---|
| `GET /api/ejercicios` | Lista los ejercicios propios; filtros `contenidoId`, `competencia`, `nivel`, `tipo`, `activo`, `q` |
| `POST /api/ejercicios` | Crea un ejercicio en un contenido propio |
| `PUT /api/ejercicios/{id}` | Edita un ejercicio propio |
| `PATCH /api/ejercicios/{id}/estado` | Activa o desactiva |
| `DELETE /api/ejercicios/{id}` | Elimina si no tiene intentos |

Todos requieren `Authorization: Bearer <token>` con rol `DOCENTE`. Un contenido o ejercicio ajeno responde 403; uno inexistente, 404.

---

## 3. Validaciones

- Opción múltiple: exactamente 4 opciones A–D y exactamente una correcta.
- Numérica: respuesta y tolerancia obligatorias, tolerancia > 0; sin opciones.
- El contenido debe pertenecer al docente y estar activo para crear ejercicios.
- La competencia y la asignatura del contenido no se cambian después de crearlo.
- El código del ejercicio es único; si se omite, el backend genera `DOC-XXXXXXXX`.
- Si un ejercicio tiene intentos:
  - no se elimina;
  - se puede desactivar;
  - solo se editan enunciado, nivel, pista, explicación y error común;
  - se rechazan cambios de tipo, opciones, respuesta o contenido.
- Si un contenido tiene ejercicios:
  - no se elimina;
  - se puede desactivar;
  - al desactivarlo, el motor deja de usar sus ejercicios.

---

## 4. Archivos

### Backend

| Archivo | Qué hace |
|---|---|
| `modulos/contenido/controller/ContenidoController.java` | APIs de contenidos |
| `modulos/contenido/controller/EjercicioController.java` | APIs de ejercicios |
| `modulos/contenido/service/ContenidoService.java` | Reglas y propiedad de contenidos |
| `modulos/contenido/service/EjercicioService.java` | Validación, edición y borrado de ejercicios |
| `modulos/contenido/repository/*` | Consultas de contenidos, ejercicios y pertenencia |
| `modulos/contenido/dto/*` | Contratos de entrada y salida |
| `modulos/practica/repository/IntentoPracticaRepository.java` | `existsByEjercicioId` para bloquear el borrado |

### Frontend

| Archivo | Qué hace |
|---|---|
| `api/contenidosApi.js` | Cliente CRUD de contenidos |
| `api/ejerciciosApi.js` | Cliente CRUD de ejercicios |
| `features/docente/ActividadesPage.jsx` | Pantalla con pestañas y acciones |
| `features/docente/actividades/*` | Tablas, filtros y formularios |
| `features/docente/ActividadesPage.test.jsx` | Flujo de la pantalla |

---

## 5. Pruebas

Backend:

```bash
.\mvnw.cmd verify
```

`ActividadesControllerTest` (12 pruebas) cubre: crear/listar/editar contenidos, ejercicio múltiple y numérico, validaciones, código duplicado, permisos, borrado sin intentos, borrado bloqueado con intentos, desactivación, edición restringida y uso por el motor.

Frontend:

```bash
npm test
npm run lint
npm run build
```

`ActividadesPage.test.jsx` cubre pestañas, creación de contenido, creación y edición de ejercicio, activación/desactivación, eliminación y oferta de desactivar cuando hay intentos.

---

## 6. Cómo probarlo a mano

1. Entrar como docente.
2. Ir a **Actividades** → pestaña **Contenidos** → crear un contenido.
3. Pestaña **Ejercicios** → crear un ejercicio de opción múltiple o numérico.
4. Verificar la vista previa con `MathText`.
5. Iniciar sesión como estudiante, abrir **Práctica** y comprobar que aparece un ejercicio activo.
6. Desactivar el ejercicio y comprobar que el motor deja de ofrecerlo.
