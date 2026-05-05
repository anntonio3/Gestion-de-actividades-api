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
import java.util.Optional;

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

    // ========================================================
    //  NUEVO US-14: queries para administración de espacios
    // ========================================================

    /**
     * Lista todos los espacios anclados a un punto del mapa,
     * con punto y datos del recurso cargados (evita N+1).
     */
    @Query("""
        SELECT e FROM RecursoEspacio e
        LEFT JOIN FETCH e.punto
        WHERE e.punto IS NOT NULL
    """)
    List<RecursoEspacio> findAllConPunto();

    /**
     * Verifica si un punto del mapa ya está ocupado por otro espacio.
     * Si idEspacioActual es null, valida para creación (cualquier ocupado falla).
     * Si trae valor, valida para edición (excluye el espacio actual).
     */
    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END
        FROM RecursoEspacio e
        WHERE e.punto.idPunto = :idPunto
          AND (:idEspacioActual IS NULL OR e.idRecurso <> :idEspacioActual)
    """)
    boolean isPuntoOcupado(@Param("idPunto") Integer idPunto,
                           @Param("idEspacioActual") Integer idEspacioActual);

    /**
     * Carga un espacio con punto y equipamiento para el detalle.
     */
    @Query("""
        SELECT DISTINCT e FROM RecursoEspacio e
        LEFT JOIN FETCH e.punto
        LEFT JOIN FETCH e.equipamiento eq
        LEFT JOIN FETCH eq.recurso
        WHERE e.idRecurso = :id
    """)
    Optional<RecursoEspacio> findByIdConDetalle(@Param("id") Integer id);

}
