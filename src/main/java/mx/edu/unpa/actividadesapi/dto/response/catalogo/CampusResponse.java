package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class CampusResponse {

    private Integer idCampus;
    private String nombre;
    private String ciudad;

    public CampusResponse(Integer idCampus, String nombre, String ciudad) {
        this.idCampus = idCampus;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }
}
