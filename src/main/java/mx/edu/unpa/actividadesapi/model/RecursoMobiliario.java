package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "recursos_mobiliario")
@PrimaryKeyJoinColumn(name = "id_recurso")
public class RecursoMobiliario extends Recurso {

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
