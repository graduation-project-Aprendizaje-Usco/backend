# Integración end-to-end del Sprint 4 (#140)

**Estado:** verificado en local el 10 de noviembre de 2026. **Staging pendiente de #33.**

## Qué se valida

Que los módulos compartan usuarios, roles y datos, y que los tres flujos completos se puedan recorrer sin modificar la base de datos a mano.

## Prueba automatizada

```powershell
.\mvnw.cmd "-Dtest=FlujosCompletosE2ETest,AdminActividadesControllerTest" test
```

`FlujosCompletosE2ETest` usa Testcontainers, MockMvc y login real:

1. **Administrador:** crea un docente y un estudiante con contraseña temporal, verifica el login, desactiva/reactiva al estudiante, impide desactivarse a sí mismo y consulta panel y catálogo.
2. **Docente:** crea un grupo, inscribe al estudiante, consulta la lista de estudiantes, la analítica del grupo y el historial del estudiante.
3. **Estudiante:** presenta los 24 ítems del diagnóstico, resuelve tres ejercicios de práctica con retroalimentación y consulta su progreso e historial.

Resultado local: **backend completo con 274 pruebas, 0 fallos** (incluye las 2 E2E y las 2 del catálogo de actividades), además de `npm run lint`, `npm test` (122 pruebas) y `npm run build` en el frontend.

## Recorrido manual en local

Con `docker compose up -d`, `.\mvnw.cmd spring-boot:run` y `npm run dev`:

1. Entrar como `admin@aprendizaje.test` / `Admin2026*`.
2. En **Usuarios**, crear un docente y copiar su contraseña temporal.
3. En **Grupos** y **Actividades**, comprobar que el panel enlaza a las vistas y que muestran los datos reales.
4. Cerrar sesión y entrar con el docente creado; crear un grupo e inscribir a un estudiante registrado.
5. Entrar como estudiante; presentar el diagnóstico, resolver ejercicios en **Práctica** y abrir **Mi progreso**.
6. Volver al docente y comprobar que el grupo muestra al estudiante y su actividad.
7. Volver al administrador y verificar que los conteos del panel incluyen al nuevo docente, el grupo y la actividad.

No se requiere ningún `INSERT`, `UPDATE` ni ajuste manual en PostgreSQL.

## Pendiente

Repetir este mismo recorrido sobre la URL de staging cuando se complete #33. Hasta entonces, el criterio de aceptación de #140 queda cumplido en local y **bloqueado en staging**.
