package co.edu.usco.aprendizaje_usco.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Firma (JwtEncoder) y validación (JwtDecoder) de los tokens con una llave
 * simétrica HS256 tomada de app.jwt.secret.
 *
 * El JwtDecoder valida firma y expiración (con 60 s de tolerancia): un token
 * alterado o vencido no autentica y Spring Security responde 401.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(JwtProperties props) {
        return NimbusJwtEncoder.withSecretKey(llave(props)).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties props) {
        return NimbusJwtDecoder.withSecretKey(llave(props)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Convierte el claim "rol" del token (ej. "DOCENTE") en la autoridad
     * "ROLE_DOCENTE", que es lo que revisa @PreAuthorize("hasRole('DOCENTE')").
     * JwtRoleConverter (SCRUM-12) descarta cualquier rol que no sea uno de los 3 válidos.
     * El nombre del usuario autenticado queda como el claim "sub" (su id).
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(JwtRoleConverter jwtRoleConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtRoleConverter);
        return converter;
    }

    private static SecretKey llave(JwtProperties props) {
        return new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

}
