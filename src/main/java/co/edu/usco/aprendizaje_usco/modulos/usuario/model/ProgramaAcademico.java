package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Programas académicos a los que está dirigida la plataforma (lista cerrada).
 *
 * En la base se guarda el NOMBRE (columna usuario.programa_academico), no el
 * nombre del enum. Debe coincidir con PROGRAMAS_ACADEMICOS del frontend
 * (src/features/auth/programas.js).
 */
public enum ProgramaAcademico {

    INGENIERIA_AGRICOLA("Ingeniería Agrícola"),
    INGENIERIA_AGROINDUSTRIAL("Ingeniería Agroindustrial"),
    INGENIERIA_CIVIL("Ingeniería Civil"),
    INGENIERIA_DE_PETROLEOS("Ingeniería de Petróleos"),
    INGENIERIA_DE_SOFTWARE("Ingeniería de Software"),
    INGENIERIA_ELECTRONICA("Ingeniería Electrónica"),
    LICENCIATURA_EN_MATEMATICAS("Licenciatura en Matemáticas");

    private final String nombre;

    ProgramaAcademico(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /** Busca por nombre sin importar mayúsculas ni espacios extremos: " ingeniería civil " → INGENIERIA_CIVIL. */
    public static Optional<ProgramaAcademico> desdeNombre(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String buscado = texto.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(p -> p.nombre.toLowerCase(Locale.ROOT).equals(buscado))
                .findFirst();
    }

    public static List<String> nombres() {
        return Arrays.stream(values()).map(ProgramaAcademico::getNombre).toList();
    }

}
