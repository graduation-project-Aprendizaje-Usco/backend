package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.common.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

class CorreoInvitacionServiceTest {

    @Test
    void requiereSmtpParaEnviarElEnlaceDeConfiguracionDeCuenta() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(null);
        CorreoInvitacionService correoService = new CorreoInvitacionService(
                proveedor, "no-reply@aprendizaje-usco.test", "", "", "");

        assertThatThrownBy(() -> correoService.enviarConfiguracionCuenta(
                "persona@usco.edu.co", "Persona", "https://frontend.test/activar-cuenta?token=secreto", 168))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("No se creó la cuenta");
    }
}
