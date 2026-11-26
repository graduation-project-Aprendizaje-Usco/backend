package co.edu.usco.aprendizaje_usco.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reloj de la aplicación y tareas programadas.
 *
 * Los servicios que dependen de la hora (por ejemplo, el tiempo límite de la prueba
 * diagnóstica) reciben este Clock en vez de llamar a Instant.now(): así una prueba
 * unitaria puede usar un reloj fijo.
 *
 * @EnableScheduling activa los métodos @Scheduled (ej. BarridoIntentosVencidos).
 */
@Configuration
@EnableScheduling
public class TiempoConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

}
