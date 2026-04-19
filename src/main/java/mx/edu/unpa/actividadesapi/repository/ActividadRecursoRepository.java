package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.ActividadRecurso;
import mx.edu.unpa.actividadesapi.model.ActividadRecursoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRecursoRepository extends JpaRepository<ActividadRecurso, ActividadRecursoId> {
    List<ActividadRecurso> findByActividadIdActividad(Integer idActividad);
}
