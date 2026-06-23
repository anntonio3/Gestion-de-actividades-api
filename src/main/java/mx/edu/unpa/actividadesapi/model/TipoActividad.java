package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.NivelImportancia;

@Data
@Entity
@Table(name = "tipos_actividad")
public class TipoActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    // US-25: nivel de importancia para sugerir destacado al aprobar
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_importancia", nullable = false)
    private NivelImportancia nivelImportancia = NivelImportancia.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

}
