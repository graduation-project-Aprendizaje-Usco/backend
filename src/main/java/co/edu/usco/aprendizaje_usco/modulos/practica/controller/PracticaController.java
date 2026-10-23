package co.edu.usco.aprendizaje_usco.modulos.practica.controller;

import co.edu.usco.aprendizaje_usco.modulos.practica.dto.EjercicioPracticaResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.IntentoPracticaRequest;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.ResultadoIntentoResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.service.AsignaturaValidator;
import co.edu.usco.aprendizaje_usco.modulos.practica.service.PracticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/practica")
@PreAuthorize("hasRole('ESTUDIANTE')")
@Tag(name = "Practica adaptativa", description = "Seleccion de ejercicios e intentos (solo estudiantes)")
public class PracticaController {

    private final PracticaService practicaService;
    private final AsignaturaValidator asignaturaValidator;

    public PracticaController(PracticaService practicaService, AsignaturaValidator asignaturaValidator) {
        this.practicaService = practicaService;
        this.asignaturaValidator = asignaturaValidator;
    }

    @GetMapping("/siguiente")
    @Operation(summary = "Siguiente ejercicio adaptativo de una asignatura")
    public EjercicioPracticaResponse siguiente(@RequestParam String asignatura,
                                               @AuthenticationPrincipal Jwt jwt) {
        String codigo = asignaturaValidator.validar(asignatura);
        return practicaService.siguiente(usuarioId(jwt), codigo);
    }

    @PostMapping("/intentos")
    @Operation(summary = "Registrar la respuesta a un ejercicio")
    public ResponseEntity<ResultadoIntentoResponse> responder(@Valid @RequestBody IntentoPracticaRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        PracticaService.Registro registro = practicaService.responder(usuarioId(jwt), request);
        return ResponseEntity.status(registro.creado() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(registro.respuesta());
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

}
