package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.ActividadOrganizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadOrganizadorRepository extends JpaRepository<ActividadOrganizador, Integer> {
    List<ActividadOrganizador> findByActividadIdActividad(Integer idActividad);
}