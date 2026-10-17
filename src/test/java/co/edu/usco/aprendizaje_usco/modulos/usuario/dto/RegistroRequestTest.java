package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Reglas del registro (hallazgo 6 de la revisión): correo de estudiante,
 * contraseña con letra y número y programa de la lista cerrada.
 * Usa el validador de Bean Validation directamente: no levanta Spring.
 */
class RegistroRequestTest {

    private static final String CORREO = "u20201234567@usco.edu.co";
    private static final String CLAVE = "Clave2026*";
    private static final String PROGRAMA = "Ingeniería de Software";

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void crearValidador() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        fabrica.close();
    }

    @Test
    void registroCompletoYValidoNoTieneErrores() {
        assertThat(validar(CORREO, CLAVE, PROGRAMA)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "u20201234567@usco.edu.co",
            "U20201234567@USCO.EDU.CO",      // mayúsculas: se normaliza al guardar
            "  u20201234567@usco.edu.co  "   // espacios al copiar y pegar
    })
    void aceptaCorreosDeEstudiante(String correo) {
        assertThat(campos(validar(correo, CLAVE, PROGRAMA))).doesNotContain("correo");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ana.perez@usco.edu.co",
            "u2020123456@usco.edu.co",
            "u202012345678@usco.edu.co",
            "u20201234567@gmail.com",
            "u20201234567@usco.edu.co.co",
            "20201234567@usco.edu.co",
            "u2020123456a@usco.edu.co"
    })
    void rechazaCorreosQueNoSonDeEstudiante(String correo) {
        assertThat(mensajes(validar(correo, CLAVE, PROGRAMA)))
                .contains(RegistroRequest.MENSAJE_CORREO_ESTUDIANTE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SoloLetras", "1234567890", "Corta1", "ñandú2026"})
    void validaLaContrasena(String password) {
        boolean esValida = password.length() >= 8
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
        assertThat(campos(validar(CORREO, password, PROGRAMA)).contains("password")).isEqualTo(!esValida);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ingeniería Civil", "  ingeniería de petróleos ", "LICENCIATURA EN MATEMÁTICAS"})
    void aceptaProgramasDeLaLista(String programa) {
        assertThat(campos(validar(CORREO, CLAVE, programa))).doesNotContain("programaAcademico");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Medicina", "Ingenieria de Software"})
    void rechazaProgramasVaciosOFueraDeLaLista(String programa) {
        assertThat(campos(validar(CORREO, CLAVE, programa))).contains("programaAcademico");
    }

    private static Set<ConstraintViolation<RegistroRequest>> validar(String correo, String password, String programa) {
        return validador.validate(new RegistroRequest("Ana", "Pérez", correo, password, programa));
    }

    private static Set<String> campos(Set<ConstraintViolation<RegistroRequest>> errores) {
        return errores.stream().map(e -> e.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }

    private static Set<String> mensajes(Set<ConstraintViolation<RegistroRequest>> errores) {
        return errores.stream().map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }

}
