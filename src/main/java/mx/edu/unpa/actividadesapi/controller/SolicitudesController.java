package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.ActualizarActividadRequestDTO;
import mx.edu.unpa.actividadesapi.dto.response.SolicitudResponseDTO;
import mx.edu.unpa.actividadesapi.service.SolicitudesService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SolicitudesController {

    private final SolicitudesService actividadService;

    // US-04: Consultar mis solicitudes
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<SolicitudResponseDTO>> getMisSolicitudes(
            @RequestParam Integer idProfesor,
            @RequestParam(required = false) String estado) {

        return ResponseEntity.ok(actividadService.getMisSolicitudes(idProfesor, estado));
    }

    // US-05: Editar datos de una actividad PENDIENTE
    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> editarActividad(
            @PathVariable Integer id,
            @RequestParam Integer idProfesor,
            @Valid @RequestBody ActualizarActividadRequestDTO dto) {

        return ResponseEntity.ok(actividadService.editarActividad(id, idProfesor, dto));
    }

    // Reemplazar (o agregar) la imagen de portada — solo UNA imagen permitida.
    // Si ya existía una imagen se elimina antes de guardar la nueva.
    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolicitudResponseDTO> reemplazarImagen(
            @PathVariable Integer id,
            @RequestParam Integer idProfesor,
            @RequestPart("imagen") MultipartFile imagen) {

        return ResponseEntity.ok(actividadService.reemplazarImagen(id, idProfesor, imagen));
    }

    // Eliminar la imagen de portada sin reemplazarla
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<SolicitudResponseDTO> eliminarImagen(
            @PathVariable Integer id,
            @RequestParam Integer idProfesor) {

        return ResponseEntity.ok(actividadService.eliminarImagen(id, idProfesor));
    }
}