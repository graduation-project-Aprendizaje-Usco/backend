package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;

class CorreoInvitacionServiceTest {

    @Test
    void creaCorreoDeInvitacionConBotonLogoYTextoAlternativo() {
        ResendEmailService resend = mock(ResendEmailService.class);
        CorreoInvitacionService correoService = new CorreoInvitacionService(resend, plantillas());
        String enlace = "https://aprendizaje-usco.tech/registro?token=token-seguro";
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);

        correoService.enviar("estudiante@usco.edu.co", enlace);

        verify(resend).enviar(eq("estudiante@usco.edu.co"), eq("Invitación a Aprendizaje USCO"),
                texto.capture(), html.capture());
        assertThat(texto.getValue())
                .contains("Un docente te invitó a unirte a su grupo")
                .contains("válido por 7 días")
                .contains(enlace);
        assertThat(html.getValue())
                .contains("src=\"https://www.aprendizaje-usco.tech/img/usco-logo-blanco.png\"")
                .contains("href=\"" + enlace + "\"")
                .contains("Aceptar invitación")
                .doesNotContain(">" + enlace + "<");
    }

    @Test
    void creaCorreoDeActivacionConNombreEscapadoYBoton() {
        ResendEmailService resend = mock(ResendEmailService.class);
        CorreoInvitacionService correoService = new CorreoInvitacionService(resend, plantillas());
        String enlace = "https://aprendizaje-usco.tech/activar-cuenta?token=token-seguro&origen=correo";
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);

        correoService.enviarConfiguracionCuenta("docente@usco.edu.co", "Diego <Admin>", enlace, 168);

        verify(resend).enviar(eq("docente@usco.edu.co"),
                eq("Configura tu contraseña de Aprendizaje USCO"), texto.capture(), html.capture());
        assertThat(texto.getValue())
                .contains("Hola Diego <Admin>")
                .contains("válido por 168 horas y de un solo uso")
                .contains(enlace);
        assertThat(html.getValue())
                .contains("Hola Diego &lt;Admin&gt;")
                .contains("href=\"https://aprendizaje-usco.tech/activar-cuenta?token=token-seguro&amp;origen=correo\"")
                .contains("Configurar mi contraseña")
                .doesNotContain("<Admin>");
    }

    private static PlantillasCorreo plantillas() {
        return new PlantillasCorreo(new DefaultResourceLoader(), "https://www.aprendizaje-usco.tech");
    }
}
