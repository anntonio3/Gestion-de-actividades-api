package mx.edu.unpa.actividadesapi.controller;

import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.dto.response.ActividadDetallePublicoResponse;
import mx.edu.unpa.actividadesapi.dto.response.EventoDestacadoResponse;
import mx.edu.unpa.actividadesapi.model.Categoria;
import mx.edu.unpa.actividadesapi.service.CalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendario")
@CrossOrigin(origins = "http://localhost:4200")
public class CalendarioController {

    @Autowired private CalendarioService service;

    // Actividades aprobadas, filtradas por categoría opcional
    @GetMapping("/publico")
    public ResponseEntity<List<ActividadPublicaDTO>> getPublico(
            @RequestParam(required = false) Integer categoria) {
        return ResponseEntity.ok(service.getActividadesPublicas(categoria));
    }

    // Lista de categorías para los chips del frontend
    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> getCategorias() {
        return ResponseEntity.ok(service.getCategorias());
    }

    // Detalle completo de una actividad pública (modal de detalle)
    @GetMapping("/publico/{id}")
    public ResponseEntity<ActividadDetallePublicoResponse> getDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getDetalleActividad(id));
    }

    // US-27: Evento destacado activo para el banner publico.
    // 204 si no hay ninguno (el front no muestra el banner).
    @GetMapping("/destacado")
    public ResponseEntity<EventoDestacadoResponse> getDestacado() {
        EventoDestacadoResponse destacado = service.getEventoDestacado();
        return destacado != null
                ? ResponseEntity.ok(destacado)
                : ResponseEntity.noContent().build();
    }
}
