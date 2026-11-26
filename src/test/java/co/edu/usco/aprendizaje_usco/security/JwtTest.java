package co.edu.usco.aprendizaje_usco.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SCRUM-83: emisión y validación del JWT, de punta a punta por HTTP.
 * Criterio: un token emitido es aceptado; uno alterado o vencido responde 401.
 *
 * Usa dos endpoints que solo existen en esta prueba (ControladorDePrueba).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, JwtTest.ControladorDePrueba.class})
@ActiveProfiles("test")
class JwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties props;

    @Test
    void tokenEmitidoEsAceptado() throws Exception {
        String token = emitir(7L, NombreRol.ESTUDIANTE);

        mockMvc.perform(get("/prueba/protegido").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("7 ROLE_ESTUDIANTE"));
    }

    @Test
    void tokenTieneLosClaimsDelContrato() {
        JwtService.TokenEmitido emitido = jwtService.emitir(usuario(7L, NombreRol.DOCENTE));
        String payload = decodificar(emitido.valor().split("\\.")[1]);

        assertThat(payload)
                .contains("\"sub\":\"7\"", "\"correo\":\"ana@usco.edu.co\"", "\"rol\":\"DOCENTE\"",
                          "\"iat\":", "\"exp\":");
        assertThat(emitido.expiraEn())
                .isBetween(Instant.now().plus(Duration.ofMinutes(props.expirationMinutes() - 1)),
                           Instant.now().plus(Duration.ofMinutes(props.expirationMinutes() + 1)));
    }

    @Test
    void sinTokenResponde401ConFormatoEstandar() throws Exception {
        mockMvc.perform(get("/prueba/protegido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Debes iniciar sesión para continuar"));
    }

    @Test
    void tokenAlteradoResponde401() throws Exception {
        // Se cambia el rol a ADMIN en el contenido del token sin volver a firmarlo.
        String[] partes = emitir(7L, NombreRol.ESTUDIANTE).split("\\.");
        String payloadFalso = codificar(decodificar(partes[1]).replace("ESTUDIANTE", "ADMIN"));
        String tokenAlterado = partes[0] + "." + payloadFalso + "." + partes[2];

        mockMvc.perform(get("/prueba/protegido").header("Authorization", "Bearer " + tokenAlterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tokenVencidoResponde401() throws Exception {
        // Emitido hace 1 día: ya pasó su expiración (más los 60 s de tolerancia).
        Clock ayer = Clock.fixed(Instant.now().minus(Duration.ofDays(1)), ZoneOffset.UTC);
        String vencido = new JwtService(jwtEncoder, props, ayer)
                .emitir(usuario(7L, NombreRol.ESTUDIANTE)).valor();

        mockMvc.perform(get("/prueba/protegido").header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalFormadoResponde401() throws Exception {
        mockMvc.perform(get("/prueba/protegido").header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elRolDelTokenSeUsaParaLosPermisos() throws Exception {
        mockMvc.perform(get("/prueba/solo-docente")
                        .header("Authorization", "Bearer " + emitir(1L, NombreRol.ESTUDIANTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/prueba/solo-docente")
                        .header("Authorization", "Bearer " + emitir(2L, NombreRol.DOCENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void enElLoginSeIgnoraUnTokenVencido() throws Exception {
        // Si el navegador guarda un token viejo, igual debe poder volver a iniciar sesión.
        mockMvc.perform(post("/api/auth/login").header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().is(not(401)));
    }

    // ------------------------------------------------------------------ apoyo

    private String emitir(Long id, NombreRol rol) {
        return jwtService.emitir(usuario(id, rol)).valor();
    }

    private static UsuarioAutenticado usuario(Long id, NombreRol nombreRol) {
        Rol rol = mock(Rol.class);
        when(rol.getNombre()).thenReturn(nombreRol);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getCorreo()).thenReturn("ana@usco.edu.co");
        when(usuario.getNombres()).thenReturn("Ana");
        when(usuario.getRol()).thenReturn(rol);
        return UsuarioAutenticado.desde(usuario);
    }

    private static String decodificar(String parte) {
        return new String(Base64.getUrlDecoder().decode(parte), StandardCharsets.UTF_8);
    }

    private static String codificar(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    @RestController
    static class ControladorDePrueba {

        @GetMapping("/prueba/protegido")
        String protegido(Authentication auth) {
            String rol = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .findFirst().orElse("");
            return auth.getName() + " " + rol;
        }

        @GetMapping("/prueba/solo-docente")
        @PreAuthorize("hasRole('DOCENTE')")
        String soloDocente() {
            return "ok";
        }
    }

}
