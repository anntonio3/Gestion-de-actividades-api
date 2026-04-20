package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "actividad_imagenes")
public class ActividadImagen {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_imagen")
        private Integer idImagen;

        @ManyToOne
        @JoinColumn(name = "id_actividad")
        private Actividad actividad;

        private String url;

        @Column(name = "nombre_archivo")
        private String nombreArchivo;

        @Column(name = "es_portada")
        private Boolean esPortada;
}
