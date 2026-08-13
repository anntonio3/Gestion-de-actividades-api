package mx.edu.unpa.actividadesapi.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * US-07: Servicio para el envío masivo de recordatorios.
 */
public interface RecordatorioService {

    /**
     * Envía recordatorios por correo a todos los inscritos de una actividad.
     * El progreso se transmite en tiempo real mediante SSE.
     */
    void enviarRecordatorios(Integer idActividad, Integer idUsuario, SseEmitter emitter);
}