package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.InscripcionExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

/**
 * Repositorio para inscripciones de personas externas (US-24).
 */
@Repository
public interface InscripcionExternoRepository
        extends JpaRepository<InscripcionExterno, Integer> {

    List<InscripcionExterno> findByActividad_IdActividad(Integer idActividad);

    // Total de externos inscritos en una actividad
    int countByActividad_IdActividad(Integer idActividad);

    // Verifica duplicado por correo (cuando viene informado)
    Optional<InscripcionExterno> findByActividad_IdActividadAndCorreo(
            Integer idActividad, String correo);

    // Verifica duplicado por visitante (anti-duplicado al inscribirse)
    boolean existsByActividad_IdActividadAndVisitante_IdVisitante(
            Integer idActividad, String idVisitante);

    // Busca la inscripcion de un visitante en una actividad (para cancelar)
    Optional<InscripcionExterno> findByActividad_IdActividadAndVisitante_IdVisitante(
            Integer idActividad, String idVisitante);
}
