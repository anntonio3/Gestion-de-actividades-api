package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Integer> {
    List<Recurso> findByActivoTrue();
}