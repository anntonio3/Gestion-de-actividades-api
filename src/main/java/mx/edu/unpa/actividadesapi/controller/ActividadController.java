package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import mx.edu.unpa.actividadesapi.dto.request.ActividadRequest;
import mx.edu.unpa.actividadesapi.dto.response.ActividadResponse;
import mx.edu.unpa.actividadesapi.service.ActividadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private static final Logger log = LoggerFactory.getLogger(ActividadController.class);

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    /**
     * US-03: Registrar nueva actividad con recursos
     * POST /api/actividades
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ActividadResponse> registrar(
            @RequestPart("datos") @Valid ActividadRequest request,
            @RequestPart(value = "portada", required = false) MultipartFile portada) {

        log.info("POST /api/actividades - profesor id={}", request.getIdProfesor());
        ActividadResponse response = actividadService.registrarActividad(request, portada);
        log.info("Actividad registrada exitosamente id={}", response.getIdActividad());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
