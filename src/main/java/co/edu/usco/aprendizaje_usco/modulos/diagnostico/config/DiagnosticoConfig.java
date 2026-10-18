package co.edu.usco.aprendizaje_usco.modulos.diagnostico.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registra las propiedades app.diagnostico.umbrales.* (UmbralesDiagnostico). */
@Configuration
@EnableConfigurationProperties(UmbralesDiagnostico.class)
public class DiagnosticoConfig {
}
