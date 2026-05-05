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
 * El admin gestiona los espacios desde el mapa interactivo de la UNPA.
 */
@Service
@RequiredArgsConstructor
public class EspacioAdminServiceImpl implements EspacioAdminService {

    private static final Logger log = LoggerFactory.getLogger(EspacioAdminServiceImpl.class);

    /** Id del tipo de recurso ESPACIO (sembrado en tipos_recurso) */
    private static final Integer TIPO_RECURSO_ESPACIO = 1;
    /** Id del tipo de recurso MOBILIARIO (sembrado en tipos_recurso) */
    private static final Integer TIPO_RECURSO_MOBILIARIO = 2;

    private final RecursoEspacioRepository recursoEspacioRepository;
    private final RecursoRepository recursoRepository;
    private final MapaPuntoRepository mapaPuntoRepository;
    private final EspacioEquipamientoRepository equipamientoRepository;

    // ========================================================
    //  Listado de puntos del mapa
    // ========================================================
    @Override
    @Transactional(readOnly = true)
    public List<MapaPuntoResponse> listarPuntosMapa() {
        log.info("Listando puntos del mapa");

        // Cargar todos los puntos
        List<MapaPunto> puntos = mapaPuntoRepository.findAll();

        // Cargar espacios que ya tienen punto asignado
        List<RecursoEspacio> espacios = recursoEspacioRepository.findAllConPunto();

        // Mapear cada punto con el espacio que le corresponde (si lo hay)
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
                    espacio != null ? espacio.getIdRecurso() : null,
                    espacio != null ? espacio.getNombre() : null,
                    espacio != null ? espacio.getCapacidad() : null,
                    espacio != null ? espacio.getActivo() : null
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
        log.info("Registrando espacio en punto={}, nombre='{}'",
                request.getIdPunto(), request.getNombre());

        // 1. Validar que el punto del mapa exista
        MapaPunto punto = mapaPuntoRepository.findById(request.getIdPunto())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El punto del mapa con id " + request.getIdPunto() + " no existe"));

        // 2. Validar que el punto no esté ocupado por otro espacio
        if (recursoEspacioRepository.isPuntoOcupado(request.getIdPunto(), null)) {
            log.warn("Intento de asignar punto ya ocupado: idPunto={}", request.getIdPunto());
            throw new BusinessException("El punto seleccionado ya tiene un espacio asignado");
        }

        // 3. Validar el equipamiento (recursos válidos, sin duplicados, tipo correcto)
        validarEquipamiento(request.getEquipamiento());

        // 4. Crear el RecursoEspacio (hereda de Recurso, así que los campos
        //    de la tabla padre y la hija se persisten en cascada por la herencia JOINED)
        RecursoEspacio espacio = new RecursoEspacio();

        // Campos heredados de Recurso
        TipoRecurso tipoEspacio = new TipoRecurso();
        tipoEspacio.setIdTipoRecurso(TIPO_RECURSO_ESPACIO);
        espacio.setTipoRecurso(tipoEspacio);
        espacio.setNombre(request.getNombre());
        espacio.setDescripcion(request.getDescripcion());
        espacio.setActivo(true);

        // Campos propios de RecursoEspacio
        espacio.setCapacidad(request.getCapacidad());
        espacio.setUbicacion(request.getUbicacion());
        espacio.setPunto(punto);

        RecursoEspacio guardado = recursoEspacioRepository.save(espacio);
        log.info("Espacio creado con id={}", guardado.getIdRecurso());

        // 5. Guardar equipamiento
        guardarEquipamiento(guardado, request.getEquipamiento());

        log.info("Espacio id={} registrado con {} equipamiento(s)",
                guardado.getIdRecurso(), request.getEquipamiento().size());

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

        // Si cambia el punto del mapa, validar que el nuevo no esté ocupado
        Integer idPuntoActual = espacio.getPunto() != null
                ? espacio.getPunto().getIdPunto() : null;

        if (!request.getIdPunto().equals(idPuntoActual)) {
            MapaPunto nuevoPunto = mapaPuntoRepository.findById(request.getIdPunto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El punto del mapa con id " + request.getIdPunto() + " no existe"));

            if (recursoEspacioRepository.isPuntoOcupado(request.getIdPunto(), idEspacio)) {
                log.warn("Intento de mover espacio {} a punto ocupado {}",
                        idEspacio, request.getIdPunto());
                throw new BusinessException("El punto seleccionado ya tiene otro espacio asignado");
            }
            espacio.setPunto(nuevoPunto);
        }

        // Validar equipamiento
        validarEquipamiento(request.getEquipamiento());

        // Actualizar campos del espacio
        espacio.setNombre(request.getNombre());
        espacio.setDescripcion(request.getDescripcion());
        espacio.setCapacidad(request.getCapacidad());
        espacio.setUbicacion(request.getUbicacion());

        // Reescribir equipamiento (más simple que hacer diff)
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

    /** Valida que cada equipamiento sea de tipo MOBILIARIO y no haya duplicados. */
    private void validarEquipamiento(List<EquipamientoRequest> equipamiento) {
        if (equipamiento == null || equipamiento.isEmpty()) {
            return;
        }

        // Detectar duplicados por idRecurso
        Set<Integer> recursosVistos = new HashSet<>();
        for (EquipamientoRequest e : equipamiento) {
            if (!recursosVistos.add(e.getIdRecurso())) {
                throw new BusinessException(
                        "El recurso con id " + e.getIdRecurso() +
                                " está duplicado en el equipamiento");
            }
        }

        // Validar tipo MOBILIARIO
        for (EquipamientoRequest e : equipamiento) {
            Recurso recurso = recursoRepository.findById(e.getIdRecurso())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El recurso con id " + e.getIdRecurso() + " no existe"));

            if (!recurso.getTipoRecurso().getIdTipoRecurso().equals(TIPO_RECURSO_MOBILIARIO)) {
                throw new BusinessException(
                        "El recurso '" + recurso.getNombre() +
                                "' no es de tipo MOBILIARIO y no puede usarse como equipamiento");
            }
        }
    }

    /** Persiste la lista de equipamiento asociada al espacio. */
    private void guardarEquipamiento(RecursoEspacio espacio,
                                     List<EquipamientoRequest> equipamiento) {
        if (equipamiento == null || equipamiento.isEmpty()) {
            return;
        }

        for (EquipamientoRequest e : equipamiento) {
            // Ya validado en validarEquipamiento, solo recuperamos
            Recurso recurso = recursoRepository.findById(e.getIdRecurso())
                    .orElseThrow();

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

        if (espacio.getPunto() != null) {
            dto.setIdPunto(espacio.getPunto().getIdPunto());
            dto.setEtiquetaPunto(espacio.getPunto().getEtiqueta());
            dto.setCoordX(espacio.getPunto().getCoordX());
            dto.setCoordY(espacio.getPunto().getCoordY());
        }

        List<EquipamientoResponse> equipamiento = espacio.getEquipamiento().stream()
                .map(e -> new EquipamientoResponse(
                        e.getRecurso().getIdRecurso(),
                        e.getRecurso().getNombre(),
                        e.getCantidad(),
                        e.getCaracteristicas()))
                .toList();

        dto.setEquipamiento(equipamiento);
        return dto;
    }
}
