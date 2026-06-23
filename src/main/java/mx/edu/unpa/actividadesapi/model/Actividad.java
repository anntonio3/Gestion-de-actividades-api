package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Data
@NoArgsConstructor
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

    // Relacion con campus donde se realiza la actividad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campus", nullable = false)
    private Campus campus;

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

    @Column(name = "requiere_inscripcion", nullable = false)
    private Boolean requiereInscripcion = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoActividad estado = EstadoActividad.PENDIENTE;

    // Relación con usuarios (vicerrector que revisa) — puede ser null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vicerrector", nullable = true)
    private Usuario vicerrector;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // US-26: Evento destacado
    // Solo una actividad puede tener destacado_activo = true a la vez
    // (garantizado por indice funcional uq_un_destacado_activo en BD).
    @Column(name = "destacado_activo", nullable = false)
    private Boolean destacadoActivo = false;

    // Admin que confirmo el destacado (US-26)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destacado_por", nullable = true)
    private Usuario destacadoPor;

    @Column(name = "fecha_destacado")
    private LocalDateTime fechaDestacado;

    @OneToMany(mappedBy = "actividad", fetch = FetchType.LAZY)
    private List<ActividadImagen> imagenes;

    // Optimistic locking: previene escrituras concurrentes
    // entre edicion del profesor (US-05) y revision del admin (US-08/US-09).
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

}