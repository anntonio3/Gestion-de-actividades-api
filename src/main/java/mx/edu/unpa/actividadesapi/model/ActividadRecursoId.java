package mx.edu.unpa.actividadesapi.model;

import java.io.Serializable;
import java.util.Objects;

public class ActividadRecursoId implements Serializable {

    private Integer actividad;
    private Integer recurso;

    public ActividadRecursoId() {
    }

    public ActividadRecursoId(Integer actividad, Integer recurso) {
        this.actividad = actividad;
        this.recurso = recurso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActividadRecursoId)) return false;
        ActividadRecursoId that = (ActividadRecursoId) o;
        return Objects.equals(actividad, that.actividad) &&
                Objects.equals(recurso, that.recurso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actividad, recurso);
    }
}
