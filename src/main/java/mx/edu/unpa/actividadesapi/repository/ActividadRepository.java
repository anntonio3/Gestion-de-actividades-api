package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    // Todas las aprobadas
    List<Actividad> findByEstado(Actividad.EstadoActividad estado);

    // Filtrar por tipo además del estado
    List<Actividad> findByEstadoAndTipo_IdTipo(Actividad.EstadoActividad estado, Integer idTipo);

    // NUEVO: filtrar por categoría (a través de tipo → categoria)
    List<Actividad> findByEstadoAndTipo_Categoria_IdCategoria(
            Actividad.EstadoActividad estado, Integer idCategoria);
}
