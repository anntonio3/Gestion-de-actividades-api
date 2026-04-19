package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.TipoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoActividadRepository extends JpaRepository<TipoActividad, Integer> {
}
