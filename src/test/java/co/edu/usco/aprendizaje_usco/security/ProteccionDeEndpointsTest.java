package co.edu.usco.aprendizaje_usco.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hallazgo 7 de la revisión: la regla general de SecurityConfig es solo
 * "autenticado", así que un endpoint nuevo SIN @PreAuthorize quedaría abierto a
 * cualquier rol. Esta prueba falla si algún controlador tiene un endpoint sin
 * @PreAuthorize (en el método o en la clase) que no esté en la lista de abajo.
 *
 * Si tu endpoint es público o para cualquier usuario con sesión A PROPÓSITO,
 * agrégalo a EXCEPCIONES con el motivo, y que lo vea el revisor del PR.
 */
class ProteccionDeEndpointsTest {

    private static final String PAQUETE_BASE = "co.edu.usco.aprendizaje_usco";

    /** "Clase#método" que no llevan @PreAuthorize a propósito. */
    private static final Set<String> EXCEPCIONES = Set.of(
            "AuthController#login",      // público (SecurityConfig: permitAll)
            "AuthController#registrar",  // público (SecurityConfig: permitAll)
            "AuthController#me"          // cualquier usuario con sesión: devuelve sus propios datos
    );

    @Test
    void todoEndpointTienePreAuthorizeOEstaEnLasExcepciones() throws Exception {
        List<String> sinProteccion = new ArrayList<>();

        for (Class<?> controlador : controladores()) {
            boolean claseProtegida = AnnotatedElementUtils.hasAnnotation(controlador, PreAuthorize.class);
            for (Method metodo : controlador.getDeclaredMethods()) {
                if (!AnnotatedElementUtils.hasAnnotation(metodo, RequestMapping.class)) {
                    continue; // no es un endpoint
                }
                String nombre = controlador.getSimpleName() + "#" + metodo.getName();
                boolean protegido = claseProtegida || AnnotatedElementUtils.hasAnnotation(metodo, PreAuthorize.class);
                if (!protegido && !EXCEPCIONES.contains(nombre)) {
                    sinProteccion.add(nombre);
                }
            }
        }

        assertThat(sinProteccion)
                .as("Endpoints sin @PreAuthorize: agrega la anotación con el rol que corresponde, "
                        + "o inclúyelos en EXCEPCIONES si son públicos a propósito")
                .isEmpty();
    }

    @Test
    void lasExcepcionesSiguenExistiendo() throws Exception {
        // Evita que la lista se llene de nombres viejos que ya no existen.
        List<String> existentes = new ArrayList<>();
        for (Class<?> controlador : controladores()) {
            for (Method metodo : controlador.getDeclaredMethods()) {
                existentes.add(controlador.getSimpleName() + "#" + metodo.getName());
            }
        }
        assertThat(existentes).containsAll(EXCEPCIONES);
    }

    /** @RestController del código de la aplicación (sin los controladores internos de las pruebas). */
    private static List<Class<?>> controladores() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider escaner = new ClassPathScanningCandidateComponentProvider(false);
        escaner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<Class<?>> clases = new ArrayList<>();
        for (BeanDefinition definicion : escaner.findCandidateComponents(PAQUETE_BASE)) {
            Class<?> clase = ClassUtils.forName(definicion.getBeanClassName(), ProteccionDeEndpointsTest.class.getClassLoader());
            if (!clase.isMemberClass()) {
                clases.add(clase);
            }
        }
        assertThat(clases).as("no se encontró ningún controlador").isNotEmpty();
        return clases;
    }

}
