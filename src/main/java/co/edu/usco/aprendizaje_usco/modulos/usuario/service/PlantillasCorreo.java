package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/** Carga y renderiza las plantillas HTML de los correos transaccionales. */
@Component
public class PlantillasCorreo {

    private static final String LOGO_PATH = "/img/usco-logo-blanco.png";

    private final String invitacion;
    private final String activacion;
    private final String logoUrl;

    public PlantillasCorreo(ResourceLoader resourceLoader,
                            @Value("${FRONTEND_URL:http://localhost:5173}") String frontendUrl) {
        this.invitacion = cargar(resourceLoader, "classpath:/templates/email/invitacion-grupo.html");
        this.activacion = cargar(resourceLoader, "classpath:/templates/email/activar-cuenta.html");
        this.logoUrl = crearLogoUrl(frontendUrl);
    }

    public String invitacion(String enlace) {
        return render(invitacion, Map.of(
                "logoUrl", logoUrl,
                "actionUrl", validarEnlace(enlace)));
    }

    public String activacion(String nombres, String enlace, long vigenciaHoras) {
        return render(activacion, Map.of(
                "logoUrl", logoUrl,
                "nombres", Objects.toString(nombres, ""),
                "actionUrl", validarEnlace(enlace),
                "vigenciaHoras", Long.toString(vigenciaHoras)));
    }

    private static String cargar(ResourceLoader resourceLoader, String ruta) {
        Resource resource = resourceLoader.getResource(ruta);
        try (var input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar la plantilla de correo " + ruta, ex);
        }
    }

    private static String crearLogoUrl(String frontendUrl) {
        URI base = URI.create(frontendUrl);
        validarUrlWeb(base);
        return base.resolve(LOGO_PATH).toASCIIString();
    }

    private static String validarEnlace(String enlace) {
        URI uri = URI.create(enlace);
        validarUrlWeb(uri);
        return enlace;
    }

    private static void validarUrlWeb(URI uri) {
        String scheme = uri.getScheme();
        if (uri.getHost() == null
                || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("Los enlaces del correo deben ser URL HTTP o HTTPS absolutas.");
        }
    }

    private static String render(String plantilla, Map<String, String> valores) {
        String resultado = plantilla;
        for (Map.Entry<String, String> valor : valores.entrySet()) {
            resultado = resultado.replace("{{" + valor.getKey() + "}}", escaparHtml(valor.getValue()));
        }
        return resultado;
    }

    private static String escaparHtml(String valor) {
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
