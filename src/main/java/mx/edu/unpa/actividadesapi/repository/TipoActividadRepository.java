package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.TipoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoActividadRepository extends JpaRepository<TipoActividad, Integer> {

    List<TipoActividad> findByCategoriaIdCategoria(Integer idCategoria);

}

