package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, String> {
}
