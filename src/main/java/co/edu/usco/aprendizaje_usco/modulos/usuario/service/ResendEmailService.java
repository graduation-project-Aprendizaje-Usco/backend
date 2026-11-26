package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ServicioNoDisponibleException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Cliente centralizado de la API HTTPS de Resend para correos transaccionales. */
@Service
public class ResendEmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private static final String USER_AGENT = "aprendizaje-usco-backend/0.0.1";

    private final RestClient restClient;
    private final String apiKey;
    private final String remitente;

    public ResendEmailService(@Qualifier("resendRestClient") RestClient restClient,
                              @Value("${app.resend.api-key:}") String apiKey,
                              @Value("${app.mail.from:Aprendizaje USCO <noreply@aprendizaje-usco.tech>}")
                              String remitente) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.remitente = remitente;
    }

    public void enviar(String destinatario, String asunto, String texto, String html) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new ReglaNegocioException("El correo destinatario es obligatorio.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new ServicioNoDisponibleException(
                    "El envío de correo no está configurado: falta RESEND_API_KEY.");
        }

        SolicitudCorreo solicitud = new SolicitudCorreo(remitente, List.of(destinatario), asunto, texto, html);
        RespuestaCorreo respuesta;
        try {
            respuesta = restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(RespuestaCorreo.class);
        } catch (RestClientResponseException ex) {
            log.error("Resend rechazó el correo con HTTP {}", ex.getStatusCode().value());
            throw new ServicioNoDisponibleException(
                    "El proveedor de correo no pudo aceptar el mensaje. Revisa la configuración de Resend.");
        } catch (ResourceAccessException ex) {
            log.error("No se pudo conectar con la API de Resend", ex);
            throw new ServicioNoDisponibleException(
                    "No se pudo conectar con el proveedor de correo. Inténtalo de nuevo más tarde.");
        } catch (RestClientException ex) {
            log.error("Falló la comunicación HTTP con la API de Resend", ex);
            throw new ServicioNoDisponibleException(
                    "Falló la comunicación con el proveedor de correo. Inténtalo de nuevo más tarde.");
        }

        if (respuesta == null || respuesta.id() == null || respuesta.id().isBlank()) {
            log.error("Resend respondió sin un identificador de correo");
            throw new ServicioNoDisponibleException(
                    "El proveedor de correo devolvió una respuesta inválida.");
        }
    }

    private record SolicitudCorreo(String from, List<String> to, String subject, String text, String html) {
    }

    public record RespuestaCorreo(String id) {
    }
}
