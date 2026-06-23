package mx.edu.unpa.actividadesapi.exception;

import lombok.Getter;

/**
 * US-26: Se lanza cuando el admin intenta destacar un evento pero ya existe
 * otro destacado activo y no confirmo el reemplazo (confirmarReemplazo=false).
 * Lleva los datos del destacado actual para que el front muestre la advertencia.
 */
@Getter
public class DestacadoConflictoException extends RuntimeException {

    private final Integer idActual;
    private final String nombreActual;

    public DestacadoConflictoException(Integer idActual, String nombreActual) {
        super("Ya existe un evento destacado activo: '" + nombreActual + "'");
        this.idActual = idActual;
        this.nombreActual = nombreActual;
    }
}
