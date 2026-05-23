package mx.edu.unpa.actividadesapi.controller;

import mx.edu.unpa.actividadesapi.dto.response.AvisoResponse;
import mx.edu.unpa.actividadesapi.service.AvisoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * US-18: Endpoint publico para que cualquier usuario vea el corcho sin login.
 * US-20: Tambien lo consume UNPA Grades; el CORS se abre a '*' en WebConfig
 *        SOLO para esta ruta, manteniendo cerrados los demas endpoints.
 */
@RestController
@RequestMapping("/api/corcho")
public class CorchoController {

    private static final Logger log = LoggerFactory.getLogger(CorchoController.class);

    @Autowired
    private AvisoService avisoService;

    /**
     * US-18/US-20: Lista publica de avisos.
     * Filtros opcionales:
     *   ?fecha=YYYY-MM-DD                       -> solo de ese dia
     *   ?desde=YYYY-MM-DD&hasta=YYYY-MM-DD      -> rango
     *   sin parametros                          -> todos los activos, proximos primero
     */
    @GetMapping
    public ResponseEntity<List<AvisoResponse>> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        log.info("GET /api/corcho fecha={} desde={} hasta={}", fecha, desde, hasta);
        return ResponseEntity.ok(avisoService.listarPublico(fecha, desde, hasta));
    }

    /**
     * US-18: Detalle individual del aviso (para abrir un modal o pagina completa).
     */
    @GetMapping("/{id}")
    public ResponseEntity<AvisoResponse> obtener(@PathVariable Integer id) {
        log.info("GET /api/corcho/{}", id);
        return ResponseEntity.ok(avisoService.obtenerPorId(id));
    }
}
