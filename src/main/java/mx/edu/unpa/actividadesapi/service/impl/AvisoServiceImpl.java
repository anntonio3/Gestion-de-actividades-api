package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.request.AvisoRequest;
import mx.edu.unpa.actividadesapi.dto.response.AvisoResponse;
import mx.edu.unpa.actividadesapi.enums.Rol;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Aviso;
import mx.edu.unpa.actividadesapi.model.Usuario;
import mx.edu.unpa.actividadesapi.repository.AvisoRepository;
import mx.edu.unpa.actividadesapi.repository.UsuarioRepository;
import mx.edu.unpa.actividadesapi.service.AvisoService;
import mx.edu.unpa.actividadesapi.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvisoServiceImpl implements AvisoService {

    private static final Logger log = LoggerFactory.getLogger(AvisoServiceImpl.class);

    @Autowired private AvisoRepository avisoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private StorageService storageService;

    // ================================================================
    // US-17: Crear aviso
    // ================================================================
    @Override
    @Transactional
    public AvisoResponse crear(AvisoRequest request, MultipartFile foto) {
        log.info("US-17: Creando aviso para profesor id={}", request.getIdProfesor());

        Usuario profesor = validarProfesor(request.getIdProfesor());

        Aviso aviso = new Aviso();
        aviso.setProfesor(profesor);
        aviso.setTitulo(request.getTitulo().trim());
        aviso.setDescripcion(request.getDescripcion().trim());
        aviso.setFechaEvento(request.getFechaEvento());
        aviso.setHoraEvento(request.getHoraEvento());
        aviso.setActivo(true);

        // Guardar primero para tener id (lo usamos como subcarpeta de la foto)
        aviso = avisoRepository.save(aviso);

        if (foto != null && !foto.isEmpty()) {
            String url = storageService.guardar(foto, "avisos/aviso-" + aviso.getIdAviso());
            aviso.setFotoUrl(url);
            aviso = avisoRepository.save(aviso);
            log.info("Foto guardada para aviso id={}: {}", aviso.getIdAviso(), url);
        }

        log.info("Aviso publicado id={} titulo='{}'", aviso.getIdAviso(), aviso.getTitulo());
        return toResponse(aviso);
    }

    // ================================================================
    // US-19: Editar aviso
    // ================================================================
    @Override
    @Transactional
    public AvisoResponse actualizar(Integer idAviso, AvisoRequest request, MultipartFile foto) {
        log.info("US-19: Actualizando aviso id={} por profesor id={}",
                idAviso, request.getIdProfesor());

        Aviso aviso = buscarActivo(idAviso);

        // Solo el dueno puede editar (mientras no haya auth, validamos por idProfesor del body)
        if (!aviso.getProfesor().getIdUsuario().equals(request.getIdProfesor())) {
            log.warn("Profesor id={} intento editar aviso id={} que no le pertenece",
                    request.getIdProfesor(), idAviso);
            throw new BusinessException("No tienes permiso para editar este aviso");
        }

        aviso.setTitulo(request.getTitulo().trim());
        aviso.setDescripcion(request.getDescripcion().trim());
        aviso.setFechaEvento(request.getFechaEvento());
        aviso.setHoraEvento(request.getHoraEvento());

        // Solo reemplaza la foto si llega una nueva
        if (foto != null && !foto.isEmpty()) {
            if (aviso.getFotoUrl() != null) {
                storageService.eliminar(aviso.getFotoUrl());
            }
            String url = storageService.guardar(foto, "avisos/aviso-" + aviso.getIdAviso());
            aviso.setFotoUrl(url);
            log.info("Foto reemplazada en aviso id={}: {}", idAviso, url);
        }

        aviso = avisoRepository.save(aviso);
        log.info("Aviso id={} actualizado", idAviso);
        return toResponse(aviso);
    }

    // ================================================================
    // Soft delete
    // ================================================================
    @Override
    @Transactional
    public void desactivar(Integer idAviso, Integer idProfesor) {
        log.info("Desactivando aviso id={} por profesor id={}", idAviso, idProfesor);

        Aviso aviso = buscarActivo(idAviso);

        if (!aviso.getProfesor().getIdUsuario().equals(idProfesor)) {
            log.warn("Profesor id={} intento desactivar aviso id={} ajeno", idProfesor, idAviso);
            throw new BusinessException("No tienes permiso para retirar este aviso");
        }

        aviso.setActivo(false);
        avisoRepository.save(aviso);
        log.info("Aviso id={} desactivado (soft delete)", idAviso);
    }

    // ================================================================
    // US-19: Listado del profesor
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public List<AvisoResponse> listarPorProfesor(Integer idProfesor) {
        validarProfesor(idProfesor);
        return avisoRepository
                .findByProfesor_IdUsuarioAndActivoTrueOrderByFechaPublicacionDesc(idProfesor)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ================================================================
    // US-18/US-20: Listado publico
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public List<AvisoResponse> listarPublico(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        List<Aviso> lista;

        if (fecha != null) {
            // Filtro por fecha exacta
            lista = avisoRepository
                    .findByActivoTrueAndFechaEventoOrderByHoraEventoAsc(fecha);
        } else if (desde != null && hasta != null) {
            // Filtro por rango
            if (hasta.isBefore(desde)) {
                throw new BusinessException("La fecha 'hasta' no puede ser anterior a 'desde'");
            }
            lista = avisoRepository
                    .findByActivoTrueAndFechaEventoBetweenOrderByFechaEventoAscHoraEventoAsc(
                            desde, hasta);
        } else {
            // Sin filtros: todos los activos, proximos primero
            lista = avisoRepository.findByActivoTrueOrderByFechaEventoAscHoraEventoAsc();
        }

        return lista.stream().map(this::toResponse).toList();
    }

    // ================================================================
    // Detalle individual
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public AvisoResponse obtenerPorId(Integer idAviso) {
        return toResponse(buscarActivo(idAviso));
    }

    // ================================================================
    // Helpers privados
    // ================================================================

    private Aviso buscarActivo(Integer idAviso) {
        Aviso aviso = avisoRepository.findById(idAviso)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el aviso con id: " + idAviso));

        if (!aviso.getActivo()) {
            throw new ResourceNotFoundException(
                    "El aviso con id " + idAviso + " fue retirado");
        }
        return aviso;
    }

    private Usuario validarProfesor(Integer idProfesor) {
        Usuario profesor = usuarioRepository.findById(idProfesor)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor no encontrado con id: " + idProfesor));

        if (profesor.getRol() != Rol.PROFESOR) {
            throw new BusinessException(
                    "El usuario id=" + idProfesor + " no tiene rol PROFESOR");
        }
        if (Boolean.FALSE.equals(profesor.getActivo())) {
            throw new BusinessException(
                    "El profesor id=" + idProfesor + " no esta activo");
        }
        return profesor;
    }

    private AvisoResponse toResponse(Aviso a) {
        AvisoResponse dto = new AvisoResponse();
        dto.setIdAviso(a.getIdAviso());
        dto.setTitulo(a.getTitulo());
        dto.setDescripcion(a.getDescripcion());
        dto.setFechaEvento(a.getFechaEvento());
        dto.setHoraEvento(a.getHoraEvento());
        dto.setFotoUrl(a.getFotoUrl());
        dto.setFechaPublicacion(a.getFechaPublicacion());
        dto.setFechaActualizacion(a.getFechaActualizacion());

        if (a.getProfesor() != null) {
            dto.setIdProfesor(a.getProfesor().getIdUsuario());
            dto.setNombreProfesor(
                    a.getProfesor().getNombre() + " " + a.getProfesor().getApellidos());
        }
        return dto;
    }
}
