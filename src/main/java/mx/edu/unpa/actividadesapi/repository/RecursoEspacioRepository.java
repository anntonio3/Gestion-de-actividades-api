package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Recurso;
import mx.edu.unpa.actividadesapi.model.RecursoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RecursoEspacioRepository extends JpaRepository<RecursoEspacio, Integer> {
    List<RecursoEspacio> findByActivoTrue();

    /**
     * Retorna los IDs de espacios ocupados en el rango dado
     * por actividades con estado APROBADA.
     */
    @Query("""
        SELECT DISTINCT ar.recurso.idRecurso
        FROM ActividadRecurso ar
        WHERE ar.recurso.idRecurso IN (
                SELECT e.idRecurso FROM RecursoEspacio e
              )
          AND ar.actividad.estado = mx.edu.unpa.actividadesapi.enums.EstadoActividad.APROBADA
          AND ar.actividad.fechaActividad = :fecha
          AND ar.actividad.horaInicio < :horaFin
          AND ar.actividad.horaFin    > :horaInicio
    """)
    List<Integer> findIdsOcupados(
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

}
