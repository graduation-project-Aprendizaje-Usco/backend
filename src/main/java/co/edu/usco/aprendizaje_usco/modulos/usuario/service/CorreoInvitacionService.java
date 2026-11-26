package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import org.springframework.stereotype.Service;

/** Construye los mensajes de invitación y activación; Resend realiza el envío HTTPS. */
@Service
public class CorreoInvitacionService {

    private final ResendEmailService resendEmailService;

    public CorreoInvitacionService(ResendEmailService resendEmailService) {
        this.resendEmailService = resendEmailService;
    }

    public void enviar(String correo, String enlace) {
        String texto = """
                Hola,

                Un docente te invitó a unirte a su grupo en Aprendizaje USCO.
                Crea tu cuenta con este enlace (válido por 7 días):

                %s

                Si no esperabas esta invitación, ignora este mensaje.
                """.formatted(enlace);
        resendEmailService.enviar(correo, "Invitación a Aprendizaje USCO", texto);
    }

    public void enviarConfiguracionCuenta(String correo, String nombres, String enlace, long vigenciaHoras) {
        String texto = """
                Hola %s,

                El administrador creó tu cuenta en Aprendizaje USCO.
                Usa este enlace para definir tu contraseña (válido por %d horas y de un solo uso):

                %s

                Si no esperabas este mensaje, puedes ignorarlo.
                """.formatted(nombres, vigenciaHoras, enlace);
        resendEmailService.enviar(correo, "Configura tu contraseña de Aprendizaje USCO", texto);
    }
}
