package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "recursos_espacio")
@PrimaryKeyJoinColumn(name = "id_recurso")
public class RecursoEspacio extends Recurso {

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;

}
