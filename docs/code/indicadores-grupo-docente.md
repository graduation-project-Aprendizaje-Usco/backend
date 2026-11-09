# Indicadores de grupo para el docente

**Proyecto:** Aprendizaje USCO<br>
**Épica:** EPIC-4 — Seguimiento del progreso y analítica de aprendizaje<br>
**Tareas:** 123 (servicio) y 124 (dashboard)<br>
**Responsable de la tarea:** Jorel Vargas (implementación backend: Juan David Polanco)<br>
**Ramas:** `feature/SCRUM-39-indicadores-grupo-docente` (backend) y `feature/SCRUM-40-dashboard-docente` (frontend)<br>
**Fecha:** noviembre de 2026<br>
**Estado:** implementado y verificado en la rama; pendiente de PR y merge a `develop`

Este documento describe las reglas, el contrato y las decisiones de los indicadores que consume el dashboard del docente. El dashboard reutiliza el historial de resultados de `#128`.

---

## 1. Alcance

- El docente consulta **solo sus grupos**; un docente ajeno recibe 403.
- El resumen incluye:
  - total de estudiantes y estudiantes con actividad;
  - promedio general y promedio por dimensión;
  - distribución de la última categoría de diagnóstico;
  - estudiantes en riesgo;
  - tabla de estudiantes ordenable y buscable en el cliente.
- El detalle de un estudiante incluye sus dimensiones, su último diagnóstico y el historial de resultados.
- El frontend permite crear grupos e inscribir estudiantes usando los endpoints existentes de `#99/#101`.

---

## 2. Contrato

### 2.1 Resumen

```http
GET /api/analitica/grupos/{grupoId}?competencia=ALGEBRAICA&desde=2026-08-01&hasta=2026-08-31
Authorization: Bearer <token DOCENTE>
```

Respuesta 200 (resumida):

```json
{
  "grupo": { "id": 1, "codigo": "CALCULO-2026-2-A", "nombre": "Cálculo Diferencial" },
  "totalEstudiantes": 24,
  "estudiantesConActividad": 20,
  "promedioGeneral": 68.5,
  "dimensiones": [
    {
      "codigo": "ALGEBRAICA",
      "nombre": "Competencia algebraica",
      "promedio": 72.1,
      "estudiantesEvaluados": 19,
      "intentosEvaluados": 80,
      "intentosCorrectos": 58
    }
  ],
  "distribucionCategorias": [
    { "categoria": "ALTO_RIESGO", "cantidad": 4, "porcentaje": 16.67 },
    { "categoria": "RIESGO_MODERADO", "cantidad": 8, "porcentaje": 33.33 },
    { "categoria": "NIVEL_ADECUADO", "cantidad": 12, "porcentaje": 50.00 },
    { "categoria": "SIN_DIAGNOSTICO", "cantidad": 0, "porcentaje": 0.00 }
  ],
  "estudiantesEnRiesgo": 4,
  "estudiantes": [
    {
      "id": 12,
      "nombres": "Ana",
      "apellidos": "Pérez",
      "correo": "u20201234567@usco.edu.co",
      "intentos": 8,
      "aciertos": 3,
      "promedio": 37.5,
      "nivel": "INICIAL",
      "categoriaDiagnostico": "ALTO_RIESGO",
      "sinActividad": false,
      "enRiesgo": true,
      "motivosRiesgo": ["Diagnóstico en alto riesgo", "Promedio de práctica menor al 50 %"]
    }
  ]
}
```

### 2.2 Detalle

```http
GET /api/analitica/grupos/{grupoId}/estudiantes/{estudianteId}?desde=2026-08-01&hasta=2026-08-31
```

Devuelve `estudiante`, `dimensiones` y `ultimoDiagnostico`. Si el estudiante no pertenece al grupo, responde 404.

### 2.3 Errores

| Código | Caso |
|---|---|
| 400 | Competencia distinta de `ALGEBRAICA`, `LOGICA` o `FUNCIONAL` |
| 401 | Sin token o token inválido |
| 403 | Rol distinto de `DOCENTE`, o grupo de otro docente |
| 404 | Grupo inexistente, o estudiante fuera del grupo en el detalle |
| 422 | `desde` posterior a `hasta` |

---

## 3. Reglas de cálculo

- **Periodo:** `desde` y `hasta` son fechas inclusivas en `America/Bogota`. Se aplican a `intento_practica.creado_en` y a `intento_diagnostico.finalizado_en`. Sin fechas se usa todo el historial.
- **Promedio por dimensión:** promedio simple de los porcentajes individuales de los estudiantes con intentos de práctica en esa dimensión. No se pondera por cantidad de intentos.
- **Promedio general:** promedio simple de los promedios de los estudiantes con actividad.
- **Categoría:** última categoría de diagnóstico finalizado de cada estudiante; `SIN_DIAGNOSTICO` si no tiene.
- **Riesgo:** último diagnóstico `ALTO_RIESGO` o promedio de práctica menor al 50 %. La falta de actividad se informa con `sinActividad` y no se marca como riesgo por sí sola.
- **Nivel de práctica:** se usa `NivelDominio` del módulo de progreso (`INICIAL`, `BASICO`, `INTERMEDIO`, `AVANZADO`; `SIN_EVALUAR` sin actividad).

---

## 4. Permisos

- `GrupoService.obtenerGrupoDelDocente(grupoId, docenteId)` devuelve el grupo o lanza 404/403.
- `GrupoService.esDocenteDe(docenteId, estudianteId)` reutiliza la regla de `#102` y se usa en el detalle.
- El controlador exige `@PreAuthorize("hasRole('DOCENTE')")`; `ADMIN` y `ESTUDIANTE` reciben 403.

---

## 5. Archivos

### Backend

| Archivo | Cambio |
|---|---|
| `modulos/analitica/controller/AnaliticaGrupoController.java` | Endpoints del resumen y del detalle |
| `modulos/analitica/service/IndicadoresGrupoService.java` | Agregaciones, filtros, riesgo y detalle |
| `modulos/analitica/repository/AnaliticaGrupoRepository.java` | Consultas SQL batch |
| `modulos/analitica/dto/*` | Contratos de respuesta |
| `modulos/usuario/service/GrupoService.java` | `esDocenteDe` y `obtenerGrupoDelDocente` |
| `modulos/usuario/repository/GrupoEstudianteRepository.java` | Consulta de pertenencia docente–estudiante |
| `src/test/java/.../analitica/controller/AnaliticaGrupoControllerTest.java` | 9 pruebas de integración |

### Frontend

| Archivo | Cambio |
|---|---|
| `api/gruposApi.js` | Listar, crear e inscribir |
| `api/analiticaApi.js` | Resumen y detalle |
| `features/docente/InicioDocentePage.jsx` | Dashboard, filtros y URL |
| `features/docente/components/*` | Selector, filtros, KPIs, gráficas, tabla, detalle y diálogos |
| `features/docente/InicioDocentePage.test.jsx` | 6 pruebas del flujo |
| `api/gruposApi.test.js` / `api/analiticaApi.test.js` | Contratos de los clientes |

---

## 6. Pruebas

```bash
# Backend (Docker Desktop)
.\mvnw.cmd verify

# Frontend
npm test
npm run lint
npm run build
```

Resultados: backend **249 pruebas** y frontend **98 pruebas** (al momento de esta implementación).

---

## 7. Cómo probarlo a mano

1. Entrar como `docente@aprendizaje.test` / `Docente2026*`.
2. En el dashboard, crear un grupo si no existe.
3. Inscribir estudiantes por correo.
4. Pedir a los estudiantes que presenten el diagnóstico y resuelvan ejercicios.
5. Abrir `/docente`, cambiar grupo, competencia y rango de fechas, ordenar la tabla y abrir “Ver detalle”.
6. Comprobar que el detalle muestra dimensiones, último diagnóstico e historial.

---

## 8. Decisiones y coordinación

- **Periodo como rango de fechas:** evita una migración y permite acotar los intentos a un semestre o corte. El modelo `grupo` no tiene `periodo`.
- **Riesgo:** regla acordada para el MVP; si el equipo cambia el umbral, se ajusta en el servicio y se documenta aquí.
- **Detalle en Drawer:** evita duplicar una tabla de estudiantes en otra ruta y reutiliza `HistorialResultados`.
- **Creación e inscripción:** el backend ya existía; se agregó la UI mínima para que el docente pueda dejar el grupo operativo sin salir del dashboard.
- **Historial:** el componente de `#128` se reutiliza con `estudianteId`; los permisos de grupo los valida el backend.
