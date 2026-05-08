package mx.edu.unpa.actividadesapi.model;

import java.io.Serializable;
import java.util.Objects;

public class ActividadAsistenciaId implements Serializable {

    private String visitante;
    private Integer actividad;

    public ActividadAsistenciaId() {}

    public ActividadAsistenciaId(String visitante, Integer actividad) {
        this.visitante = visitante;
        this.actividad = actividad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActividadAsistenciaId that)) return false;
        return Objects.equals(visitante, that.visitante) &&
                Objects.equals(actividad, that.actividad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(visitante, actividad);
    }
}
