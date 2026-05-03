package mx.edu.unpa.actividadesapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ActividadNoEditableException extends RuntimeException {

    public ActividadNoEditableException(String mensaje) {
        super(mensaje);
    }
}