package co.edu.usco.aprendizaje_usco.common;


import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.common.error.GlobalExceptionHandler;
import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verifica los criterios de aceptación del manejador global de errores
 * usando un controlador de prueba (solo existe en los tests).
 * MockMvc "standalone": no levanta Spring ni la base de datos, corre en milisegundos.
 */
public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void dtoInvalidoDevuelve400ConLosCamposYMensajes() throws Exception {
        mockMvc.perform(post("/prueba/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"correo\":\"no-es-un-correo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/prueba/usuarios"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errores[*].campo", containsInAnyOrder("nombre", "correo")))
                .andExpect(jsonPath("$.errores[0].mensaje").isNotEmpty());
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/prueba/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud no es un JSON válido"));
    }

    @Test
    void idInexistenteDevuelve404ConFormatoEstandar() throws Exception {
        mockMvc.perform(get("/prueba/ejercicios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ejercicio con id 99 no encontrado"))
                .andExpect(jsonPath("$.path").value("/prueba/ejercicios/99"))
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    void conflictoPropioDevuelve409() throws Exception {
        mockMvc.perform(get("/prueba/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El correo ya está registrado"));
    }

    @Test
    void violacionDeIntegridadDevuelve409SinDetalleSql() throws Exception {
        mockMvc.perform(get("/prueba/integridad"))
                .andExpect(status().isConflict())
                .andExpect(content().string(not(containsString("usuario_correo_key"))));
    }

    @Test
    void reglaDeNegocioDevuelve422() throws Exception {
        mockMvc.perform(get("/prueba/regla"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("El tiempo de la prueba ya terminó"));
    }

    @Test
    void errorInesperadoDevuelve500SinTraza() throws Exception {
        mockMvc.perform(get("/prueba/fallo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrió un error inesperado. Intenta de nuevo más tarde."))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("detalle interno secreto"))));
    }

    // ------------------------------------------------------------------ apoyo

    record UsuarioDePrueba(@NotBlank String nombre, @NotBlank @Email String correo) {
    }

    @RestController
    static class ControladorDePrueba {

        @PostMapping("/prueba/usuarios")
        UsuarioDePrueba crear(@Valid @RequestBody UsuarioDePrueba dto) {
            return dto;
        }

        @GetMapping("/prueba/ejercicios/{id}")
        String buscar(@PathVariable Long id) {
            throw new RecursoNoEncontradoException("Ejercicio", id);
        }

        @GetMapping("/prueba/conflicto")
        String conflicto() {
            throw new ConflictoException("El correo ya está registrado");
        }

        @GetMapping("/prueba/integridad")
        String integridad() {
            throw new DataIntegrityViolationException(
                    "duplicate key value violates unique constraint \"usuario_correo_key\"");
        }

        @GetMapping("/prueba/regla")
        String regla() {
            throw new ReglaNegocioException("El tiempo de la prueba ya terminó");
        }

        @GetMapping("/prueba/fallo")
        String fallo() {
            throw new IllegalStateException("detalle interno secreto");
        }
    }

}
