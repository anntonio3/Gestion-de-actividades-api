package mx.edu.unpa.actividadesapi.enums;

/**
 * Tipo de participante inscrito en una actividad.
 * Se infiere segun el origen del registro de inscripcion:
 * ALUMNO   -> viene de inscripciones_actividad con id_alumno
 * DOCENTE  -> viene de inscripciones_actividad con id_usuario
 * EXTERNO  -> viene de inscripciones_externo (US-24)
 */
public enum TipoParticipante {
    ALUMNO,
    DOCENTE,
    EXTERNO
}
