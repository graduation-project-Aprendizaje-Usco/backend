package co.edu.usco.aprendizaje_usco.modulos.contenido.service;

import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.AdminActividadesResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.ContenidoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas de solo lectura para las vistas de administración de actividades (SCRUM-46). */
@Service
@Transactional(readOnly = true)
public class AdminActividadesService {

    private final ContenidoRepository contenidoRepository;
    private final EjercicioRepository ejercicioRepository;

    public AdminActividadesService(ContenidoRepository contenidoRepository,
                                   EjercicioRepository ejercicioRepository) {
        this.contenidoRepository = contenidoRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    public AdminActividadesResponse listar() {
        List<AdminActividadesResponse.ContenidoAdmin> contenidos = contenidoRepository.findAllParaAdmin().stream()
                .map(contenido -> contenidoAdmin(contenido))
                .toList();
        List<AdminActividadesResponse.EjercicioAdmin> ejercicios = ejercicioRepository.findAllParaAdmin().stream()
                .map(ejercicio -> ejercicioAdmin(ejercicio))
                .toList();
        return new AdminActividadesResponse(contenidos, ejercicios);
    }

    private AdminActividadesResponse.ContenidoAdmin contenidoAdmin(Contenido contenido) {
        return new AdminActividadesResponse.ContenidoAdmin(
                contenido.getId(),
                contenido.getTitulo(),
                contenido.getTema(),
                contenido.getAsignatura(),
                contenido.getCompetencia().getCodigo(),
                contenido.getCompetencia().getNombre(),
                contenido.isActivo(),
                ejercicioRepository.countByContenidoId(contenido.getId()),
                correo(contenido.getCreadoPor()));
    }

    private static AdminActividadesResponse.EjercicioAdmin ejercicioAdmin(Ejercicio ejercicio) {
        return new AdminActividadesResponse.EjercicioAdmin(
                ejercicio.getId(),
                ejercicio.getCodigo(),
                ejercicio.getContenido().getTitulo(),
                ejercicio.getCompetencia().getCodigo(),
                ejercicio.getNivel().getValor(),
                ejercicio.getTipo(),
                ejercicio.isActivo(),
                correo(ejercicio.getCreadoPor()));
    }

    /** Los datos semilla no tienen creador; el frontend los muestra como "Semilla". */
    private static String correo(Usuario usuario) {
        return usuario == null ? null : usuario.getCorreo();
    }
}
