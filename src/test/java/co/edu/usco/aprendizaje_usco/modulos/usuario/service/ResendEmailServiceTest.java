package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.edu.usco.aprendizaje_usco.common.exception.ServicioNoDisponibleException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ResendEmailServiceTest {

    private static final String API_URL = "https://api.resend.com/emails";
    private static final String FROM = "Aprendizaje USCO <noreply@aprendizaje-usco.tech>";

    @Test
    void enviaPorHttpsConBearerUserAgentYContenidoPlano() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer re_test_key"))
                .andExpect(header(HttpHeaders.USER_AGENT, "aprendizaje-usco-backend/0.0.1"))
                .andExpect(content().json("""
                        {
                          "from": "Aprendizaje USCO <noreply@aprendizaje-usco.tech>",
                          "to": ["docente@usco.edu.co"],
                          "subject": "Asunto de prueba",
                          "text": "Mensaje de prueba",
                          "html": "<p>Mensaje HTML</p>"
                        }
                        """))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));
        ResendEmailService service = new ResendEmailService(builder.build(), "re_test_key", FROM);

        service.enviar("docente@usco.edu.co", "Asunto de prueba", "Mensaje de prueba", "<p>Mensaje HTML</p>");

        server.verify();
    }

    @Test
    void apiKeyAusenteNoRealizaLaPeticion() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = new ResendEmailService(builder.build(), " ", FROM);

        assertThatThrownBy(() -> service.enviar("docente@usco.edu.co", "Asunto", "Texto", "<p>Texto</p>"))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("RESEND_API_KEY");

        server.verify();
    }

    @Test
    void apiKeyInvalidaSeReportaComoProveedorNoDisponibleSinFiltrarLaClave() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(API_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"name\":\"authentication_error\",\"message\":\"Invalid API key\"}"));
        ResendEmailService service = new ResendEmailService(builder.build(), "re_secret_test", FROM);

        assertThatThrownBy(() -> service.enviar("docente@usco.edu.co", "Asunto", "Texto", "<p>Texto</p>"))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageNotContaining("re_secret_test");

        server.verify();
    }

    @Test
    void destinatarioRechazadoNoSeConsideraEnviado() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(API_URL))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"name\":\"validation_error\",\"message\":\"Invalid recipient\"}"));
        ResendEmailService service = new ResendEmailService(builder.build(), "re_test_key", FROM);

        assertThatThrownBy(() -> service.enviar("no-es-un-correo", "Asunto", "Texto", "<p>Texto</p>"))
                .isInstanceOf(ServicioNoDisponibleException.class);

        server.verify();
    }

    @Test
    void falloDeConexionSeReportaComoProveedorNoDisponible() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(API_URL))
                .andRespond(withException(new IOException("connection timed out")));
        ResendEmailService service = new ResendEmailService(builder.build(), "re_test_key", FROM);

        assertThatThrownBy(() -> service.enviar("docente@usco.edu.co", "Asunto", "Texto", "<p>Texto</p>"))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("conectar");

        server.verify();
    }
}
