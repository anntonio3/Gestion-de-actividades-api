package mx.edu.unpa.actividadesapi.repository;


import mx.edu.unpa.actividadesapi.model.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRecursoRepository extends JpaRepository<TipoRecurso, Integer> {
}
