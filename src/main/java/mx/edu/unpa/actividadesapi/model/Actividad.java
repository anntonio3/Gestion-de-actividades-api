package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Integer idActividad;

    // Relación con usuarios (profesor que registra)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesor", nullable = false)
    private Usuario profesor;

    // Relación con tipos_actividad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoActividad tipo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDate fechaActividad;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoActividad estado = EstadoActividad.PENDIENTE;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // NUEVO
    @OneToMany(mappedBy = "actividad", fetch = FetchType.LAZY)
    private List<ActividadImagen> imagenes;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoActividad {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }
}