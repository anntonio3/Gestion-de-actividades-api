package mx.edu.unpa.actividadesapi.controller;

import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.service.CalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendario")
@CrossOrigin(origins = "http://localhost:4200")
public class CalendarioController {

    @Autowired
    private CalendarioService service;

    @GetMapping("/publico")
    public ResponseEntity<List<ActividadPublicaDTO>> getPublico(
            @RequestParam(required = false) Integer tipo) {

        return ResponseEntity.ok(service.getActividadesPublicas(tipo));
    }
}
