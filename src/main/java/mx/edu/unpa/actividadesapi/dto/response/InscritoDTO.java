package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.TipoParticipante;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InscritoDTO {
    private String nombre;
    private TipoParticipante tipoParticipante;
    private LocalDateTime fechaInscripcion;
}