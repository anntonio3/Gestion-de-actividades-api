package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class RecursoResponse {
    private Integer idRecurso;
    private String nombre;
    private String tipoRecurso;
    private String descripcion;

    public RecursoResponse(Integer idRecurso, String nombre, String tipoRecurso, String descripcion) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.tipoRecurso = tipoRecurso;
        this.descripcion = descripcion;
    }
}

