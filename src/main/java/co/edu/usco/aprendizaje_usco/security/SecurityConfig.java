package co.edu.usco.aprendizaje_usco.security;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RespuestasErrorSeguridad respuestasError;
    private final JwtRoleConverter jwtRoleConverter;

    public SecurityConfig(
            RespuestasErrorSeguridad respuestasError,
            JwtRoleConverter jwtRoleConverter) {
        this.respuestasError = respuestasError;
        this.jwtRoleConverter = jwtRoleConverter;
    }

    // La clave se configura en Base64; HS256 requiere al menos 256 bits.
    @Bean
    SecretKey jwtSecretKey(
            @Value("${security.jwt.secret}") String secretBase64) {
        byte[] bytes = Base64.getDecoder().decode(secretBase64);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "La clave JWT debe contener al menos 32 bytes");
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // Resource Server usa este decoder para verificar la firma y vigencia del JWT.
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Convierte el claim "rol" del JWT en authorities de Spring Security.
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwtRoleConverter);
        return converter;
    }

    // Las rutas de autenticación y documentación son públicas; las demás exigen JWT.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint(respuestasError)
                .accessDeniedHandler(respuestasError))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(respuestasError)
                .accessDeniedHandler(respuestasError));

        return http.build();
    }

}
