package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class CarreraResponse {
    private Integer idCarrera;
    private String nombre;

    public CarreraResponse(Integer idCarrera, String nombre) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
    }
}
