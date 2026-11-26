package co.edu.usco.aprendizaje_usco.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Hallazgo 4 de la revisión: /api/ping/{rol} solo existe en las pruebas.
 * En la configuración normal no se registra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PingEnConfiguracionNormalTest {

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void sinPerfilTestNoSeRegistraElPing() {
        assertThat(contexto.getBeanNamesForType(PingController.class)).isEmpty();
    }

    @Test
    void sinPerfilTestLaRutaDelPingResponde404() throws Exception {
        String token = jwtService.emitir(admin()).valor();

        mockMvc.perform(get("/api/ping/ADMIN").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private static UsuarioAutenticado admin() {
        Rol rol = mock(Rol.class);
        when(rol.getNombre()).thenReturn(NombreRol.ADMIN);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(1L);
        when(usuario.getCorreo()).thenReturn("admin@aprendizaje.test");
        when(usuario.getRol()).thenReturn(rol);
        return UsuarioAutenticado.desde(usuario);
    }

}
