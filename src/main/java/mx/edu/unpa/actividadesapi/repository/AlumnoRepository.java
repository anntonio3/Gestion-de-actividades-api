package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    Optional<Alumno> findByMatricula(String matricula);

    Optional<Alumno> findByCorreo(String correo);

    boolean existsByMatricula(String matricula);

    boolean existsByCorreo(String correo);
}
