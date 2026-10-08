package co.edu.usco.aprendizaje_usco.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación de la API en /swagger-ui.html con el botón "Authorize".
 *
 * Cómo probar un endpoint protegido:
 *   1. Ejecutar POST /api/auth/login y copiar el valor de "token".
 *   2. Clic en "Authorize", pegar SOLO el token (sin la palabra Bearer) y aceptar.
 *   3. Las siguientes peticiones desde Swagger llevan el header Authorization.
 *
 * Todos los endpoints quedan marcados como protegidos por defecto. En los
 * públicos (ej. AuthController) se puede quitar el candado con
 * {@code @SecurityRequirements()} sobre la clase o el método.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Aprendizaje USCO",
                version = "0.1.0",
                description = "Diagnóstico, práctica adaptativa y analítica de aprendizaje para Cálculo Diferencial."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtenido en POST /api/auth/login")
public class OpenApiConfig {

}
