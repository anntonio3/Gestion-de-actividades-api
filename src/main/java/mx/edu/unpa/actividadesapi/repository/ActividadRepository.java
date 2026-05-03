package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    List<Actividad> findByIdProfesorOrderByFechaRegistroDesc(Integer idProfesor);
    // Filtradas por estado
    List<Actividad> findByIdProfesorAndEstadoOrderByFechaRegistroDesc(
            Integer idProfesor, EstadoActividad estado);
}