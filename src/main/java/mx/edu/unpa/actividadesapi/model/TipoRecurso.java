package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tipos_recurso")
public class TipoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_recurso")
    private Integer idTipoRecurso;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;
}
