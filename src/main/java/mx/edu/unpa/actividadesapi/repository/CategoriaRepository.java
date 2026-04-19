package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {}


