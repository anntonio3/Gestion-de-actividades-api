package mx.edu.unpa.actividadesapi.repository;


import mx.edu.unpa.actividadesapi.model.usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<usuarios, Integer> {
    Optional<usuarios> findByCorreo(String correo);
}
