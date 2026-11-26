# Registro de incidencias (#142)

**Fecha:** 10 de noviembre de 2026.
**Criterio de cierre:** sin defectos de prioridad Alta abiertos.

| ID | Descripción | Severidad | CA | Estado | Responsable | Evidencia / decisión |
|---|---|---|---|---|---|---|
| INC-01 | La opción «Roles y permisos» del menú del administrador abría una página «en construcción» aunque el cambio de rol ya existe en Usuarios | Baja | CA-13 | Corregida | Fernando / Nicolás | `RolesPage.jsx` ahora explica los roles y enlaza a Usuarios con el filtro correspondiente; `RolesPage.test.jsx` |
| INC-02 | No existe entorno de staging (#33), por lo que las verificaciones se hicieron en local | Bloqueante externo | CA-01 a CA-16 | Abierta | Jorel | Depende de #33; se repite el recorrido cuando exista la URL |
| INC-03 | No hay usuarios piloto reales disponibles para #143 | Bloqueante externo | RNF-01 | Abierta | Juan David | Se ejecutó simulación interna; falta reclutar 4–6 participantes |
| INC-04 | `/docente/estudiantes` y `/docente/reportes` siguen siendo páginas «en construcción»; el dashboard del docente ya cubre estudiantes y analítica | Baja | CA-10 / CA-11 | Aceptada como alcance conocido | Equipo | No bloquea los criterios; documentado en README del frontend |

## Balance

- Defectos **críticos:** 0.
- Defectos **altos:** 0.
- Defectos **medios:** 0.
- Defectos **bajos abiertos:** 1 (INC-04, aceptado como alcance conocido).
- Bloqueos externos: 2 (staging y pilotos reales).
