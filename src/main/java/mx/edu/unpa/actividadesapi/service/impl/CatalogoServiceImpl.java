package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.response.catalogo.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.CatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
    public List<EspacioResponse> getEspacios() {
        return recursoEspacioRepository.findByActivoTrue().stream()
                .map(e -> new EspacioResponse(
                        e.getIdRecurso(),
                        e.getNombre(),
                        e.getDescripcion(),
                        e.getCapacidad(),
                        e.getUbicacion()))
                .toList();
    }

    @Override
    public List<MobiliarioResponse> getMobiliario() {
        return recursoMobiliarioRepository.findByActivoTrue().stream()
                .map(m -> new MobiliarioResponse(
                        m.getIdRecurso(),
                        m.getNombre(),
                        m.getDescripcion(),
                        m.getCantidad()))
                .toList();
    }

}
