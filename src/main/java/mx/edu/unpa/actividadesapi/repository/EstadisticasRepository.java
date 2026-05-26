package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Consultas de estadísticas para US-21, US-22 y US-23.
 */
@Repository
public interface EstadisticasRepository extends JpaRepository<Actividad, Integer> {

    /**
     * US-21: Total de actividades APROBADAS agrupadas por mes y año (general).
     * Retorna Object[]: [anio(Integer), mes(Integer), cantidad(Long)]
     */
    @Query(value = """
        SELECT
            YEAR(a.fecha_actividad)  AS anio,
            MONTH(a.fecha_actividad) AS mes,
            COUNT(*)                 AS cantidad
        FROM actividades a
        WHERE a.estado = 'APROBADA'
        GROUP BY YEAR(a.fecha_actividad), MONTH(a.fecha_actividad)
        ORDER BY anio ASC, mes ASC
        """, nativeQuery = true)
    List<Object[]> contarPorMesAnioGeneral();

    /**
     * US-22: Actividades APROBADAS por mes, año y campus.
     * CORRECCIÓN: la tabla `actividades` ya tiene id_campus directamente;
     * se une con la tabla `campus` para obtener el nombre.
     * Retorna Object[]: [anio, mes, idCampus, nombreCampus, cantidad]
     */
    @Query(value = """
        SELECT
            YEAR(a.fecha_actividad)  AS anio,
            MONTH(a.fecha_actividad) AS mes,
            c.id_campus              AS idDepartamento,
            c.nombre                 AS nombreDepartamento,
            COUNT(*)                 AS cantidad
        FROM actividades a
        INNER JOIN campus c ON c.id_campus = a.id_campus
        WHERE a.estado = 'APROBADA'
        GROUP BY YEAR(a.fecha_actividad), MONTH(a.fecha_actividad),
                 c.id_campus, c.nombre
        ORDER BY anio ASC, mes ASC, c.nombre ASC
        """, nativeQuery = true)
    List<Object[]> contarPorMesAnioCampus();

    /**
     * US-23: Actividades APROBADAS por mes, año y carrera.
     * Se une con actividad_organizadores → carreras para el desglose por carrera.
     * Retorna Object[]: [anio, mes, idCarrera, nombreCarrera, cantidad]
     */
    @Query(value = """
        SELECT
            YEAR(a.fecha_actividad)  AS anio,
            MONTH(a.fecha_actividad) AS mes,
            c.id_carrera,
            c.nombre                 AS nombreCarrera,
            COUNT(*)                 AS cantidad
        FROM actividades a
        INNER JOIN actividad_organizadores ao
            ON ao.id_actividad = a.id_actividad
        INNER JOIN carreras c
            ON c.id_carrera = ao.id_carrera
        WHERE a.estado = 'APROBADA'
          AND ao.id_carrera IS NOT NULL
        GROUP BY YEAR(a.fecha_actividad), MONTH(a.fecha_actividad),
                 c.id_carrera, c.nombre
        ORDER BY anio ASC, mes ASC, c.nombre ASC
        """, nativeQuery = true)
    List<Object[]> contarPorMesAnioCarrera();
}
