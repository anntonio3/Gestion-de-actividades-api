package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "actividad_imagenes")
public class ActividadImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "nombre_archivo", length = 150)
    private String nombreArchivo;

    @Column(name = "es_portada", nullable = false)
    private Boolean esPortada = false;

    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
    }
}

