package co.edu.usco.aprendizaje_usco.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    @Test
    void permiteElFrontendLocalYElDominioPublicado() {
        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(
                "http://localhost:5173,https://www.aprendizaje-usco.tech");

        CorsConfiguration configuration = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/auth/login"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "https://www.aprendizaje-usco.tech");
    }
}
