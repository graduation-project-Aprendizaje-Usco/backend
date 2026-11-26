package co.edu.usco.aprendizaje_usco.config;


import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS para el frontend. Spring Security lo usa automáticamente gracias a
 * {@code .cors(Customizer.withDefaults())} en SecurityConfig.
 *
 * Los orígenes vienen de app.cors.allowed-origins (variable CORS_ORIGINS en
 * staging). Varios orígenes se separan con coma:
 *   CORS_ORIGINS=http://localhost:5173,https://aprendizaje-usco.ejemplo.app
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String origenesPermitidos) {

        List<String> origenes = Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Content-Type es necesario para los POST/PUT con JSON (el navegador hace preflight).
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Location"));
        // El token viaja en el header Authorization, no en cookies.
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

}
