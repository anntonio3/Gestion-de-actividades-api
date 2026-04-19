package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

}
