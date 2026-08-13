package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.InscripcionActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionActividadRepository
        extends JpaRepository<InscripcionActividad, Integer> {

    // Buscar inscripcion existente de un usuario en una actividad
    Optional<InscripcionActividad> findByActividad_IdActividadAndUsuario_IdUsuario(
            Integer idActividad, Integer idUsuario);

    // Buscar inscripcion existente de un alumno en una actividad
    Optional<InscripcionActividad> findByActividad_IdActividadAndAlumno_IdAlumno(
            Integer idActividad, Integer idAlumno);

    // Contar inscritos en una actividad
    int countByActividad_IdActividad(Integer idActividad);

    // Listar inscripciones de un usuario institucional
    List<InscripcionActividad> findByUsuario_IdUsuarioOrderByFechaInscripcionDesc(Integer idUsuario);

    // Listar inscripciones de un alumno
    List<InscripcionActividad> findByAlumno_IdAlumnoOrderByFechaInscripcionDesc(Integer idAlumno);

    // Listar inscripciones de una actividad, ordenadas por fecha (US-28)
    List<InscripcionActividad> findByActividad_IdActividadOrderByFechaInscripcionAsc(Integer idActividad);

    /**
     * Detecta conflicto de horario para un usuario institucional.
     * Devuelve inscripciones en actividades aprobadas que se solapan
     * con el horario dado, excluyendo la actividad objetivo.
     */
    @Query("""
        SELECT i FROM InscripcionActividad i
        WHERE i.usuario.idUsuario = :idUsuario
          AND i.actividad.idActividad <> :idActividadObjetivo
          AND i.actividad.estado = mx.edu.unpa.actividadesapi.enums.EstadoActividad.APROBADA
          AND i.actividad.fechaActividad = :fecha
          AND i.actividad.horaInicio < :horaFin
          AND i.actividad.horaFin    > :horaInicio
    """)
    List<InscripcionActividad> findConflictosUsuario(
            @Param("idUsuario")          Integer idUsuario,
            @Param("idActividadObjetivo") Integer idActividadObjetivo,
            @Param("fecha")              LocalDate fecha,
            @Param("horaInicio")         LocalTime horaInicio,
            @Param("horaFin")            LocalTime horaFin);

    /**
     * Detecta conflicto de horario para un alumno.
     */
    @Query("""
        SELECT i FROM InscripcionActividad i
        WHERE i.alumno.idAlumno = :idAlumno
          AND i.actividad.idActividad <> :idActividadObjetivo
          AND i.actividad.estado = mx.edu.unpa.actividadesapi.enums.EstadoActividad.APROBADA
          AND i.actividad.fechaActividad = :fecha
          AND i.actividad.horaInicio < :horaFin
          AND i.actividad.horaFin    > :horaInicio
    """)
    List<InscripcionActividad> findConflictosAlumno(
            @Param("idAlumno")           Integer idAlumno,
            @Param("idActividadObjetivo") Integer idActividadObjetivo,
            @Param("fecha")              LocalDate fecha,
            @Param("horaInicio")         LocalTime horaInicio,
            @Param("horaFin")            LocalTime horaFin);
}
