package co.edu.usco.aprendizaje_usco.modulos.contenido.model;

/**
 * Dificultad de un ejercicio y nivel actual de practica de un estudiante.
 * Se persiste como SMALLINT (1, 2, 3), no como ordinal.
 */
public enum NivelDificultad {

    BASICO(1),
    INTERMEDIO(2),
    AVANZADO(3);

    private final int valor;

    NivelDificultad(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    public NivelDificultad siguiente() {
        return this == AVANZADO ? this : values()[ordinal() + 1];
    }

    public NivelDificultad anterior() {
        return this == BASICO ? this : values()[ordinal() - 1];
    }

    public static NivelDificultad desde(int valor) {
        for (NivelDificultad nivel : values()) {
            if (nivel.valor == valor) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Nivel de dificultad invalido: " + valor);
    }

}
