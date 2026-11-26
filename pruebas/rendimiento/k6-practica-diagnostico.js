/**
 * Rendimiento de los servicios críticos (RNF-02).
 *
 * Uso local (k6 en Docker):
 *   docker run --rm -i ^
 *     -e BASE_URL=http://host.docker.internal:8080 ^
 *     -v "%CD%:/scripts" ^
 *     grafana/k6 run --summary-export=/scripts/pruebas/rendimiento/resultado.json ^
 *     /scripts/pruebas/rendimiento/k6-practica-diagnostico.js
 *
 * Variables:
 *   BASE_URL   URL del backend (por defecto http://localhost:8080)
 *   USUARIOS   número de estudiantes/VUs (por defecto 30)
 *   DURACION   duración del escenario (por defecto 1m)
 *   PASSWORD   contraseña de los estudiantes creados (por defecto Estudiante2026*)
 *
 * El setup crea los estudiantes con POST /api/auth/registro y deja un intento
 * diagnóstico listo. Si el correo ya existe, inicia sesión.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const USUARIOS = Number(__ENV.USUARIOS || 30);
const DURACION = __ENV.DURACION || '1m';
const PASSWORD = __ENV.PASSWORD || 'Estudiante2026*';
const PROGRAMA = 'Ingeniería de Software';

export const options = {
  scenarios: {
    carga: {
      executor: 'constant-vus',
      vus: USUARIOS,
      duration: DURACION,
      gracefulStop: '10s',
    },
  },
  thresholds: {
    'http_req_duration{endpoint:practica_siguiente}': ['p(95)<1000'],
    'http_req_duration{endpoint:diagnostico_finalizar}': ['p(95)<1000'],
    'http_req_failed{endpoint:practica_siguiente}': ['rate<0.01'],
    'http_req_failed{endpoint:diagnostico_finalizar}': ['rate<0.01'],
  },
};

const jsonHeaders = { 'Content-Type': 'application/json' };

function auth(token) {
  return { Authorization: `Bearer ${token}` };
}

function correoDeCarga(indice) {
  const marca = String(Date.now()).slice(-6);
  const secuencia = String(indice + 1).padStart(5, '0');
  return `u${marca}${secuencia}@usco.edu.co`;
}

function registrarOCrearSesion(correo) {
  const registro = http.post(
    `${BASE}/api/auth/registro`,
    JSON.stringify({
      nombres: 'Carga',
      apellidos: `K6 ${correo.slice(-5)}`,
      correo,
      password: PASSWORD,
      programaAcademico: PROGRAMA,
    }),
    { headers: jsonHeaders, tags: { setup: 'true' } },
  );

  if (registro.status !== 201 && registro.status !== 409) {
    throw new Error(`No se pudo registrar ${correo}: ${registro.status} ${registro.body}`);
  }

  const login = http.post(
    `${BASE}/api/auth/login`,
    JSON.stringify({ correo, password: PASSWORD }),
    { headers: jsonHeaders, tags: { setup: 'true' } },
  );
  if (login.status !== 200) {
    throw new Error(`No se pudo iniciar sesión ${correo}: ${login.status} ${login.body}`);
  }
  return login.json('token');
}

export function setup() {
  const usuarios = [];
  for (let i = 0; i < USUARIOS; i += 1) {
    const correo = correoDeCarga(i);
    const token = registrarOCrearSesion(correo);
    const intento = http.post(`${BASE}/api/diagnosticos/intentos`, null, {
      headers: auth(token),
      tags: { setup: 'true' },
    });
    if (intento.status !== 200 && intento.status !== 201) {
      throw new Error(`No se pudo iniciar el diagnóstico de ${correo}: ${intento.status} ${intento.body}`);
    }
    usuarios.push({ token, intentoId: intento.json('id') });
  }
  return { usuarios };
}

export default function (data) {
  const usuario = data.usuarios[(__VU - 1) % data.usuarios.length];

  const siguiente = http.get(
    `${BASE}/api/practica/siguiente?asignatura=CALCULO_DIFERENCIAL`,
    { headers: auth(usuario.token), tags: { endpoint: 'practica_siguiente' } },
  );
  check(siguiente, {
    'siguiente responde 200': (r) => r.status === 200,
  });

  const finalizar = http.post(
    `${BASE}/api/diagnosticos/intentos/${usuario.intentoId}/finalizar`,
    null,
    { headers: auth(usuario.token), tags: { endpoint: 'diagnostico_finalizar' } },
  );
  check(finalizar, {
    'finalizar responde 200': (r) => r.status === 200,
  });

  sleep(0.5);
}
