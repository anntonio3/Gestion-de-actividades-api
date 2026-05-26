package mx.edu.unpa.actividadesapi.controller;


import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaCarreraDTO;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaCampusDTO;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaMesDTO;
import mx.edu.unpa.actividadesapi.service.EstadisticasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de estadísticas para la vista admin/gráfica.
 *
 * US-21 → GET /api/admin/estadisticas/general
 * US-22 → GET /api/admin/estadisticas/por-campus
 * US-23 → GET /api/admin/estadisticas/por-carrera
 */
@RestController
@RequestMapping("/api/admin/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private static final Logger log = LoggerFactory.getLogger(EstadisticasController.class);

    private final EstadisticasService estadisticasService;

    /**
     * US-21: Gráfica general — cantidad de actividades por mes y año.
     * GET /api/admin/estadisticas/general
     */
    @GetMapping("/general")
    public ResponseEntity<List<EstadisticaMesDTO>> obtenerGeneral() {
        log.info("GET /api/admin/estadisticas/general");
        return ResponseEntity.ok(estadisticasService.obtenerGeneral());
    }

    /**
     * US-22: Gráfica por campus (departamento) — cantidad de actividades por mes, año y campus.
     * GET /api/admin/estadisticas/por-campus
     */
    @GetMapping("/por-campus")
    public ResponseEntity<List<EstadisticaCampusDTO>> obtenerPorCampus() {
        log.info("GET /api/admin/estadisticas/por-campus");
        return ResponseEntity.ok(estadisticasService.obtenerPorCampus());
    }

    /**
     * US-23: Gráfica por carrera — cantidad de actividades por mes, año y carrera.
     * GET /api/admin/estadisticas/por-carrera
     */
    @GetMapping("/por-carrera")
    public ResponseEntity<List<EstadisticaCarreraDTO>> obtenerPorCarrera() {
        log.info("GET /api/admin/estadisticas/por-carrera");
        return ResponseEntity.ok(estadisticasService.obtenerPorCarrera());
    }
}
