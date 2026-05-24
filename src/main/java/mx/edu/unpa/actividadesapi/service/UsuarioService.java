package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.UsuarioCrearRequest;
import mx.edu.unpa.actividadesapi.dto.request.UsuarioEditarRequest;
import mx.edu.unpa.actividadesapi.dto.response.UsuarioResponse;
import mx.edu.unpa.actividadesapi.enums.Rol;

import java.util.List;

public interface UsuarioService {

    // US-01: Listar usuarios con filtros opcionales
    List<UsuarioResponse> listar(Rol rol, Boolean activo, String busqueda);

    // US-01: Obtener detalle de un usuario
    UsuarioResponse obtenerPorId(Integer id);

    // US-01: Crear nuevo usuario (contraseña generada automáticamente)
    UsuarioResponse crear(UsuarioCrearRequest request);

    // US-01: Editar datos del usuario (sin contraseña)
    UsuarioResponse editar(Integer id, UsuarioEditarRequest request);

    // US-01: Activar o desactivar usuario
    void cambiarEstado(Integer id, Boolean activo);
}
