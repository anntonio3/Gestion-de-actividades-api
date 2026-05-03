package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    // US-03: actividades del profesor
    List<Actividad> findByProfesorIdUsuario(Integer idProfesor);

    // Filtra por profesor y estado (útil para US-04 más adelante)
    List<Actividad> findByProfesorIdUsuarioAndEstado(Integer idProfesor, EstadoActividad estado);
}
