package mx.edu.unpa.actividadesapi.controller;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.SolicitudResponseDTO;
import mx.edu.unpa.actividadesapi.service.ActividadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ActividadController {
    // Clase antes llamada ActividadController

    private final ActividadService actividadService;

    // US-04: Como profesor, quiero consultar el estado de mis solicitudes
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<SolicitudResponseDTO>> getMisSolicitudes(
            @RequestParam Integer idProfesor,
            @RequestParam(required = false) String estado) {

        List<SolicitudResponseDTO> solicitudes = actividadService.getMisSolicitudes(idProfesor, estado);
        return ResponseEntity.ok(solicitudes);
    }
}