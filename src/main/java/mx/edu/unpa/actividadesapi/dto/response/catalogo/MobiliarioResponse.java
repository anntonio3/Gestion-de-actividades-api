package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class MobiliarioResponse {
    private Integer idRecurso;
    private String nombre;
    private String descripcion;
    private Integer cantidadTotal;       // el inventario
    private Integer cantidadDisponible;  // lo realmente libre en ese rango

    public MobiliarioResponse(Integer idRecurso, String nombre, String descripcion, Integer cantidadTotal, Integer cantidadDisponible) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadDisponible;
    }

}
