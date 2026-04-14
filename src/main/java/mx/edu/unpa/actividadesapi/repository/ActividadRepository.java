package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
    List<Actividad> findByIdProfesorOrderByFechaRegistroDesc(Integer idProfesor);
    // Filtradas por estado
    List<Actividad> findByIdProfesorAndEstadoOrderByFechaRegistroDesc(
            Integer idProfesor, Actividad.EstadoActividad estado);
}