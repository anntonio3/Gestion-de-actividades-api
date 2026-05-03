package mx.edu.unpa.actividadesapi.dto.response.catalogo;

import lombok.Data;

@Data
public class DepartamentoResponse {
    private Integer idDepartamento;
    private String nombre;

    public DepartamentoResponse(Integer idDepartamento, String nombre) {
        this.idDepartamento = idDepartamento;
        this.nombre = nombre;
    }
}
