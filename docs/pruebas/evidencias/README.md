# Evidencias de la ejecución funcional (#141)

**Fecha:** 10 de noviembre de 2026.
**Entorno:** local (Docker Desktop, PostgreSQL 17 vía Testcontainers, Java 21, Node 24).

## Comandos y resultados

| Comando | Resultado |
|---|---|
| `.\mvnw.cmd verify` (backend) | BUILD SUCCESS; **274 pruebas, 0 fallos** |
| `FlujosCompletosE2ETest` | 2 pruebas E2E, 0 fallos |
| `npm run lint` (frontend) | Sin errores |
| `npm test` (frontend) | **122 pruebas en 22 archivos**, 0 fallos |
| `npm run build` (frontend) | Build de producción correcto |

Los reportes generados no se versionan:

- Backend: `target/surefire-reports/`.
- Frontend: salida de Vitest en consola.
- Cobertura del diagnóstico: `target/site/jacoco/index.html` cuando se ejecuta el comando de cobertura.

## Evidencia documental

- Matriz de los 16 CA: [`../matriz-aceptacion-ca.md`](../matriz-aceptacion-ca.md).
- Incidencias y correcciones: [`../registro-incidencias.md`](../registro-incidencias.md).
- Flujos E2E: [`../e2e-sprint-4.md`](../e2e-sprint-4.md).
- Protocolo, consentimiento y observaciones: [`../protocolo-usabilidad.md`](../protocolo-usabilidad.md), [`../consentimiento-informado.md`](../consentimiento-informado.md) y [`../observaciones-piloto.md`](../observaciones-piloto.md).

## Pendiente

- Capturas de la revisión visual y sesiones con usuarios reales.
- Repetición en staging cuando exista #33.
