package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "app.bootstrap-admin.email=bootstrap-admin@example.org",
        "app.bootstrap-admin.password=BootstrapPassword2026!",
        "app.bootstrap-admin.nombres=Admin",
        "app.bootstrap-admin.apellidos=Inicial"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class BootstrapAdminRunnerIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BootstrapAdminRunner bootstrapAdminRunner;

    @Test
    void creaAdminConFlywayYNoLoDuplicaAlReiniciar() throws Exception {
        var correo = "bootstrap-admin@example.org";
        var administrador = usuarioRepository.findByCorreo(correo).orElseThrow();

        assertThat(administrador.getRol().getNombre()).isEqualTo(NombreRol.ADMIN);
        assertThat(administrador.estaActivo()).isTrue();
        assertThat(passwordEncoder.matches("BootstrapPassword2026!", administrador.getPasswordHash())).isTrue();
        assertThat(usuarioRepository.countByRol_Nombre(NombreRol.ADMIN)).isEqualTo(1);

        bootstrapAdminRunner.run(mock(ApplicationArguments.class));

        assertThat(usuarioRepository.countByRol_Nombre(NombreRol.ADMIN)).isEqualTo(1);
    }

}
