
package co.edu.usco.aprendizaje_usco.security;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping("/{rol}")
    @PreAuthorize("hasAuthority('ROLE_' + #rol)")
    public Map<String, String> ping(@PathVariable("rol") String rol) {
        return Map.of(
                "message", "Acceso autorizado",
                "rol", rol
        );
    }
}
