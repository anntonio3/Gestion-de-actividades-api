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

    // US-14: código de bien (opcional)
    @Column(name = "codigo", length = 50)
    private String codigo;

    // US-14: número de inventario institucional (opcional)
    @Column(name = "num_inventario", length = 100)
    private String numInventario;

    // US-14: total físico en almacén (antes "cantidad")
    @Column(name = "existencias", nullable = false)
    private Integer existencias = 0;

    // US-14: unidades actualmente sin asignar
    @Column(name = "disponibles", nullable = false)
    private Integer disponibles = 0;

    // US-14: ruta relativa de la foto (ej: inmobiliario/uuid.jpg)
    @Column(name = "foto", length = 500)
    private String foto;

    // US-14: observaciones libres (mantenimiento, estado, etc.)
    @Column(name = "nota", columnDefinition = "TEXT")
    private String nota;
}