package co.edu.usco.aprendizaje_usco.modulos.contenido.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NivelDificultadConverter implements AttributeConverter<NivelDificultad, Short> {

    @Override
    public Short convertToDatabaseColumn(NivelDificultad nivel) {
        return nivel == null ? null : (short) nivel.getValor();
    }

    @Override
    public NivelDificultad convertToEntityAttribute(Short valor) {
        return valor == null ? null : NivelDificultad.desde(valor);
    }

}
