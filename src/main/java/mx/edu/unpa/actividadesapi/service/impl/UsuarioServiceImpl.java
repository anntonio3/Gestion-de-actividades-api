package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.UsuarioCrearRequest;
import mx.edu.unpa.actividadesapi.dto.request.UsuarioEditarRequest;
import mx.edu.unpa.actividadesapi.dto.response.UsuarioResponse;
import mx.edu.unpa.actividadesapi.enums.Rol;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Usuario;
import mx.edu.unpa.actividadesapi.repository.UsuarioRepository;
import mx.edu.unpa.actividadesapi.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    // ID del administrador raíz del sistema (sembrado en el seed).
    // No se puede desactivar para evitar quedar sin acceso.
    private static final Integer ID_ADMIN_RAIZ = 1;

    private final UsuarioRepository usuarioRepository;

    // ================================================================
    //  Listar
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Rol rol, Boolean activo, String busqueda) {
        log.info("Listando usuarios filtros[rol={}, activo={}, q={}]", rol, activo, busqueda);

        // Traemos todos y filtramos en memoria.
        // El volumen de usuarios es bajo en este sistema (decenas, no miles),
        // así que no justifica JPQL dinámico por ahora.
        List<Usuario> todos = usuarioRepository.findAll();

        String q = (busqueda == null) ? "" : busqueda.trim().toLowerCase();

        return todos.stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .filter(u -> activo == null || u.getActivo().equals(activo))
                .filter(u -> q.isEmpty()
                        || u.getNombre().toLowerCase().contains(q)
                        || u.getApellidos().toLowerCase().contains(q)
                        || u.getCorreo().toLowerCase().contains(q))
                .sorted((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()))
                .map(this::toResponse)
                .toList();
    }

    // ================================================================
    //  Detalle
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        log.info("Obteniendo usuario id={}", id);
        return toResponse(buscarOLanzar(id));
    }

    // ================================================================
    //  Crear
    // ================================================================
    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        log.info("Creando usuario correo={} rol={}", request.getCorreo(), request.getRol());

        String correoNormalizado = request.getCorreo().trim().toLowerCase();

        // Validar unicidad del correo
        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            log.warn("Intento de crear usuario con correo duplicado: {}", correoNormalizado);
            throw new BusinessException(
                    "Ya existe un usuario registrado con el correo: " + correoNormalizado);
        }

        // Generar contraseña inicial = parte antes del @
        // Ejemplo: pedro@unpa.edu.mx → contraseña inicial "pedro"
        // TODO US-00: aplicar BCrypt cuando se implemente el ticket de autenticación.
        String passwordInicial = correoNormalizado.substring(0, correoNormalizado.indexOf('@'));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setContrasenaHash(passwordInicial);
        usuario.setRol(request.getRol());
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado id={} correo={}", guardado.getIdUsuario(), guardado.getCorreo());

        return toResponse(guardado);
    }

    // ================================================================
    //  Editar
    // ================================================================
    @Override
    @Transactional
    public UsuarioResponse editar(Integer id, UsuarioEditarRequest request) {
        log.info("Editando usuario id={}", id);

        Usuario usuario = buscarOLanzar(id);
        String correoNormalizado = request.getCorreo().trim().toLowerCase();

        // Validar unicidad del correo si cambió
        if (!usuario.getCorreo().equals(correoNormalizado)
                && usuarioRepository.existsByCorreo(correoNormalizado)) {
            log.warn("Intento de editar usuario id={} con correo duplicado: {}", id, correoNormalizado);
            throw new BusinessException(
                    "Ya existe un usuario registrado con el correo: " + correoNormalizado);
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setRol(request.getRol());

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario id={} actualizado correctamente", id);

        return toResponse(guardado);
    }

    // ================================================================
    //  Cambiar estado
    // ================================================================
    @Override
    @Transactional
    public void cambiarEstado(Integer id, Boolean activo) {
        log.info("Cambiando estado usuario id={} activo={}", id, activo);

        // Proteger al administrador raíz del sistema
        if (ID_ADMIN_RAIZ.equals(id) && Boolean.FALSE.equals(activo)) {
            log.warn("Intento de desactivar al administrador raíz id={}", id);
            throw new BusinessException(
                    "No se puede desactivar al administrador principal del sistema");
        }

        Usuario usuario = buscarOLanzar(id);
        usuario.setActivo(activo);
        log.info("Usuario id={} {} correctamente", id, activo ? "activado" : "desactivado");
    }

    // ================================================================
    //  Helpers privados
    // ================================================================

    private Usuario buscarOLanzar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con id: " + id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombre(u.getNombre());
        dto.setApellidos(u.getApellidos());
        dto.setCorreo(u.getCorreo());
        dto.setRol(u.getRol());
        dto.setActivo(u.getActivo());
        dto.setFechaRegistro(u.getFechaRegistro());
        dto.setIniciales(calcularIniciales(u.getNombre(), u.getApellidos()));
        return dto;
    }

    /**
     * Genera las iniciales del usuario para el avatar.
     * Toma la primera letra del nombre y la primera letra del primer apellido.
     * Ejemplo: "Carlos Hernández López" → "CH"
     */
    private String calcularIniciales(String nombre, String apellidos) {
        String iniciales = "";
        if (nombre != null && !nombre.isBlank()) {
            iniciales += nombre.trim().charAt(0);
        }
        if (apellidos != null && !apellidos.isBlank()) {
            iniciales += apellidos.trim().charAt(0);
        }
        return iniciales.toUpperCase();
    }
}
