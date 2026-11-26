# Rendimiento de servicios críticos (RNF-02)

**Proyecto:** Aprendizaje USCO<br>
**Tarea:** 135 — Ejecutar pruebas de rendimiento de servicios críticos<br>
**Responsable:** Jorel Vargas (implementación: Juan David Polanco)<br>
**Rama:** `feature/SCRUM-39-indicadores-grupo-docente`<br>
**Fecha:** noviembre de 2026<br>
**Estado:** ejecutado el 9 de noviembre de 2026; RNF-02 cumple

---

## 1. Objetivo

Medir bajo carga el tiempo de respuesta de:

1. `GET /api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL`
2. `POST /api/diagnosticos/intentos/{id}/finalizar`

El criterio es **p95 < 1 s con 30 usuarios concurrentes** (ajustable después de la
primera medición), y dejar el resultado registrado para el capítulo 7.

---

## 2. Script

`pruebas/rendimiento/k6-practica-diagnostico.js`

El `setup()`:

1. Crea 30 estudiantes de carga con correos `u` + 11 dígitos + `@usco.edu.co`.
2. Inicia sesión y obtiene el token de cada uno.
3. Crea (o recupera) un intento diagnóstico por estudiante.

El escenario mantiene 30 VUs durante un minuto. Cada iteración mide los dos
endpoints y los etiqueta como `practica_siguiente` y `diagnostico_finalizar`.
Los umbrales se evalúan por etiqueta para no contaminar la medición con el setup.

---

## 3. Cómo ejecutarlo

Con el backend usando la configuración local de `.env` y Docker Desktop abierto:

```powershell
docker run --rm -i `
  -e BASE_URL=http://host.docker.internal:8080 `
  -v "${PWD}:/scripts" `
  grafana/k6 run --summary-export=/scripts/pruebas/rendimiento/resultado.json `
  /scripts/pruebas/rendimiento/k6-practica-diagnostico.js
```

El detalle de requisitos y variables está en
[`pruebas/rendimiento/README.md`](../../pruebas/rendimiento/README.md).

---

## 4. Registro de la ejecución

| Fecha | Responsable | Versión | Duración | VUs | Entorno |
|---|---|---|---|---|---|
| 2026-11-09 | Juan David Polanco | rama `feature/SCRUM-39-indicadores-grupo-docente` | 1 min | 30 | Backend en Docker Compose + PostgreSQL 17 local |

| Endpoint | p50 | p95 | RPS aprox. | Errores | Umbral | Resultado |
|---|---:|---:|---:|---:|---|---|
| `GET /api/practica/siguiente` | 10.07 ms | **20.20 ms** | 57 | 0.00 % | p95 < 1 s | Cumple |
| `POST /api/diagnosticos/intentos/{id}/finalizar` | 7.85 ms | **20.97 ms** | 57 | 0.00 % | p95 < 1 s | Cumple |

**Conclusión:** RNF-02 se cumple con 30 VUs: p95 de ambos endpoints por debajo de 1 s
y sin errores HTTP. El resumen exportado está en
`pruebas/rendimiento/resultado.json` (el archivo se genera al ejecutar el script).

**Hallazgo corregido.** En la primera ejecución, la finalización del diagnóstico
agotó el pool de conexiones porque el listener de inicialización de niveles usaba
`REQUIRES_NEW` mientras la transacción de finalización todavía retenía su
conexión. Se cambió `NivelEstudianteService.inicializarDesdeResultados` a la
propagación por defecto; la segunda ejecución terminó con 0 errores.

---

## 5. Interpretación

- Si el p95 supera 1 s, revisar primero el pool de conexiones, los índices de
  `intento_practica`/`intento_diagnostico` y las consultas del motor de selección.
- Si el error rate supera 1 %, revisar límites de conexiones y tiempos de espera.
- Cualquier ajuste de umbral debe quedar justificado en este documento y en el
  capítulo 7 del informe.
