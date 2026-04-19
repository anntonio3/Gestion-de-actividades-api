package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class CategoriaResponse {
    private Integer idCategoria;
    private String nombre;
    private String descripcion;

    public CategoriaResponse(Integer idCategoria, String nombre, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

}