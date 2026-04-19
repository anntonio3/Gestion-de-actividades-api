package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class EspacioResponse {
    private Integer idRecurso;
    private String nombre;
    private String descripcion;
    private Integer capacidad;
    private String ubicacion;

    public EspacioResponse(Integer idRecurso, String nombre, String descripcion,
                           Integer capacidad, String ubicacion) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
    }

}
