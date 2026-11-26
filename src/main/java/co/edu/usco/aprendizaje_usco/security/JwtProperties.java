package co.edu.usco.aprendizaje_usco.security;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades app.jwt.* de application.properties.
 *
 * @param secret            llave de firma HS256 (variable JWT_SECRET); mínimo 32 bytes
 * @param expirationMinutes duración del token en minutos (variable JWT_EXPIRATION_MINUTES)
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {

    /**
     * HS256 exige una llave de al menos 256 bits; si no, la app no arranca.
     * Sin JWT_SECRET la app no arranca, independientemente del entorno.
     */
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno JWT_SECRET (llave para firmar los tokens). "
                    + "Defínela en el archivo .env local o en el entorno de despliegue.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) debe tener mínimo 32 caracteres");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("app.jwt.expiration-minutes debe ser mayor que 0");
        }
    }

}
