package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "recursos_mobiliario")
@PrimaryKeyJoinColumn(name = "id_recurso")
@DiscriminatorValue("MOBILIARIO")
public class RecursoMobiliario extends Recurso {

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
