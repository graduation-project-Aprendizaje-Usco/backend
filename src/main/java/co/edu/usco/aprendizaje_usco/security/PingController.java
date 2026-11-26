package co.edu.usco.aprendizaje_usco.security;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de PRUEBA del control de acceso por rol (SCRUM-12, SCRUM-91):
 * GET /api/ping/{rol} responde 200 solo al rol indicado y 403 a los demás.
 *
 * Existe únicamente en las pruebas automáticas: en ejecución normal la ruta
 * no se registra y responde 404. Las pruebas de AccesoPorRolTest lo usan.
 */
@Profile("test")
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
