# Pruebas de rendimiento

Script de carga de los servicios críticos de RNF-02:

- `GET /api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL`
- `POST /api/diagnosticos/intentos/{id}/finalizar`

## Requisitos

- Backend corriendo con la configuración local de `.env` y PostgreSQL.
- Docker Desktop (para ejecutar k6 sin instalarlo).
- Al menos 30 estudiantes disponibles; el script los crea por API.

## Ejecución

Con el backend en `http://localhost:8080` y k6 en Docker:

```powershell
docker run --rm -i `
  -e BASE_URL=http://host.docker.internal:8080 `
  -v "${PWD}:/scripts" `
  grafana/k6 run --summary-export=/scripts/pruebas/rendimiento/resultado.json `
  /scripts/pruebas/rendimiento/k6-practica-diagnostico.js
```

Si k6 está instalado en el equipo:

```powershell
$env:BASE_URL = 'http://localhost:8080'
k6 run --summary-export=pruebas/rendimiento/resultado.json pruebas/rendimiento/k6-practica-diagnostico.js
```

Variables opcionales:

| Variable | Valor por defecto | Uso |
|---|---|---|
| `BASE_URL` | `http://localhost:8080` | URL del backend |
| `USUARIOS` | `30` | VUs y estudiantes creados |
| `DURACION` | `1m` | Duración del escenario |
| `PASSWORD` | `Estudiante2026*` | Contraseña de los estudiantes creados |

## Umbrales

- `p(95) < 1000 ms` para cada endpoint.
- Tasa de errores HTTP menor al 1 % por endpoint.

El resultado se exporta a `pruebas/rendimiento/resultado.json` y se registra en
`docs/code/rendimiento-servicios-criticos.md` para el capítulo 7 del informe.

## Nota sobre staging

El registro público de estudiantes se usa para crear la carga. Si staging tiene
el registro cerrado por invitación (#150), usar cuentas creadas previamente y
adaptar el `setup()` para iniciar sesión con esas cuentas.
