package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.repository.InscripcionActividadRepository;
import mx.edu.unpa.actividadesapi.repository.InscripcionExternoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean auxiliar separado para recolectar correos dentro de una transacción propia.
 * Necesario porque @Transactional no funciona en llamadas self-invocation (this.metodo()).
 */
@Service
@RequiredArgsConstructor
public class RecordatorioCorreosHelper {

    private final InscripcionActividadRepository inscripcionRepository;
    private final InscripcionExternoRepository   externoRepository;

    @Transactional(readOnly = true)
    public List<String> recolectarCorreos(Integer idActividad) {
        List<String> correos = new ArrayList<>();

        inscripcionRepository.findByActividad_IdActividad(idActividad).forEach(ins -> {
            if (ins.getUsuario() != null && ins.getUsuario().getCorreo() != null)
                correos.add(ins.getUsuario().getCorreo());
            if (ins.getAlumno() != null && ins.getAlumno().getCorreo() != null)
                correos.add(ins.getAlumno().getCorreo());
        });

        externoRepository.findByActividad_IdActividad(idActividad).forEach(ext -> {
            if (ext.getCorreo() != null && !ext.getCorreo().isBlank())
                correos.add(ext.getCorreo());
        });

        return correos;
    }
}