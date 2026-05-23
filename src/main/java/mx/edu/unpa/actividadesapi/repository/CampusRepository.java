package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Integer> {
}