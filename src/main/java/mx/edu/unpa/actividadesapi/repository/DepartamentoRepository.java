package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {

}
