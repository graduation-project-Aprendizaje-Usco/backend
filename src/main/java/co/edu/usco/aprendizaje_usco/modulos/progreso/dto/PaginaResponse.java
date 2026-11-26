package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

import java.util.List;

/** Una página de resultados. pagina empieza en 0. */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> PaginaResponse<T> de(List<T> contenido, int pagina, int tamano, long totalElementos) {
        return new PaginaResponse<>(contenido, pagina, tamano, totalElementos,
                (int) Math.ceil((double) totalElementos / tamano));
    }

}
