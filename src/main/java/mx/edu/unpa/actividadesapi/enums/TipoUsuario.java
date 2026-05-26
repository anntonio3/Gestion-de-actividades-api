package mx.edu.unpa.actividadesapi.enums;

/**
 * Tipo de actor autenticado en el sistema.
 * Distingue entre el rol de la tabla usuarios (ADMIN/PROFESOR)
 * y el tipo especial ALUMNO que tiene su propia tabla.
 */
public enum TipoUsuario {
    ADMIN,
    PROFESOR,
    ALUMNO
}
