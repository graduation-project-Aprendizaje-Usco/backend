package co.edu.usco.aprendizaje_usco.modulos.usuario.validacion;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Valida {@link ProgramaAcademicoValido} contra la lista cerrada de ProgramaAcademico. */
public class ProgramaAcademicoValidator implements ConstraintValidator<ProgramaAcademicoValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        return valor == null || ProgramaAcademico.desdeNombre(valor).isPresent();
    }

}
