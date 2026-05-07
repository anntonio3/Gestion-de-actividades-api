package mx.edu.unpa.actividadesapi.exception;

/**
 * Se lanza cuando una entidad fue modificada por otro usuario
 * mientras la actual la estaba editando (optimistic locking).
 */
public class ConcurrenciaException extends RuntimeException {
    public ConcurrenciaException(String mensaje) {
        super(mensaje);
    }
}