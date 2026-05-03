package mx.edu.unpa.actividadesapi.controller;

import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
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

    // NUEVO: lista de categorías para los chips del frontend
    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> getCategorias() {
        return ResponseEntity.ok(service.getCategorias());
    }
}
