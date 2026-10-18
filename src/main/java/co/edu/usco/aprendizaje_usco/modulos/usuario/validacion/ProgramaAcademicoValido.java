package co.edu.usco.aprendizaje_usco.modulos.usuario.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * El texto debe ser uno de los programas de {@link co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico}.
 * Un valor nulo se considera válido: para exigirlo, combinar con @NotBlank.
 */
@Documented
@Constraint(validatedBy = ProgramaAcademicoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProgramaAcademicoValido {

    String message() default "Elige un programa académico de la lista";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
