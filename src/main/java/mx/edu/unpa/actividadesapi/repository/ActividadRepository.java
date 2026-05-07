package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
    // Todas las aprobadas
    List<Actividad> findByEstado(EstadoActividad estado);

    // Filtrar por tipo además del estado
    List<Actividad> findByEstadoAndTipo_IdTipo(EstadoActividad estado, Integer idTipo);

    // NUEVO: filtrar por categoría (a través de tipo → categoria)
    List<Actividad> findByEstadoAndTipo_Categoria_IdCategoria(
            EstadoActividad estado, Integer idCategoria);

    List<Actividad> findByProfesor_IdUsuarioOrderByFechaRegistroDesc(Integer idProfesor);
    List<Actividad> findByProfesor_IdUsuarioAndEstadoOrderByFechaRegistroDesc(Integer idProfesor, EstadoActividad estado);

    // US-07: Listado para el panel de vicerrectoria
    // Orden: PENDIENTES primero, luego por fecha de registro descendente.
    List<Actividad> findAllByOrderByFechaRegistroDesc();

    /**
     * US-10: Detecta espacios ocupados por OTRAS actividades APROBADAS
     * en el mismo dia y rango horario que se solapa.
     * Excluye la actividad indicada para no auto-detectarse.
     */
    @Query("""
        SELECT DISTINCT ar.recurso.idRecurso
        FROM ActividadRecurso ar
        WHERE ar.recurso.idRecurso IN (
                SELECT e.idRecurso FROM RecursoEspacio e
              )
          AND ar.actividad.estado = mx.edu.unpa.actividadesapi.enums.EstadoActividad.APROBADA
          AND ar.actividad.idActividad <> :idActividadExcluir
          AND ar.actividad.fechaActividad = :fecha
          AND ar.actividad.horaInicio < :horaFin
          AND ar.actividad.horaFin    > :horaInicio
    """)
    List<Integer> findEspaciosOcupadosExcluyendo(
            @Param("idActividadExcluir") Integer idActividadExcluir,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    /**
     * US-10: Suma de unidades de mobiliario ya asignadas a OTRAS actividades
     * APROBADAS en el mismo dia y rango horario.
     */
    @Query("""
        SELECT ar.recurso.idRecurso, COALESCE(SUM(ar.cantidadRequerida), 0)
        FROM ActividadRecurso ar
        WHERE ar.recurso.idRecurso IN (
                SELECT m.idRecurso FROM RecursoMobiliario m
              )
          AND ar.actividad.estado = mx.edu.unpa.actividadesapi.enums.EstadoActividad.APROBADA
          AND ar.actividad.idActividad <> :idActividadExcluir
          AND ar.actividad.fechaActividad = :fecha
          AND ar.actividad.horaInicio < :horaFin
          AND ar.actividad.horaFin    > :horaInicio
        GROUP BY ar.recurso.idRecurso
    """)
    List<Object[]> findMobiliarioOcupadoExcluyendo(
            @Param("idActividadExcluir") Integer idActividadExcluir,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

}