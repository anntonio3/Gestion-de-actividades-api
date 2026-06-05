package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.EquipamientoRequest;
import mx.edu.unpa.actividadesapi.dto.request.EspacioRequest;
import mx.edu.unpa.actividadesapi.dto.response.EquipamientoResponse;
import mx.edu.unpa.actividadesapi.dto.response.EspacioDetalleResponse;
import mx.edu.unpa.actividadesapi.dto.response.MapaPuntoResponse;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.EspacioAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lógica de negocio para administración de espacios físicos (US-14).
 *
 * Restricción de ubicación:
 *   - Espacio interno : idPunto presente, campos externos nulos.
 *   - Espacio externo : latitud + longitud + urlMaps presentes, idPunto nulo.
 *   - No puede tener ambas ni ninguna.
 */
@Service
@RequiredArgsConstructor
public class EspacioAdminServiceImpl implements EspacioAdminService {

    private static final Logger log = LoggerFactory.getLogger(EspacioAdminServiceImpl.class);

    /** Id del tipo de recurso ESPACIO (sembrado en tipos_recurso). */
    private static final Integer TIPO_RECURSO_ESPACIO = 1;
    /** Id del tipo de recurso MOBILIARIO (sembrado en tipos_recurso). */
    private static final Integer TIPO_RECURSO_MOBILIARIO = 2;

    private final RecursoEspacioRepository recursoEspacioRepository;
    private final RecursoRepository        recursoRepository;
    private final MapaPuntoRepository      mapaPuntoRepository;
    private final EspacioEquipamientoRepository equipamientoRepository;

    // ========================================================
    //  Listado de puntos del mapa
    // ========================================================
    @Override
    @Transactional(readOnly = true)
    public List<MapaPuntoResponse> listarPuntosMapa() {
        log.info("Listando puntos del mapa");

        List<MapaPunto>    puntos   = mapaPuntoRepository.findAll();
        List<RecursoEspacio> espacios = recursoEspacioRepository.findAllConPunto();

        return puntos.stream().map(punto -> {
            RecursoEspacio espacio = espacios.stream()
                    .filter(e -> e.getPunto() != null
                            && e.getPunto().getIdPunto().equals(punto.getIdPunto()))
                    .findFirst()
                    .orElse(null);

            return new MapaPuntoResponse(
                    punto.getIdPunto(),
                    punto.getEtiqueta(),
                    punto.getCoordX(),
                    punto.getCoordY(),
                    espacio != null ? espacio.getIdRecurso()  : null,
                    espacio != null ? espacio.getNombre()     : null,
                    espacio != null ? espacio.getCapacidad()  : null,
                    espacio != null ? espacio.getActivo()     : null
            );
        }).toList();
    }

    // ========================================================
    //  Detalle de un espacio
    // ========================================================
    @Override
    @Transactional(readOnly = true)
    public EspacioDetalleResponse obtenerDetalle(Integer idEspacio) {
        log.info("Consultando detalle del espacio id={}", idEspacio);

        RecursoEspacio espacio = recursoEspacioRepository.findByIdConDetalle(idEspacio)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el espacio con id: " + idEspacio));

        return mapearDetalle(espacio);
    }

    // ========================================================
    //  Registrar espacio
    // ========================================================
    @Override
    @Transactional
    public EspacioDetalleResponse registrar(EspacioRequest request) {
        log.info("Registrando espacio nombre='{}' interno={}",
                request.getNombre(), request.getIdPunto() != null);

        // Valida que la ubicación sea excluyente y completa
        validarUbicacion(request, null);

        RecursoEspacio espacio = new RecursoEspacio();

        TipoRecurso tipoEspacio = new TipoRecurso();
        tipoEspacio.setIdTipoRecurso(TIPO_RECURSO_ESPACIO);
        espacio.setTipoRecurso(tipoEspacio);
        espacio.setNombre(request.getNombre());
        espacio.setDescripcion(request.getDescripcion());
        espacio.setActivo(true);
        espacio.setCapacidad(request.getCapacidad());
        espacio.setUbicacion(request.getUbicacion());

        if (request.getIdPunto() != null) {
            // Espacio interno: anclarlo al punto del mapa
            MapaPunto punto = mapaPuntoRepository.findById(request.getIdPunto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El punto del mapa con id " + request.getIdPunto() + " no existe"));
            espacio.setPunto(punto);
            log.info("Espacio interno anclado al punto id={}", request.getIdPunto());
        } else {
            // Espacio externo: guardar coordenadas de Google Maps
            espacio.setLatitud(request.getLatitud());
            espacio.setLongitud(request.getLongitud());
            espacio.setUrlMaps(request.getUrlMaps());
            log.info("Espacio externo con coordenadas lat={} lng={}",
                    request.getLatitud(), request.getLongitud());
        }

        validarEquipamiento(request.getEquipamiento());
        RecursoEspacio guardado = recursoEspacioRepository.save(espacio);
        guardarEquipamiento(guardado, request.getEquipamiento());

        log.info("Espacio registrado con id={} nombre='{}'",
                guardado.getIdRecurso(), guardado.getNombre());
        return obtenerDetalle(guardado.getIdRecurso());
    }

    // ========================================================
    //  Actualizar espacio
    // ========================================================
    @Override
    @Transactional
    public EspacioDetalleResponse actualizar(Integer idEspacio, EspacioRequest request) {
        log.info("Actualizando espacio id={}", idEspacio);

        RecursoEspacio espacio = recursoEspacioRepository.findById(idEspacio)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el espacio con id: " + idEspacio));

        // Valida ubicación considerando el espacio actual (excluir su propio punto)
        validarUbicacion(request, idEspacio);

        // Actualizar datos comunes
        espacio.setNombre(request.getNombre());
        espacio.setDescripcion(request.getDescripcion());
        espacio.setCapacidad(request.getCapacidad());
        espacio.setUbicacion(request.getUbicacion());

        if (request.getIdPunto() != null) {
            // Cambió o mantiene ubicación interna
            Integer idPuntoActual = espacio.getPunto() != null
                    ? espacio.getPunto().getIdPunto() : null;

            if (!request.getIdPunto().equals(idPuntoActual)) {
                MapaPunto nuevoPunto = mapaPuntoRepository.findById(request.getIdPunto())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "El punto del mapa con id " + request.getIdPunto() + " no existe"));
                espacio.setPunto(nuevoPunto);
                log.info("Punto del mapa actualizado a id={}", request.getIdPunto());
            }
            // Limpiar campos externos si antes era externo
            espacio.setLatitud(null);
            espacio.setLongitud(null);
            espacio.setUrlMaps(null);
        } else {
            // Cambió o mantiene ubicación externa
            espacio.setPunto(null);
            espacio.setLatitud(request.getLatitud());
            espacio.setLongitud(request.getLongitud());
            espacio.setUrlMaps(request.getUrlMaps());
            log.info("Ubicación externa actualizada lat={} lng={}",
                    request.getLatitud(), request.getLongitud());
        }

        validarEquipamiento(request.getEquipamiento());
        equipamientoRepository.eliminarPorEspacio(idEspacio);
        equipamientoRepository.flush();
        guardarEquipamiento(espacio, request.getEquipamiento());

        log.info("Espacio id={} actualizado correctamente", idEspacio);
        return obtenerDetalle(idEspacio);
    }

    // ========================================================
    //  Cambiar estado
    // ========================================================
    @Override
    @Transactional
    public void cambiarEstado(Integer idEspacio, Boolean activo) {
        log.info("Cambiando estado del espacio id={} a activo={}", idEspacio, activo);

        RecursoEspacio espacio = recursoEspacioRepository.findById(idEspacio)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el espacio con id: " + idEspacio));

        espacio.setActivo(activo);
        log.info("Estado actualizado para espacio id={}", idEspacio);
    }

    // ========================================================
    //  Métodos privados auxiliares
    // ========================================================

    /**
     * Valida que la ubicación sea excluyente y completa.
     *
     * Casos válidos:
     *   1. idPunto presente, latitud/longitud/urlMaps nulos -> interno
     *   2. latitud + longitud + urlMaps presentes, idPunto nulo -> externo
     *
     * Casos inválidos:
     *   - Ni interno ni externo (falta todo)
     *   - Tiene idPunto Y coordenadas externas
     *   - Tiene coordenadas externas pero le falta latitud, longitud o urlMaps
     *
     * @param idEspacioActual null al crear, el id al editar (para excluirlo de la
     *                        validación de punto ocupado).
     */
    private void validarUbicacion(EspacioRequest request, Integer idEspacioActual) {
        boolean tieneInterno = request.getIdPunto() != null;
        boolean tieneExterno = request.getLatitud() != null
                || request.getLongitud() != null
                || (request.getUrlMaps() != null && !request.getUrlMaps().isBlank());

        if (!tieneInterno && !tieneExterno) {
            log.warn("Intento de guardar espacio sin ubicación definida");
            throw new BusinessException(
                    "Debes indicar una ubicación: selecciona un punto del mapa UNPA "
                            + "o proporciona coordenadas de Google Maps");
        }

        if (tieneInterno && tieneExterno) {
            log.warn("Intento de guardar espacio con ubicación interna y externa simultáneas");
            throw new BusinessException(
                    "Un espacio no puede tener ubicación interna (mapa UNPA) "
                            + "y externa (Google Maps) al mismo tiempo");
        }

        if (tieneExterno) {
            // Si es externo, los tres campos son obligatorios
            if (request.getLatitud() == null || request.getLongitud() == null) {
                log.warn("Espacio externo sin latitud o longitud");
                throw new BusinessException(
                        "Para un espacio externo debes proporcionar latitud y longitud");
            }
            if (request.getUrlMaps() == null || request.getUrlMaps().isBlank()) {
                log.warn("Espacio externo sin URL de Google Maps");
                throw new BusinessException(
                        "Para un espacio externo debes proporcionar la URL de Google Maps");
            }
        }

        if (tieneInterno) {
            // Validar que el punto no esté ocupado por otro espacio
            if (recursoEspacioRepository.isPuntoOcupado(request.getIdPunto(), idEspacioActual)) {
                log.warn("Punto del mapa id={} ya está ocupado por otro espacio",
                        request.getIdPunto());
                throw new BusinessException(
                        "El punto seleccionado ya tiene un espacio asignado");
            }
        }
    }

    /** Valida que cada recurso del equipamiento sea de tipo MOBILIARIO y sin duplicados. */
    private void validarEquipamiento(List<EquipamientoRequest> equipamiento) {
        if (equipamiento == null || equipamiento.isEmpty()) return;

        Set<Integer> vistos = new HashSet<>();
        for (EquipamientoRequest e : equipamiento) {
            if (!vistos.add(e.getIdRecurso())) {
                throw new BusinessException(
                        "El recurso con id " + e.getIdRecurso()
                                + " está duplicado en el equipamiento");
            }
        }

        for (EquipamientoRequest e : equipamiento) {
            Recurso recurso = recursoRepository.findById(e.getIdRecurso())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El recurso con id " + e.getIdRecurso() + " no existe"));

            if (!recurso.getTipoRecurso().getIdTipoRecurso().equals(TIPO_RECURSO_MOBILIARIO)) {
                throw new BusinessException(
                        "El recurso '" + recurso.getNombre()
                                + "' no es de tipo MOBILIARIO y no puede usarse como equipamiento");
            }
        }
    }

    /** Persiste la lista de equipamiento asociada al espacio. */
    private void guardarEquipamiento(RecursoEspacio espacio,
                                     List<EquipamientoRequest> equipamiento) {
        if (equipamiento == null || equipamiento.isEmpty()) return;

        for (EquipamientoRequest e : equipamiento) {
            Recurso recurso = recursoRepository.findById(e.getIdRecurso()).orElseThrow();

            EspacioEquipamiento ee = new EspacioEquipamiento();
            ee.setEspacio(espacio);
            ee.setRecurso(recurso);
            ee.setCantidad(e.getCantidad());
            ee.setCaracteristicas(e.getCaracteristicas());
            equipamientoRepository.save(ee);
        }
    }

    /** Construye el DTO de detalle a partir de la entidad. */
    private EspacioDetalleResponse mapearDetalle(RecursoEspacio espacio) {
        EspacioDetalleResponse dto = new EspacioDetalleResponse();
        dto.setIdEspacio(espacio.getIdRecurso());
        dto.setNombre(espacio.getNombre());
        dto.setDescripcion(espacio.getDescripcion());
        dto.setCapacidad(espacio.getCapacidad());
        dto.setUbicacion(espacio.getUbicacion());
        dto.setActivo(espacio.getActivo());
        dto.setEsExterno(espacio.esExterno());

        if (espacio.esInterno() && espacio.getPunto() != null) {
            dto.setIdPunto(espacio.getPunto().getIdPunto());
            dto.setEtiquetaPunto(espacio.getPunto().getEtiqueta());
            dto.setCoordX(espacio.getPunto().getCoordX());
            dto.setCoordY(espacio.getPunto().getCoordY());
        } else if (espacio.esExterno()) {
            dto.setLatitud(espacio.getLatitud());
            dto.setLongitud(espacio.getLongitud());
            dto.setUrlMaps(espacio.getUrlMaps());
        }

        List<EquipamientoResponse> equip = espacio.getEquipamiento().stream()
                .map(e -> new EquipamientoResponse(
                        e.getRecurso().getIdRecurso(),
                        e.getRecurso().getNombre(),
                        e.getCantidad(),
                        e.getCaracteristicas()))
                .toList();
        dto.setEquipamiento(equip);
        return dto;
    }
}
