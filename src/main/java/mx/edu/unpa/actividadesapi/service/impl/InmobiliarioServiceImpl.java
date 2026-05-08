package mx.edu.unpa.actividadesapi.service.impl;


import mx.edu.unpa.actividadesapi.dto.request.InmobiliarioRequest;
import mx.edu.unpa.actividadesapi.dto.response.InmobiliarioResponse;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.RecursoMobiliario;
import mx.edu.unpa.actividadesapi.model.TipoRecurso;
import mx.edu.unpa.actividadesapi.repository.RecursoMobiliarioRepository;
import mx.edu.unpa.actividadesapi.repository.TipoRecursoRepository;
import mx.edu.unpa.actividadesapi.service.InmobiliarioService;
import mx.edu.unpa.actividadesapi.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class InmobiliarioServiceImpl implements InmobiliarioService {

    private static final Logger log = LoggerFactory.getLogger(InmobiliarioServiceImpl.class);

    // ID del tipo "MOBILIARIO" en la tabla tipos_recurso (orden de inserción = 2)
    private static final int ID_TIPO_MOBILIARIO = 2;

    @Autowired
    private RecursoMobiliarioRepository mobiliarioRepository;

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    @Autowired
    private StorageService storageService;

    // ─────────────────────────────────────────────────────────────────
    // US-14: Registrar nuevo inmobiliario
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public InmobiliarioResponse crear(InmobiliarioRequest request, MultipartFile foto) {

        validarDisponibles(request.getDisponibles(), request.getExistencias());

        TipoRecurso tipoMobiliario = tipoRecursoRepository.findById(ID_TIPO_MOBILIARIO)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso MOBILIARIO no encontrado"));

        RecursoMobiliario mobiliario = new RecursoMobiliario();
        mobiliario.setTipoRecurso(tipoMobiliario);
        mobiliario.setNombre(request.getNombre());
        mobiliario.setDescripcion(request.getDescripcion());
        mobiliario.setActivo(true);
        mobiliario.setCodigo(request.getCodigo());
        mobiliario.setNumInventario(request.getNumInventario());
        mobiliario.setExistencias(request.getExistencias());
        mobiliario.setDisponibles(request.getDisponibles());
        mobiliario.setNota(request.getNota());

        // Guardar primero para obtener el ID (lo usamos como subcarpeta)
        mobiliario = mobiliarioRepository.save(mobiliario);

        // Subir foto si viene
        if (foto != null && !foto.isEmpty()) {
            String url = storageService.guardar(foto, "inmobiliario/inmobiliario-" + mobiliario.getIdRecurso());
            mobiliario.setFoto(url);
            mobiliario = mobiliarioRepository.save(mobiliario);
            log.info("Foto guardada para inmobiliario id={}: {}", mobiliario.getIdRecurso(), url);
        }

        log.info("Inmobiliario creado: id={} nombre={}", mobiliario.getIdRecurso(), mobiliario.getNombre());
        return toResponse(mobiliario);
    }

    // ─────────────────────────────────────────────────────────────────
    // Listar todo el inmobiliario activo
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<InmobiliarioResponse> listar(String nombre) {
        List<RecursoMobiliario> lista = mobiliarioRepository.findByActivoTrue();

        // Filtro en memoria por nombre (simple, sin query extra)
        if (nombre != null && !nombre.isBlank()) {
            String filtro = nombre.trim().toLowerCase();
            lista = lista.stream()
                    .filter(m -> m.getNombre().toLowerCase().contains(filtro))
                    .toList();
        }

        return lista.stream().map(this::toResponse).toList();
    }

    // ─────────────────────────────────────────────────────────────────
    // Obtener uno por ID
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public InmobiliarioResponse obtenerPorId(Integer id) {
        return toResponse(buscarActivo(id));
    }

    // ─────────────────────────────────────────────────────────────────
    // Actualizar inmobiliario
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public InmobiliarioResponse actualizar(Integer id, InmobiliarioRequest request, MultipartFile foto) {

        validarDisponibles(request.getDisponibles(), request.getExistencias());

        RecursoMobiliario mobiliario = buscarActivo(id);

        mobiliario.setNombre(request.getNombre());
        mobiliario.setDescripcion(request.getDescripcion());
        mobiliario.setCodigo(request.getCodigo());
        mobiliario.setNumInventario(request.getNumInventario());
        mobiliario.setExistencias(request.getExistencias());
        mobiliario.setDisponibles(request.getDisponibles());
        mobiliario.setNota(request.getNota());

        // Solo reemplaza la foto si llega una nueva
        if (foto != null && !foto.isEmpty()) {
            // Eliminar la foto anterior si existe
            if (mobiliario.getFoto() != null) {
                storageService.eliminar(mobiliario.getFoto());
            }
            String url = storageService.guardar(foto, "inmobiliario/inmobiliario-" + id);
            mobiliario.setFoto(url);
            log.info("Foto actualizada para inmobiliario id={}: {}", id, url);
        }

        mobiliario = mobiliarioRepository.save(mobiliario);
        log.info("Inmobiliario actualizado: id={}", id);
        return toResponse(mobiliario);
    }

    // ─────────────────────────────────────────────────────────────────
    // Baja lógica
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void desactivar(Integer id) {
        RecursoMobiliario mobiliario = buscarActivo(id);
        mobiliario.setActivo(false);
        mobiliarioRepository.save(mobiliario);
        log.info("Inmobiliario desactivado: id={}", id);
    }

    // ─────────────────────────────────────────────────────────────────
    // Métodos privados
    // ─────────────────────────────────────────────────────────────────

    private RecursoMobiliario buscarActivo(Integer id) {
        RecursoMobiliario mobiliario = mobiliarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el inmobiliario con ID: " + id));

        if (!mobiliario.getActivo()) {
            throw new ResourceNotFoundException(
                    "El inmobiliario con ID " + id + " está dado de baja");
        }
        return mobiliario;
    }

    private void validarDisponibles(Integer disponibles, Integer existencias) {
        if (disponibles > existencias) {
            throw new BusinessException(
                    "Los disponibles (" + disponibles + ") no pueden superar " +
                            "las existencias (" + existencias + ")");
        }
    }

    private InmobiliarioResponse toResponse(RecursoMobiliario m) {
        return new InmobiliarioResponse(
                m.getIdRecurso(),
                m.getNombre(),
                m.getDescripcion(),
                m.getActivo(),
                m.getCodigo(),
                m.getNumInventario(),
                m.getExistencias(),
                m.getDisponibles(),
                m.getFoto(),   // ya es la URL completa (la guarda StorageService)
                m.getNota()
        );
    }
}