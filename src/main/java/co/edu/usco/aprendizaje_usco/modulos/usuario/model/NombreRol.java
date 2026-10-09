package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

/**
 * Roles de la plataforma. Deben coincidir con:
 *  - el CHECK ck_rol_nombre de la tabla rol,
 *  - el claim "rol" del JWT,
 *  - ROLES en el frontend (src/routes/navegacion.js).
 */
public enum NombreRol {
    ESTUDIANTE,
    DOCENTE,
    ADMIN
}
