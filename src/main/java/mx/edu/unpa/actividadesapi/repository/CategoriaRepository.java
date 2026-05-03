package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    // Encuentra todas las categorías activas para los chips del frontend
}
