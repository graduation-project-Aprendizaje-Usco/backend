# Matriz de aceptación CA-01 a CA-16 (#141)

**Fecha de ejecución:** 10 de noviembre de 2026.
**Entorno:** local (Docker, PostgreSQL 17, backend Spring Boot y frontend Vite).
**Staging:** pendiente de #33.
**Resultado global:** **16 de 16 criterios cumplen** con evidencia automatizada y revisión de código. **0 no cumplen.**
**Validación visual con usuarios reales:** pendiente de #143; la simulación interna se registra en [`observaciones-piloto.md`](observaciones-piloto.md).

## Criterios

| CA | RF | Criterio | Evidencia automatizada | Evidencia manual / UI | Resultado | Incidencia |
|---|---|---|---|---|---|---|
| CA-01 | RF-01 | Autenticación y control de acceso | `AutenticacionTest`, `JwtTest`, `AuthControllerTest`, `AccesoPorRolTest`, `ProteccionDeEndpointsTest`, `LoginPage.test.jsx`, `RegistroPage.test.jsx`, `ProtectedRoute.test.jsx`, `AuthContext.test.jsx`, `FlujosCompletosE2ETest` | Login de los tres roles y redirección por rol cubiertos por pruebas de componente; revisión visual en #143 | Cumple | — |
| CA-02 | RF-02 | Aplicación de la prueba diagnóstica | `DiagnosticoControllerTest`, `FlujosCompletosE2ETest`, `DiagnosticoPage.test.jsx` | Flujo iniciar, retomar, responder y finalizar cubierto por componente; revisión visual en #143 | Cumple | — |
| CA-03 | RF-03 | Cálculo automático del puntaje | `CalculadoraPuntajeTest`, `DiagnosticoControllerTest` | Resultado contrastado con cálculo manual en las pruebas de servicio | Cumple | — |
| CA-04 | RF-04 | Asignación de categoría | `UmbralesDiagnosticoTest`, `EscalasDiagnosticoTest` | Bordes de umbral cubiertos; umbrales en `application.properties` | Cumple | — |
| CA-05 | RF-05 | Motor de selección adaptativa | `SeleccionEjercicioServiceTest`, `PracticaControllerTest`, `FlujosCompletosE2ETest` | Motor determinista probado; revisión visual de ejercicios en #143 | Cumple | — |
| CA-06 | RF-06 | Interfaz de ejercicios interactivos | `PracticaPage.test.jsx`, `practicaApi.test.js` | Opción múltiple, respuesta numérica, teclado y 404 cubiertos por componente | Cumple | — |
| CA-07 | RF-07 | Retroalimentación inmediata | `FeedbackServiceTest`, `PracticaControllerTest`, `PanelRetroalimentacion.test.jsx`, `PracticaPage.test.jsx` | Pista al primer fallo y explicación al segundo cubiertas por componente | Cumple | — |
| CA-08 | RF-08 | Registro de intentos con trazabilidad | `PracticaControllerTest`, `PracticaIdempotenciaConcurrenteTest`, `PracticaTransaccionTest`, `EsquemaPracticaTest` | Los cinco datos y la clave de idempotencia se verifican en las pruebas | Cumple | — |
| CA-09 | RF-09 | Indicadores de progreso del estudiante | `ProgresoServiceTest`, `ProgresoControllerTest`, `ProgresoPage.test.jsx`, `formatearProgreso.test.js`, `FlujosCompletosE2ETest` | Dashboard y estados vacíos cubiertos por componente | Cumple | — |
| CA-10 | RF-10 | Indicadores de grupo para el docente | `AnaliticaGrupoControllerTest`, `InicioDocentePage.test.jsx` | Grupos, filtros y detalle cubiertos por componente | Cumple | — |
| CA-11 | RF-11 | Indicadores derivados de analítica | `AnaliticaGrupoControllerTest`, `InicioDocentePage.test.jsx` | Dimensión más débil, top de errores y evolución cubiertos | Cumple | — |
| CA-12 | RF-12 | Gestión de actividades y contenidos | `ActividadesControllerTest`, `ActividadesPage.test.jsx`, `actividadesApi.test.js` | CRUD, validación, confirmación y permisos cubiertos por componente | Cumple | — |
| CA-13 | RF-13 | Administración de usuarios y roles | `AdminUsuarioControllerTest`, `UsuariosPage.test.jsx`, `AdminActividadesControllerTest`, `RolesPage.test.jsx`, `FlujosCompletosE2ETest` | Alta con contraseña temporal, edición, desactivación y autoprotección cubiertas; RolesPage corregida (INC-01) | Cumple | INC-01 corregida |
| CA-14 | RF-14 | Banco de ejercicios por competencia y nivel | `EsquemaPracticaTest` | 90 ejercicios, 45 por asignatura, 5 por combinación competencia × nivel y CSV coincidente | Cumple | — |
| CA-15 | RF-15 | Historial de resultados | `HistorialControllerTest`, `HistorialResultados.test.jsx`, `FlujosCompletosE2ETest` | Filtros, paginación y permisos cubiertos por componente | Cumple | — |
| CA-16 | RF-16 | Lógica de niveles de dificultad | `NivelServiceTest`, `NivelEstudianteServiceTest`, `PracticaControllerTest` | Subida, bajada, límites 1–3 y rachas cubiertos | Cumple | — |

## Matriz CP-01 a CP-08

| CP | CAs | Cobertura | Resultado |
|---|---|---|---|
| CP-01 | CA-01 | Autenticación, registro y control de acceso | Cumple |
| CP-02 | CA-02, CA-03, CA-04 | Diagnóstico, puntaje y categoría | Cumple |
| CP-03 | CA-05, CA-06, CA-07, CA-08, CA-16 | Práctica, retroalimentación, trazabilidad y niveles | Cumple |
| CP-04 | CA-09, CA-15 | Progreso e historial | Cumple |
| CP-05 | CA-10, CA-11 | Analítica del docente | Cumple |
| CP-06 | CA-12, CA-14 | Contenidos y banco de ejercicios | Cumple |
| CP-07 | CA-13 | Administración de usuarios y roles | Cumple |
| CP-08 | CA-01 a CA-16 | Integración de los tres flujos | Cumple en local; staging pendiente #33 |

## Comandos ejecutados

```powershell
# Backend
.\mvnw.cmd verify
# Resultado: 285 pruebas, 0 fallos.

# Frontend
npm run lint
npm test
npm run build
# Resultado: 122 pruebas en 22 archivos, lint y build correctos.
```

## Notas

- La evidencia automatizada se complementa con revisión de código y con las pruebas de componente del frontend.
- La revisión visual con usuarios y la validación en staging se registran aparte para no confundirlas con la ejecución local.
- Las incidencias y su tratamiento están en [`registro-incidencias.md`](registro-incidencias.md).
