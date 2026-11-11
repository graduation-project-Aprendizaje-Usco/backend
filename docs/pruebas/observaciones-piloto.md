# Observaciones de la validación de usabilidad (#143)

**Estado:** simulación técnica interna completada el 10 de noviembre de 2026. **Sesiones con usuarios reales pendientes.**
**Entorno:** local; staging pendiente de #33.
**Participantes reales:** ninguno todavía. No se han recogido datos personales.

## 1. Simulación técnica interna

Se recorrieron los flujos con la prueba automatizada `FlujosCompletosE2ETest`, las 274 pruebas de backend, las 122 pruebas de frontend y revisión de las pantallas implementadas. Esta simulación sirve para comprobar que el protocolo se puede ejecutar, pero **no reemplaza la observación de usuarios reales**.

| Tarea | Evidencia técnica | Observación | Estado |
|---|---|---|---|
| T1. Iniciar sesión | `LoginPage.test.jsx`, `AuthContext.test.jsx`, `AuthControllerTest` | Los tres roles entran y son redirigidos a su inicio | Cubierta |
| T2. Presentar diagnóstico | `DiagnosticoPage.test.jsx`, `DiagnosticoControllerTest`, `FlujosCompletosE2ETest` | Flujo de 24 ítems, reanudación y finalización sin bloqueos | Cubierta |
| T3. Resolver práctica | `PracticaPage.test.jsx`, `PanelRetroalimentacion.test.jsx`, `FlujosCompletosE2ETest` | Opción múltiple, respuesta numérica, pista y explicación | Cubierta |
| T4. Consultar progreso e historial | `ProgresoPage.test.jsx`, `HistorialResultados.test.jsx` | Indicadores y estados vacíos presentes | Cubierta |
| T5. Consultar grupo docente | `InicioDocentePage.test.jsx`, `AnaliticaGrupoControllerTest` | Selector, filtros, tabla y detalle disponible | Cubierta |
| T6. Gestionar usuario administrador | `UsuariosPage.test.jsx`, `RolesPage.test.jsx`, `AdminUsuarioControllerTest` | Alta, edición, desactivación y pantalla de roles | Cubierta |

## 2. Hallazgos

| ID | Hallazgo | Severidad | Tratamiento |
|---|---|---|---|
| H-01 | «Roles y permisos» era una página «en construcción» aunque el cambio de rol ya existía en Usuarios | Baja | Corregido: `RolesPage` explica los roles y enlaza a Usuarios filtrados |
| H-02 | `/docente/estudiantes` y `/docente/reportes` siguen en construcción; el dashboard ya cubre estudiantes y analítica | Baja | Aceptado como alcance conocido; no bloquea los criterios |
| H-03 | No hay staging | Bloqueante externo | Pendiente de #33 |
| H-04 | No hay participantes reales | Bloqueante externo | Pendiente de reclutamiento |

## 3. Conclusión

- El protocolo es ejecutable y las tareas están cubiertas por la implementación actual.
- No se detectaron defectos de prioridad Alta.
- La simulación técnica no produce métricas de usabilidad ni SUS: esas métricas requieren personas.
- **#143 no puede cerrarse como validación con usuarios hasta ejecutar las sesiones reales.**

## 4. Próximos pasos manuales

1. Reclutar 4–6 participantes.
2. Agendar las sesiones.
3. Firmar los consentimientos.
4. Ejecutar las tareas con moderador y observador.
5. Aplicar el SUS y entregar los datos a #144.
6. Repetir en staging cuando exista #33.
