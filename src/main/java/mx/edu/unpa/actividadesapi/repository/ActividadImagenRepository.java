package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.ActividadImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActividadImagenRepository extends JpaRepository<ActividadImagen, Integer> {
    List<ActividadImagen> findByActividadIdActividad(Integer idActividad);
    Optional<ActividadImagen> findByActividadIdActividadAndEsPortadaTrue(Integer idActividad);
}
