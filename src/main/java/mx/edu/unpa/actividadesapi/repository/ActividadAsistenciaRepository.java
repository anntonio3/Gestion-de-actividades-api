package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.ActividadAsistencia;
import mx.edu.unpa.actividadesapi.model.ActividadAsistenciaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActividadAsistenciaRepository
        extends JpaRepository<ActividadAsistencia, ActividadAsistenciaId> {

    Optional<ActividadAsistencia> findByVisitante_IdVisitanteAndActividad_IdActividad(
            String idVisitante, Integer idActividad);

    /**
     * Conteo agrupado por respuesta para una actividad.
     * Devuelve filas tipo [respuesta, total].
     */
    @Query("""
        SELECT a.respuesta, COUNT(a)
        FROM ActividadAsistencia a
        WHERE a.actividad.idActividad = :idActividad
        GROUP BY a.respuesta
    """)
    List<Object[]> contarPorRespuesta(@Param("idActividad") Integer idActividad);

    /**
     * Conteo para un lote de actividades. Usado en el calendario publico
     * para evitar N+1: 1 query por ventana de eventos en lugar de 1 por card.
     */
    @Query("""
        SELECT a.actividad.idActividad, a.respuesta, COUNT(a)
        FROM ActividadAsistencia a
        WHERE a.actividad.idActividad IN :ids
        GROUP BY a.actividad.idActividad, a.respuesta
    """)
    List<Object[]> contarPorRespuestaEnLote(@Param("ids") List<Integer> ids);
}
