package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.response.catalogo.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.CatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoServiceImpl.class);

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private TipoActividadRepository tipoActividadRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private CarreraRepository carreraRepository;
    @Autowired
    private RecursoRepository recursoRepository;
    @Autowired
    private RecursoEspacioRepository recursoEspacioRepository;
    @Autowired
    private RecursoMobiliarioRepository recursoMobiliarioRepository;

    @Override
    public List<CategoriaResponse> getCategorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaResponse(c.getIdCategoria(), c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Override
    public List<TipoActividadResponse> getTiposActividad(Integer categoriaId) {
        var tipos = (categoriaId != null)
                ? tipoActividadRepository.findByCategoriaIdCategoria(categoriaId)
                : tipoActividadRepository.findAll();

        return tipos.stream()
                .map(t -> new TipoActividadResponse(
                        t.getIdTipo(),
                        t.getNombre(),
                        t.getCategoria().getIdCategoria(),
                        t.getCategoria().getNombre()))
                .toList();
    }

    @Override
    public List<DepartamentoResponse> getDepartamentos() {
        return departamentoRepository.findAll().stream()
                .map(d -> new DepartamentoResponse(d.getIdDepartamento(), d.getNombre()))
                .toList();
    }

    @Override
    public List<CarreraResponse> getCarreras() {
        return carreraRepository.findAll().stream()
                .map(c -> new CarreraResponse(c.getIdCarrera(), c.getNombre()))
                .toList();
    }

    @Override
    public List<EspacioResponse> getEspacios(LocalDate fecha,
                                             LocalTime horaInicio,
                                             LocalTime horaFin) {
        var todos = recursoEspacioRepository.findByActivoTrue();

        // Si no hay filtro de fecha/hora, todos disponibles
        if (fecha == null || horaInicio == null || horaFin == null) {
            return todos.stream()
                    .map(e -> new EspacioResponse(
                            e.getIdRecurso(), e.getNombre(), e.getDescripcion(),
                            e.getCapacidad(), e.getUbicacion(), true))
                    .toList();
        }

        var ocupados = recursoEspacioRepository
                .findIdsOcupados(fecha, horaInicio, horaFin);

        // Logs temporales
        log.info("=== DEBUG ESPACIOS ===");
        log.info("Filtro: fecha={}, inicio={}, fin={}", fecha, horaInicio, horaFin);
        log.info("IDs ocupados encontrados: {}", ocupados);
        log.info("======================");

        return todos.stream()
                .map(e -> {
                    boolean disponible = !ocupados.contains(e.getIdRecurso());
                    log.info("Recurso id={} nombre='{}' disponible={}",
                            e.getIdRecurso(), e.getNombre(), disponible);
                    return new EspacioResponse(
                            e.getIdRecurso(), e.getNombre(), e.getDescripcion(),
                            e.getCapacidad(), e.getUbicacion(), disponible);
                })
                .toList();
    }

    @Override
    public List<MobiliarioResponse> getMobiliario(LocalDate fecha,
                                                  LocalTime horaInicio,
                                                  LocalTime horaFin) {
        var todos = recursoMobiliarioRepository.findByActivoTrue();

        if (fecha == null || horaInicio == null || horaFin == null) {
            return todos.stream()
                    .map(m -> new MobiliarioResponse(
                            m.getIdRecurso(), m.getNombre(), m.getDescripcion(),
                            m.getCantidad(), m.getCantidad()))
                    .toList();
        }

        // Construir mapa idRecurso → cantidad ocupada
        var ocupadas = recursoMobiliarioRepository
                .findCantidadesOcupadas(fecha, horaInicio, horaFin);

        Map<Integer, Integer> mapa = new HashMap<>();
        for (Object[] row : ocupadas) {
            mapa.put((Integer) row[0], ((Number) row[1]).intValue());
        }

        return todos.stream()
                .map(m -> {
                    int ocupada = mapa.getOrDefault(m.getIdRecurso(), 0);
                    int disponible = Math.max(0, m.getCantidad() - ocupada);
                    return new MobiliarioResponse(
                            m.getIdRecurso(), m.getNombre(), m.getDescripcion(),
                            m.getCantidad(), disponible);
                })
                .toList();
    }

}
