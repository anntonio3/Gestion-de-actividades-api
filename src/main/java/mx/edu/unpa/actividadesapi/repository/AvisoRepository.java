package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Integer> {

    // US-18/US-20: Listado publico ordenado por fecha del evento ASC (proximos primero)
    List<Aviso> findByActivoTrueOrderByFechaEventoAscHoraEventoAsc();

    // US-18: Filtro por fecha exacta del evento
    List<Aviso> findByActivoTrueAndFechaEventoOrderByHoraEventoAsc(LocalDate fechaEvento);

    // US-18: Filtro por rango de fechas (utiles cuando el front quiera "esta semana", etc.)
    List<Aviso> findByActivoTrueAndFechaEventoBetweenOrderByFechaEventoAscHoraEventoAsc(
            LocalDate desde, LocalDate hasta);

    // US-19: Avisos del profesor (lista de "mis avisos" para editar)
    List<Aviso> findByProfesor_IdUsuarioAndActivoTrueOrderByFechaPublicacionDesc(
            Integer idProfesor);
}
