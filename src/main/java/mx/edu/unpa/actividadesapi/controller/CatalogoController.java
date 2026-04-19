package mx.edu.unpa.actividadesapi.controller;

import mx.edu.unpa.actividadesapi.dto.response.catalogo.*;
import mx.edu.unpa.actividadesapi.service.CatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private static final Logger log = LoggerFactory.getLogger(CatalogoController.class);
    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponse>> getCategorias() {
        log.info("GET /api/categorias");
        return ResponseEntity.ok(catalogoService.getCategorias());
    }

    // Filtra tipos por categoría
    @GetMapping("/tipos-actividad")
    public ResponseEntity<List<TipoActividadResponse>> getTipos(
            @RequestParam(required = false) Integer categoriaId) {
        log.info("GET /api/tipos-actividad categoriaId={}", categoriaId);
        return ResponseEntity.ok(catalogoService.getTiposActividad(categoriaId));
    }

    @GetMapping("/carreras")
    public ResponseEntity<List<CarreraResponse>> getCarreras() {
        log.info("GET /api/carreras");
        return ResponseEntity.ok(catalogoService.getCarreras());
    }

    @GetMapping("/departamentos")
    public ResponseEntity<List<DepartamentoResponse>> getDepartamentos() {
        log.info("GET /api/departamentos");
        return ResponseEntity.ok(catalogoService.getDepartamentos());
    }

    // Reemplaza el GET /recursos por estos dos
    @GetMapping("/recursos/espacios")
    public ResponseEntity<List<EspacioResponse>> getEspacios() {
        log.info("GET /api/recursos/espacios");
        return ResponseEntity.ok(catalogoService.getEspacios());
    }

    @GetMapping("/recursos/mobiliario")
    public ResponseEntity<List<MobiliarioResponse>> getMobiliario() {
        log.info("GET /api/recursos/mobiliario");
        return ResponseEntity.ok(catalogoService.getMobiliario());
    }
}