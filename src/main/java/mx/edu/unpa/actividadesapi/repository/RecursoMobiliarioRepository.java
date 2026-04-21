package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.RecursoMobiliario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RecursoMobiliarioRepository extends JpaRepository<RecursoMobiliario, Integer> {
    List<RecursoMobiliario> findByActivoTrue();

    /**
     * Por cada mobiliario, retorna el total ya solicitado
     * en el rango de horas por actividades APROBADAS.
     * Devuelve [idRecurso, cantidadOcupada].
     */
    @Query("""
        SELECT ar.recurso.idRecurso, COALESCE(SUM(ar.cantidadRequerida), 0)
        FROM ActividadRecurso ar
        WHERE ar.recurso.idRecurso IN (
                SELECT m.idRecurso FROM RecursoMobiliario m
              )
          AND ar.actividad.estado = mx.edu.unpa.actividadesapi.enums.EstadoActividad.APROBADA
          AND ar.actividad.fechaActividad = :fecha
          AND ar.actividad.horaInicio < :horaFin
          AND ar.actividad.horaFin    > :horaInicio
        GROUP BY ar.recurso.idRecurso
    """)
    List<Object[]> findCantidadesOcupadas(
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

}
