package co.edu.usco.aprendizaje_usco.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class MezcladorOpciones {

    private static final long MULTIPLICADOR = 1_000_003L;

    private MezcladorOpciones() {
    }

    public static <T> List<T> mezclar(List<T> elementos, long... semillas) {
        List<T> copia = new ArrayList<>(elementos);
        long semilla = 0;
        for (long parte : semillas) {
            semilla = semilla * MULTIPLICADOR + parte;
        }
        Collections.shuffle(copia, new Random(semilla));
        return copia;
    }

    public static String letra(int posicion) {
        return String.valueOf((char) ('A' + posicion));
    }
}
