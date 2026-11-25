package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.validacion.ProgramaAcademicoValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/registro. Solo registra estudiantes: el rol no se
 * recibe, lo asigna el servidor.
 *
 * Reglas (decididas por el equipo):
 *  - correo institucional de estudiante: "u" + 11 dígitos + "@usco.edu.co";
 *  - contraseña de 8 a 72 caracteres con al menos una letra y un número;
 *  - programa académico obligatorio y de la lista cerrada ProgramaAcademico.
 * Las mismas reglas están en el formulario del frontend (RegistroPage); el
 * backend es la fuente de verdad.
 */
public record RegistroRequest(

        @Schema(example = "Ana María")
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres pueden tener máximo 100 caracteres")
        String nombres,

        @Schema(example = "Pérez Gómez")
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos pueden tener máximo 100 caracteres")
        String apellidos,

        @Schema(example = "u20201234567@usco.edu.co")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Pattern(regexp = PATRON_CORREO_ESTUDIANTE, flags = Pattern.Flag.CASE_INSENSITIVE,
                message = MENSAJE_CORREO_ESTUDIANTE)
        @Size(max = 150)
        String correo,

        // Máximo 72: BCrypt ignora lo que pase de 72 bytes.
        @Schema(example = "MiClave2026*")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = PATRON_PASSWORD, message = MENSAJE_PASSWORD)
        String password,

        @Schema(example = "Ingeniería de Software")
        @NotBlank(message = "El programa académico es obligatorio")
        @ProgramaAcademicoValido
        String programaAcademico,

        @Schema(description = "Token del enlace de invitación")
        String tokenInvitacion) {

    /** Compatibilidad con el registro anterior; el token es obligatorio en producción. */
    public RegistroRequest(String nombres, String apellidos, String correo, String password,
                           String programaAcademico) {
        this(nombres, apellidos, correo, password, programaAcademico, null);
    }

    /** "u" + 11 dígitos + "@usco.edu.co" (sin importar mayúsculas: el correo se guarda en minúsculas). */
    public static final String PATRON_CORREO_ESTUDIANTE = "^u\\d{11}@usco\\.edu\\.co$";
    public static final String MENSAJE_CORREO_ESTUDIANTE =
            "Usa tu correo institucional de estudiante: u + 11 dígitos + @usco.edu.co";

    /** Al menos una letra (con tildes o ñ) y al menos un dígito. */
    public static final String PATRON_PASSWORD = "^(?=.*\\p{L})(?=.*\\d).*$";
    public static final String MENSAJE_PASSWORD = "La contraseña debe tener al menos una letra y un número";

    /** Quita espacios del correo antes de validarlo (ej. al copiar y pegar). */
    public RegistroRequest {
        correo = correo == null ? null : correo.trim();
    }

    /** Sin la contraseña a propósito: el toString de un record termina en logs. */
    @Override
    public String toString() {
        return "RegistroRequest{correo=" + correo + "}";
    }

}
