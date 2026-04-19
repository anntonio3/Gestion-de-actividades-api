package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class TipoActividadResponse {
    private Integer idTipo;
    private String nombre;
    private Integer idCategoria;
    private String nombreCategoria;

    public TipoActividadResponse(Integer idTipo, String nombre, Integer idCategoria, String nombreCategoria) {
        this.idTipo = idTipo;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

}
