package mx.edu.unpa.actividadesapi.service.impl;

import jakarta.transaction.Transactional;
import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.dto.response.ActividadDetallePublicoResponse;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.CalendarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final Logger log = LoggerFactory.getLogger(CalendarioServiceImpl.class);

    @Autowired
    private ActividadRepository actividadRepo;
    @Autowired
    private CategoriaRepository categoriaRepo;
    @Autowired
    private ActividadRecursoRepository actividadRecursoRepo;
    @Autowired
    private ActividadOrganizadorRepository organizadorRepo;
    @Autowired
    private RecursoEspacioRepository recursoEspacioRepo;


    @Override
    @Transactional
    public List<ActividadPublicaDTO> getActividadesPublicas(Integer idCategoria) {
        List<Actividad> lista = (idCategoria != null)
                ? actividadRepo.findByEstadoAndTipo_Categoria_IdCategoria(
                EstadoActividad.APROBADA, idCategoria)
                : actividadRepo.findByEstado(EstadoActividad.APROBADA);

        return lista.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Categoria> getCategorias() {
        return categoriaRepo.findAll();
    }

    // ============================================================
    //  NUEVO: detalle completo de una actividad pública
    // ============================================================
    @Override
    @Transactional
    public ActividadDetallePublicoResponse getDetalleActividad(Integer idActividad) {
        log.info("Consultando detalle público de actividad id={}", idActividad);

        Actividad actividad = actividadRepo.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la actividad con id: " + idActividad));

        // Solo se puede ver el detalle de actividades aprobadas
        if (actividad.getEstado() != EstadoActividad.APROBADA) {
            log.warn("Intento de ver detalle de actividad no aprobada id={}", idActividad);
            throw new BusinessException("La actividad no está disponible públicamente");
        }

        ActividadDetallePublicoResponse dto = new ActividadDetallePublicoResponse();
        dto.setId(actividad.getIdActividad());
        dto.setNombre(actividad.getNombre());
        dto.setDescripcion(actividad.getDescripcion());
        dto.setFechaActividad(actividad.getFechaActividad());
        dto.setHoraInicio(actividad.getHoraInicio());
        dto.setHoraFin(actividad.getHoraFin());
        dto.setTipo(actividad.getTipo().getNombre());
        dto.setCampus(actividad.getCampus() != null ? actividad.getCampus().getNombre() : null);
        dto.setCategoria(actividad.getTipo().getCategoria().getNombre());

        // Imagen de portada
        if (actividad.getImagenes() != null) {
            actividad.getImagenes().stream()
                    .filter(ActividadImagen::getEsPortada)
                    .findFirst()
                    .ifPresent(img -> dto.setImagenPortada(img.getUrl()));
        }

        // Lugar (regla: una actividad tiene un solo espacio)
        dto.setLugar(buscarLugar(idActividad));

        // Organizadores
        dto.setOrganizadores(buscarOrganizadores(idActividad));

        return dto;
    }


    /**
     * Busca el espacio asociado a la actividad.
     * Filtra los recursos consultando RecursoEspacioRepository en lugar de
     * usar instanceof (que falla con proxies de Hibernate en herencia JOINED).
     */
    private ActividadDetallePublicoResponse.LugarPublicoResponse buscarLugar(Integer idActividad) {
        List<ActividadRecurso> recursos = actividadRecursoRepo
                .findByActividadIdActividad(idActividad);

        if (recursos.isEmpty()) {
            log.info("Actividad {} no tiene recursos", idActividad);
            return null;
        }

        // Sacar los IDs de los recursos asignados
        List<Integer> idsRecursos = recursos.stream()
                .map(ar -> ar.getRecurso().getIdRecurso())
                .toList();

        // Consultar cuáles de esos IDs son RecursoEspacio
        List<RecursoEspacio> espacios = recursoEspacioRepo.findAllById(idsRecursos);

        if (espacios.isEmpty()) {
            log.info("Actividad {} no tiene recursos de tipo ESPACIO", idActividad);
            return null;
        }

        if (espacios.size() > 1) {
            log.warn("Actividad {} tiene {} espacios. Mostrando el primero (regla de negocio: 1 espacio).",
                    idActividad, espacios.size());
        }

        RecursoEspacio espacio = espacios.get(0);

        ActividadDetallePublicoResponse.LugarPublicoResponse lugar = new ActividadDetallePublicoResponse.LugarPublicoResponse();
        lugar.setIdEspacio(espacio.getIdRecurso());
        lugar.setNombre(espacio.getNombre());
        lugar.setUbicacion(espacio.getUbicacion());
        lugar.setCapacidad(espacio.getCapacidad());

        if (espacio.getPunto() != null) {
            lugar.setIdPunto(espacio.getPunto().getIdPunto());
            lugar.setEtiquetaPunto(espacio.getPunto().getEtiqueta());
            lugar.setCoordX(espacio.getPunto().getCoordX());
            lugar.setCoordY(espacio.getPunto().getCoordY());
        }

        return lugar;
    }

    private List<ActividadDetallePublicoResponse.OrganizadorPublicoResponse> buscarOrganizadores(Integer idActividad) {
        List<ActividadOrganizador> organizadores = organizadorRepo
                .findByActividadIdActividad(idActividad);

        List<ActividadDetallePublicoResponse.OrganizadorPublicoResponse> resultado = new ArrayList<>();
        for (ActividadOrganizador org : organizadores) {
            if (org.getCarrera() != null) {
                ActividadDetallePublicoResponse.OrganizadorPublicoResponse o = new ActividadDetallePublicoResponse.OrganizadorPublicoResponse();
                o.setNombre(org.getCarrera().getNombre());
                o.setTipo("CARRERA");
                resultado.add(o);
            }
            if (org.getDepartamento() != null) {
                ActividadDetallePublicoResponse.OrganizadorPublicoResponse o = new ActividadDetallePublicoResponse.OrganizadorPublicoResponse();
                o.setNombre(org.getDepartamento().getNombre());
                o.setTipo("DEPARTAMENTO");
                resultado.add(o);
            }
        }
        return resultado;
    }


    private ActividadPublicaDTO toDTO(Actividad a) {
        ActividadPublicaDTO dto = new ActividadPublicaDTO();
        dto.setId(a.getIdActividad());
        dto.setNombre(a.getNombre());
        dto.setDescripcion(a.getDescripcion());
        dto.setFechaActividad(a.getFechaActividad());
        dto.setHoraInicio(a.getHoraInicio());
        dto.setHoraFin(a.getHoraFin());
        dto.setTipo(a.getTipo().getNombre());
        dto.setCampus(a.getCampus() != null ? a.getCampus().getNombre() : null);
        dto.setCategoria(a.getTipo().getCategoria().getNombre()); // NUEVO
        dto.setRequiereInscripcion(Boolean.TRUE.equals(a.getRequiereInscripcion()));

        // NUEVO: buscar imagen de portada
        if (a.getImagenes() != null) {
            a.getImagenes().stream()
                    .filter(ActividadImagen::getEsPortada)
                    .findFirst()
                    .ifPresent(img -> dto.setImagenPortada(img.getUrl()));
        }

        return dto;
    }
}
