package mx.edu.unpa.actividadesapi.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta para EspacioEquipamiento.
 * Sigue el mismo patrón que ActividadRecursoId.
 */
public class EspacioEquipamientoId implements Serializable {

    private Integer espacio;
    private Integer recurso;

    public EspacioEquipamientoId() {
    }

    public EspacioEquipamientoId(Integer espacio, Integer recurso) {
        this.espacio = espacio;
        this.recurso = recurso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EspacioEquipamientoId)) return false;
        EspacioEquipamientoId that = (EspacioEquipamientoId) o;
        return Objects.equals(espacio, that.espacio) &&
                Objects.equals(recurso, that.recurso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(espacio, recurso);
    }
}