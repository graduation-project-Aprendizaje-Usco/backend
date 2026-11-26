package co.edu.usco.aprendizaje_usco.modulos.practica.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PracticaProperties.class, ReglasNivelProperties.class})
public class PracticaConfig {
}
