package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.RecordatorioRequest;
import mx.edu.unpa.actividadesapi.service.RecordatorioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * US-07: Endpoint para envío de recordatorios por correo.
 */
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class RecordatorioController {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioController.class);
    private final RecordatorioService recordatorioService;

    @PostMapping(
            value = "/{idActividad}/recordatorio",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter enviarRecordatorio(
            @PathVariable Integer idActividad,
            @RequestBody @Valid RecordatorioRequest request) {

        log.info("POST /api/actividades/{}/recordatorio usuario={}", idActividad, request.getIdUsuario());
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        recordatorioService.enviarRecordatorios(idActividad, request.getIdUsuario(), emitter);
        return emitter;
    }
}