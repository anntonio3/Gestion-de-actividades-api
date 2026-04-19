package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.RecursoMobiliario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecursoMobiliarioRepository extends JpaRepository<RecursoMobiliario, Long> {
    List<RecursoMobiliario> findByActivoTrue();
}
