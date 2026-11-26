package co.edu.usco.aprendizaje_usco.security;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RespuestasErrorSeguridad respuestasError;

    public SecurityConfig(RespuestasErrorSeguridad respuestasError) {
        this.respuestasError = respuestasError;
    }

    /** Rutas donde se ignora el header Authorization (ver bearerTokenResolver). */
    private static final Set<String> RUTAS_SIN_TOKEN = Set.of(
            "/api/auth/login", "/api/auth/registro", "/api/auth/invitacion", "/api/auth/activar-cuenta");

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // NUEVO: usa el bean CorsConfigurationSource de config/CorsConfig
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Solo las rutas de autenticación y activación son públicas; /api/auth/me exige token.
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro",
                        "/api/auth/activar-cuenta").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/invitacion").permitAll()
                .requestMatchers("/actuator/health",
                                 "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            // NUEVO: 401 y 403 en el formato estándar de ErrorResponse
            .exceptionHandling(e -> e
                .authenticationEntryPoint(respuestasError)
                .accessDeniedHandler(respuestasError))
            // Valida "Authorization: Bearer <token>" con el JwtDecoder de JwtConfig.
            // Token alterado, vencido o mal formado → 401 en el formato estándar.
            .oauth2ResourceServer(o -> o
                .bearerTokenResolver(bearerTokenResolver())
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint(respuestasError)
                .accessDeniedHandler(respuestasError));

        return http.build();
    }

    /**
     * En login y registro se ignora el token: el frontend lo envía en todas las
     * peticiones, y uno vencido guardado en el navegador haría fallar el login con 401.
     */
    private static BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver porDefecto = new DefaultBearerTokenResolver();
        return request -> {
            String ruta = request.getRequestURI().substring(request.getContextPath().length());
            return RUTAS_SIN_TOKEN.contains(ruta) ? null : porDefecto.resolve(request);
        };
    }

    /** BCrypt (costo 10 por defecto), el mismo de los hashes en db/dev-data. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Valida correo y contraseña contra la base. Lo usará el AuthService del login:
     * authenticate() devuelve el UsuarioAutenticado o lanza BadCredentialsException.
     */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

}
