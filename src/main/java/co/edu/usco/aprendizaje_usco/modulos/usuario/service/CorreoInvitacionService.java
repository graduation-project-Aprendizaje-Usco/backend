package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía invitaciones de grupo (con enlace en log si falta SMTP) y enlaces de
 * configuración de cuenta (SMTP obligatorio; nunca deja el token en logs).
 */
@Service
public class CorreoInvitacionService {

    private static final Logger log = LoggerFactory.getLogger(CorreoInvitacionService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remitente;
    private final String mailHost;
    private final String mailUsername;
    private final String mailPassword;

    public CorreoInvitacionService(ObjectProvider<JavaMailSender> mailSender,
                                   @Value("${app.invitaciones.remitente:no-reply@aprendizaje-usco.test}")
                                   String remitente,
                                   @Value("${MAIL_HOST:}") String mailHost,
                                   @Value("${MAIL_USERNAME:}") String mailUsername,
                                   @Value("${MAIL_PASSWORD:}") String mailPassword) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.mailHost = mailHost;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    public void enviar(String correo, String enlace) {
        JavaMailSender sender = mailSender.getIfAvailable();
        boolean smtpListo = mailHost != null && !mailHost.isBlank()
                && mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank()
                && sender != null;
        if (!smtpListo) {
            log.info("Invitación para {}: {}", correo, enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correo);
        mensaje.setSubject("Invitación a Aprendizaje USCO");
        mensaje.setText("""
                Hola,

                Un docente te invitó a unirte a su grupo en Aprendizaje USCO.
                Crea tu cuenta con este enlace (válido por 7 días):

                %s

                Si no esperabas esta invitación, ignora este mensaje.
                """.formatted(enlace));
        sender.send(mensaje);
    }

    public void enviarConfiguracionCuenta(String correo, String nombres, String enlace, long vigenciaHoras) {
        JavaMailSender sender = mailSender.getIfAvailable();
        boolean smtpListo = mailHost != null && !mailHost.isBlank()
                && mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank()
                && sender != null;
        if (!smtpListo) {
            throw new ServicioNoDisponibleException(
                    "El correo no está configurado. No se creó la cuenta; configura SMTP e inténtalo de nuevo.");
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correo);
        mensaje.setSubject("Configura tu contraseña de Aprendizaje USCO");
        mensaje.setText("""
                Hola %s,

                El administrador creó tu cuenta en Aprendizaje USCO.
                Usa este enlace para definir tu contraseña (válido por %d horas y de un solo uso):

                %s

                Si no esperabas este mensaje, puedes ignorarlo.
                """.formatted(nombres, vigenciaHoras, enlace));
        sender.send(mensaje);
    }
}
