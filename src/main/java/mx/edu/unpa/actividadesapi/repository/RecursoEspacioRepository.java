package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Recurso;
import mx.edu.unpa.actividadesapi.model.RecursoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecursoEspacioRepository extends JpaRepository<RecursoEspacio, Long> {
    List<RecursoEspacio> findByActivoTrue();
}
