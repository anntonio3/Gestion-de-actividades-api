package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

@Data
public class InmobiliarioResponse {

    // ── De la tabla base: recursos ───────────────────────────────────
    private Integer idRecurso;
    private String  nombre;
    private String  descripcion;
    private Boolean activo;

    // ── De recursos_mobiliario (US-14) ───────────────────────────────
    private String  codigo;
    private String  numInventario;
    private Integer existencias;
    private Integer disponibles;
    private String  fotoUrl;   // URL completa servida por /uploads/**
    private String  nota;

    public InmobiliarioResponse(Integer idRecurso, String nombre, String descripcion,
                                Boolean activo, String codigo, String numInventario,
                                Integer existencias, Integer disponibles,
                                String fotoUrl, String nota) {
        this.idRecurso     = idRecurso;
        this.nombre        = nombre;
        this.descripcion   = descripcion;
        this.activo        = activo;
        this.codigo        = codigo;
        this.numInventario = numInventario;
        this.existencias   = existencias;
        this.disponibles   = disponibles;
        this.fotoUrl       = fotoUrl;
        this.nota          = nota;
    }
}