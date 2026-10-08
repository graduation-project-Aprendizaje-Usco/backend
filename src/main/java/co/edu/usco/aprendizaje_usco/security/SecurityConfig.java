package co.edu.usco.aprendizaje_usco.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RespuestasErrorSeguridad respuestasError;

    public SecurityConfig(RespuestasErrorSeguridad respuestasError) {
        this.respuestasError = respuestasError;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // NUEVO: usa el bean CorsConfigurationSource de config/CorsConfig
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health",
                                 "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            // NUEVO: 401 y 403 en el formato estándar de ErrorResponse
            .exceptionHandling(e -> e
                .authenticationEntryPoint(respuestasError)
                .accessDeniedHandler(respuestasError));

        // Tarea de login (JWT): cuando exista el bean JwtDecoder, agregar aquí:
        //   .oauth2ResourceServer(o -> o
        //       .jwt(Customizer.withDefaults())
        //       .authenticationEntryPoint(respuestasError)
        //       .accessDeniedHandler(respuestasError));

        return http.build();
    }

}
