package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.MapaPunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapaPuntoRepository extends JpaRepository<MapaPunto, Integer> {
}