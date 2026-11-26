
package co.edu.usco.aprendizaje_usco.security;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");

        if (rol == null || rol.isBlank()) {
            return List.of();
        }

        String normalizado = rol.trim()
                .toUpperCase(Locale.ROOT)
                .replaceFirst("^ROLE_", "");

        if (!normalizado.matches("ESTUDIANTE|DOCENTE|ADMIN")) {
            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + normalizado)
        );
    }
}
