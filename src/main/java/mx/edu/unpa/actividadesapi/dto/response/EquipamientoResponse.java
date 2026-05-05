package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

@Data
public class EquipamientoResponse {

    private Integer idRecurso;
    private String nombreRecurso;
    private Integer cantidad;
    private String caracteristicas;

    public EquipamientoResponse(Integer idRecurso, String nombreRecurso,
                                Integer cantidad, String caracteristicas) {
        this.idRecurso = idRecurso;
        this.nombreRecurso = nombreRecurso;
        this.cantidad = cantidad;
        this.caracteristicas = caracteristicas;
    }
}
