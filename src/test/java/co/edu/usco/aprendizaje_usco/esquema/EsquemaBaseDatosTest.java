package co.edu.usco.aprendizaje_usco.esquema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de integración del esquema inicial (tarea "Diseñar y crear el esquema
 * inicial de base de datos").
 *
 * Levantar PostgreSQL 17 con Testcontainers (requiere Docker), dejar que Flyway
 * aplique las migraciones y arranque la app con ddl-auto=validate: si el mapeo
 * de alguna entidad no coincide con las tablas, el contexto no arranca y todas
 * las pruebas fallan.
 *
 * @Transactional: cada prueba se deshace al terminar, así no se afectan entre sí.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
public class EsquemaBaseDatosTest {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayAplicaSoloLasMigracionesVersionadasEnPerfilTest() {
        List<String> scripts = jdbcTemplate.queryForList(
                "SELECT script FROM flyway_schema_history WHERE success ORDER BY installed_rank", String.class);

        assertThat(scripts).containsExactly(
                "V1__esquema_inicial.sql",
                "V2__datos_base_roles_competencias.sql",
                "V3__esquema_diagnostico.sql",
                "V4__datos_prueba_diagnostica.sql",
                "V5__esquema_practica.sql",
                "V6__datos_ejercicios_semilla.sql",
                "V202610242250__crear_grupos_docentes_estudiantes.sql");
    }

    @Test
    void existenLosTresRoles() {
        assertThat(rolRepository.findAll())
                .extracting(Rol::getNombre)
                .containsExactlyInAnyOrder(NombreRol.ESTUDIANTE, NombreRol.DOCENTE, NombreRol.ADMIN);
    }

    @Test
    void existenLasTresCompetencias() {
        assertThat(competenciaRepository.findAll())
                .extracting(Competencia::getCodigo)
                .containsExactlyInAnyOrder(Competencia.ALGEBRAICA, Competencia.LOGICA, Competencia.FUNCIONAL);

        assertThat(competenciaRepository.findByCodigo(Competencia.FUNCIONAL))
                .get()
                .extracting(Competencia::getNombre)
                .isEqualTo("Comprensión funcional");
    }

    @Test
    void enPerfilTestNoHayUsuariosDePrueba() {
        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void guardaYLeeUnUsuarioConSuRol() {
        Rol docente = rolRepository.findByNombre(NombreRol.DOCENTE).orElseThrow();
        usuarioRepository.saveAndFlush(
                new Usuario("Ana María", "Pérez", "  Ana.Perez@Prueba.TEST ", "$2a$10$hashDePrueba", docente));

        // Limpiar la caché de Hibernate obliga a leer de verdad desde PostgreSQL.
        entityManager.clear();

        Usuario leido = usuarioRepository.findByCorreo("ana.perez@prueba.test").orElseThrow();
        assertThat(leido.getId()).isNotNull();
        assertThat(leido.getCorreo()).isEqualTo("ana.perez@prueba.test");
        assertThat(leido.getRol().getNombre()).isEqualTo(NombreRol.DOCENTE);
        assertThat(leido.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(leido.getProgramaAcademico()).isNull();
        assertThat(leido.getCreadoEn()).isNotNull();
        assertThat(leido.getActualizadoEn()).isNotNull();
        assertThat(usuarioRepository.existsByCorreo("ana.perez@prueba.test")).isTrue();
    }

    @Test
    void noPermiteDosUsuariosConElMismoCorreo() {
        Rol estudiante = rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow();
        usuarioRepository.saveAndFlush(new Usuario("Luis", "Gómez", "luis@prueba.test", "hash", estudiante));

        // Mismo correo con otras mayúsculas: también debe fallar, porque se normaliza.
        Usuario duplicado = new Usuario("Luis", "Otro", "LUIS@prueba.test", "hash", estudiante);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laBaseRechazaUnEstadoInvalido() {
        Rol admin = rolRepository.findByNombre(NombreRol.ADMIN).orElseThrow();
        Usuario usuario = usuarioRepository.saveAndFlush(
                new Usuario("Admin", "Prueba", "estado@prueba.test", "hash", admin));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE usuario SET estado = 'BORRADO' WHERE id = ?", usuario.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}
