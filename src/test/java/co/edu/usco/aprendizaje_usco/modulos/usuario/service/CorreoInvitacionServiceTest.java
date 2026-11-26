package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CorreoInvitacionServiceTest {

    @Test
    void conservaContenidoYEnlaceDeInvitacionDeGrupo() {
        ResendEmailService resend = mock(ResendEmailService.class);
        CorreoInvitacionService correoService = new CorreoInvitacionService(resend);
        String enlace = "https://aprendizaje-usco.tech/registro?token=token-seguro";
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);

        correoService.enviar("estudiante@usco.edu.co", enlace);

        verify(resend).enviar(eq("estudiante@usco.edu.co"), eq("Invitación a Aprendizaje USCO"), texto.capture());
        assertThat(texto.getValue())
                .contains("Un docente te invitó a unirte a su grupo")
                .contains("válido por 7 días")
                .contains(enlace);
    }

    @Test
    void conservaContenidoVigenciaYEnlaceDeActivacion() {
        ResendEmailService resend = mock(ResendEmailService.class);
        CorreoInvitacionService correoService = new CorreoInvitacionService(resend);
        String enlace = "https://aprendizaje-usco.tech/activar-cuenta?token=token-seguro";
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);

        correoService.enviarConfiguracionCuenta("docente@usco.edu.co", "Diego", enlace, 168);

        verify(resend).enviar(eq("docente@usco.edu.co"),
                eq("Configura tu contraseña de Aprendizaje USCO"), texto.capture());
        assertThat(texto.getValue())
                .contains("Hola Diego")
                .contains("válido por 168 horas y de un solo uso")
                .contains(enlace);
    }
}
