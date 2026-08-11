package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * US-04: Ítem de la lista de inscritos de una actividad.
 * Se usa para mostrar la tabla en el panel del docente
 * y para generar el PDF / CSV.
 */
@Data
@AllArgsConstructor
public class InscritoListaItemResponse {

    private Integer numero;           // número de fila (1, 2, 3...)
    private String  nombre;           // nombre completo
    private String  tipoParticipante; // "Alumno" | "Docente/Staff" | "Externo"
    private String  identificador;    // matrícula, correo institucional o "—"
    private String  fechaInscripcion; // "dd/MM/yyyy HH:mm"
}