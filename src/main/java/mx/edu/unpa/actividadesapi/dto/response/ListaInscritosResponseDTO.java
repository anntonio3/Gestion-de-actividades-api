package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ListaInscritosResponseDTO {
    private Integer idActividad;
    private String nombreEvento;
    private LocalDate fechaEvento;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer totalInscritos;
    private List<InscritoDTO> inscritos;
}