package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class MobiliarioResponse {
    private Integer idRecurso;
    private String nombre;
    private String descripcion;
    private Integer cantidad;

    public MobiliarioResponse(Integer idRecurso, String nombre, String descripcion, Integer cantidad) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

}
