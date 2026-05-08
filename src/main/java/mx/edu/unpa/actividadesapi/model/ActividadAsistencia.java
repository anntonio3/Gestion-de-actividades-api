package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.RespuestaAsistencia;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "actividad_asistencia")
@IdClass(ActividadAsistenciaId.class)
public class ActividadAsistencia {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_visitante", nullable = false, columnDefinition = "CHAR(36)")
    private Visitante visitante;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    @Enumerated(EnumType.STRING)
    @Column(name = "respuesta", nullable = false)
    private RespuestaAsistencia respuesta;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaRegistro = ahora;
        this.fechaActualizacion = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}