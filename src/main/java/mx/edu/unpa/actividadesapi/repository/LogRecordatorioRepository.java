package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.LogRecordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * US-07: Repositorio para auditoría de envíos de recordatorios.
 */
@Repository
public interface LogRecordatorioRepository extends JpaRepository<LogRecordatorio, Integer> {

    /**
     * Busca el último envío para una actividad en las últimas 24 h.
     * Sirve para validar la restricción de un recordatorio por evento por día.
     */
    Optional<LogRecordatorio> findTopByActividad_IdActividadAndFechaEnvioAfterOrderByFechaEnvioDesc(
            Integer idActividad, LocalDateTime desde);
}