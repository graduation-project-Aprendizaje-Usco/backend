package co.edu.usco.aprendizaje_usco.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * SCRUM-91: 401 y 403 por rol sobre /api/ping/{rol} (PingController).
 * Pensada para reutilizarse en el hardening del Sprint 4: los endpoints reales
 * de cada rol pueden agregarse con el mismo patrón.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AccesoPorRolTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    /** Cada rol entra solo a su ping; a los demás recibe 403 en el formato estándar. */
    @ParameterizedTest
    @EnumSource(NombreRol.class)
    void cadaRolSoloEntraASuPropioEndpoint(NombreRol rol) throws Exception {
        String token = jwtService.emitir(usuario(rol)).valor();

        for (NombreRol destino : NombreRol.values()) {
            var respuesta = mockMvc.perform(get("/api/ping/" + destino).header("Authorization", "Bearer " + token));
            if (destino == rol) {
                respuesta.andExpect(status().isOk()).andExpect(jsonPath("$.rol").value(rol.name()));
            } else {
                respuesta.andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value(403))
                        .andExpect(jsonPath("$.message").value("No tienes permiso para realizar esta acción"));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(NombreRol.class)
    void sinTokenResponde401(NombreRol destino) throws Exception {
        mockMvc.perform(get("/api/ping/" + destino))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void unRolDesconocidoEnElTokenNoDaPermisos() throws Exception {
        // JwtRoleConverter descarta roles que no sean ESTUDIANTE, DOCENTE o ADMIN.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("9").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600))
                .claim(JwtService.CLAIM_ROL, "SUPERADMIN").build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/ping/SUPERADMIN").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private static UsuarioAutenticado usuario(NombreRol nombreRol) {
        Rol rol = mock(Rol.class);
        when(rol.getNombre()).thenReturn(nombreRol);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(1L);
        when(usuario.getCorreo()).thenReturn("prueba@usco.edu.co");
        when(usuario.getRol()).thenReturn(rol);
        return UsuarioAutenticado.desde(usuario);
    }

}
