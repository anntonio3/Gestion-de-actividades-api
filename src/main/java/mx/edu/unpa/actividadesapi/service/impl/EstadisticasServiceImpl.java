package mx.edu.unpa.actividadesapi.service.impl;


import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaCarreraDTO;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaCampusDTO;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaMesDTO;
import mx.edu.unpa.actividadesapi.repository.EstadisticasRepository;
import mx.edu.unpa.actividadesapi.service.EstadisticasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadisticasServiceImpl implements EstadisticasService {

    private final EstadisticasRepository estadisticasRepository;

    /** US-21: Total general por mes y año. */
    @Override
    public List<EstadisticaMesDTO> obtenerGeneral() {
        return estadisticasRepository.contarPorMesAnioGeneral()
                .stream()
                .map(row -> new EstadisticaMesDTO(
                        toInt(row[0]),   // anio
                        toInt(row[1]),   // mes
                        toLong(row[2])   // cantidad
                ))
                .toList();
    }

    /** US-22: Desglosado por campus (departamento). */
    @Override
    public List<EstadisticaCampusDTO> obtenerPorCampus() {
        return estadisticasRepository.contarPorMesAnioCampus()
                .stream()
                .map(row -> new EstadisticaCampusDTO(
                        toInt(row[0]),          // anio
                        toInt(row[1]),          // mes
                        toInt(row[2]),          // idDepartamento
                        String.valueOf(row[3]), // nombreDepartamento
                        toLong(row[4])          // cantidad
                ))
                .toList();
    }

    /** US-23: Desglosado por carrera. */
    @Override
    public List<EstadisticaCarreraDTO> obtenerPorCarrera() {
        return estadisticasRepository.contarPorMesAnioCarrera()
                .stream()
                .map(row -> new EstadisticaCarreraDTO(
                        toInt(row[0]),          // anio
                        toInt(row[1]),          // mes
                        toInt(row[2]),          // idCarrera
                        String.valueOf(row[3]), // nombreCarrera
                        toLong(row[4])          // cantidad
                ))
                .toList();
    }

    // ── helpers de conversión segura ─────────────────────────

    private Integer toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number n) return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
