package co.edu.usco.aprendizaje_usco.security;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Emite el JWT del login. Claims (contrato de SCRUM-81):
 *   sub    → id del usuario (texto)
 *   correo → correo del usuario
 *   rol    → ESTUDIANTE, DOCENTE o ADMIN
 *   iat / exp → emisión y expiración
 *
 * La validación de los tokens que llegan la hace el JwtDecoder de JwtConfig.
 */
@Service
public class JwtService {

    public static final String CLAIM_CORREO = "correo";
    public static final String CLAIM_ROL = "rol";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties props;
    private final Clock clock;

    @Autowired
    public JwtService(JwtEncoder jwtEncoder, JwtProperties props) {
        this(jwtEncoder, props, Clock.systemUTC());
    }

    /** Con reloj propio: las pruebas lo usan para emitir tokens ya vencidos. */
    JwtService(JwtEncoder jwtEncoder, JwtProperties props, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.props = props;
        this.clock = clock;
    }

    public TokenEmitido emitir(UsuarioAutenticado usuario) {
        Instant ahora = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expira = ahora.plus(props.expirationMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(CLAIM_CORREO, usuario.getCorreo())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenEmitido(token, expira);
    }

    /** Token firmado y el momento en que vence. */
    public record TokenEmitido(String valor, Instant expiraEn) {
    }

}
