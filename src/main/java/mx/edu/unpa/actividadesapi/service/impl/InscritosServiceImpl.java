package mx.edu.unpa.actividadesapi.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import mx.edu.unpa.actividadesapi.dto.response.InscritoDTO;
import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;
import mx.edu.unpa.actividadesapi.enums.TipoParticipante;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.model.InscripcionActividad;
import mx.edu.unpa.actividadesapi.model.InscripcionExterno;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.repository.InscripcionActividadRepository;
import mx.edu.unpa.actividadesapi.repository.InscripcionExternoRepository;
import mx.edu.unpa.actividadesapi.service.InscritosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.lowagie.text.Image;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;

/**
 * Servicio de consulta de inscritos (US-28).
 * Unifica inscripciones internas (alumno/docente) y externas (US-24)
 * en una sola lista, y genera PDF/CSV descargables.
 */
@Service
public class InscritosServiceImpl implements InscritosService {

    private static final Logger log = LoggerFactory.getLogger(InscritosServiceImpl.class);
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private InscripcionActividadRepository inscripcionRepository;

    @Autowired
    private InscripcionExternoRepository externoRepository;

    // ====================================================================
    // Lista unificada
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public ListaInscritosResponseDTO obtenerLista(Integer idActividad, Integer idSolicitante, String rolSolicitante) {
        log.info("Consultando lista de inscritos actividad={} solicitante={} rol={}",
                idActividad, idSolicitante, rolSolicitante);

        Actividad actividad = obtenerActividadValidada(idActividad, idSolicitante, rolSolicitante);

        List<InscritoDTO> inscritos = new ArrayList<>();

        for (InscripcionActividad i : inscripcionRepository
                .findByActividad_IdActividadOrderByFechaInscripcionAsc(idActividad)) {
            if (i.getAlumno() != null) {
                String nombreCompleto = i.getAlumno().getNombre() + " " + i.getAlumno().getApellidos();
                inscritos.add(new InscritoDTO(nombreCompleto, TipoParticipante.ALUMNO, i.getFechaInscripcion()));
            } else if (i.getUsuario() != null) {
                String nombreCompleto = i.getUsuario().getNombre() + " " + i.getUsuario().getApellidos();
                inscritos.add(new InscritoDTO(nombreCompleto, TipoParticipante.DOCENTE, i.getFechaInscripcion()));
            }
        }

        for (InscripcionExterno e : externoRepository
                .findByActividad_IdActividadOrderByFechaInscripcionAsc(idActividad)) {
            inscritos.add(new InscritoDTO(e.getNombre(), TipoParticipante.EXTERNO, e.getFechaInscripcion()));
        }

        inscritos.sort(Comparator.comparing(InscritoDTO::getFechaInscripcion));

        log.info("Lista de inscritos generada: actividad={} total={}", idActividad, inscritos.size());

        return new ListaInscritosResponseDTO(
                actividad.getIdActividad(),
                actividad.getNombre(),
                actividad.getFechaActividad(),
                actividad.getHoraInicio(),
                actividad.getHoraFin(),
                inscritos.size(),
                inscritos
        );
    }

    // ====================================================================
    // PDF
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdf(Integer idActividad, Integer idSolicitante, String rolSolicitante) {
        ListaInscritosResponseDTO lista = obtenerLista(idActividad, idSolicitante, rolSolicitante);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.LETTER, 40, 40, 60, 40);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            // Membrete
            Font fuenteTitulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font fuenteSub = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.GRAY);
            try {
                ClassPathResource logoResource = new ClassPathResource("assets/he-logo-unpa.png");
                try (InputStream is = logoResource.getInputStream()) {
                    byte[] logoBytes = is.readAllBytes();
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(140, 60);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    documento.add(logo);
                }
            } catch (Exception ex) {
                log.warn("No se pudo cargar el logo del membrete, se omite: {}", ex.getMessage());
                // No bloqueamos la generacion del PDF si falta el logo
            }
            Paragraph membrete = new Paragraph("Universidad del Papaloapan", fuenteTitulo);
            membrete.setAlignment(Element.ALIGN_CENTER);
            documento.add(membrete);

            Paragraph subtitulo = new Paragraph("Sistema de Gestion de Actividades - Lista de Inscritos", fuenteSub);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);

            // Datos del evento
            Font fuenteEvento = new Font(Font.HELVETICA, 12, Font.BOLD);
            documento.add(new Paragraph("Evento: " + lista.getNombreEvento(), fuenteEvento));
            documento.add(new Paragraph("Fecha: " + lista.getFechaEvento() +
                    "   Horario: " + lista.getHoraInicio() + " - " + lista.getHoraFin()));
            documento.add(new Paragraph("Total de inscritos: " + lista.getTotalInscritos()));
            documento.add(Chunk.NEWLINE);

            // Tabla
            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{3, 1.2f, 2});

            Font fuenteHeader = new Font(Font.HELVETICA, 10, Font.BOLD, java.awt.Color.WHITE);
            agregarCeldaHeader(tabla, "Nombre", fuenteHeader);
            agregarCeldaHeader(tabla, "Tipo", fuenteHeader);
            agregarCeldaHeader(tabla, "Fecha de inscripcion", fuenteHeader);

            Font fuenteCelda = new Font(Font.HELVETICA, 9, Font.NORMAL);
            for (InscritoDTO i : lista.getInscritos()) {
                tabla.addCell(new PdfPCell(new Phrase(i.getNombre(), fuenteCelda)));
                tabla.addCell(new PdfPCell(new Phrase(i.getTipoParticipante().name(), fuenteCelda)));
                tabla.addCell(new PdfPCell(new Phrase(i.getFechaInscripcion().format(FMT_FECHA), fuenteCelda)));
            }
            documento.add(tabla);

            documento.close();
            log.info("PDF de inscritos generado para actividad={}", idActividad);
            return salida.toByteArray();

        } catch (DocumentException ex) {
            log.error("Error al generar PDF de inscritos actividad={}: {}", idActividad, ex.getMessage());
            throw new BusinessException("No fue posible generar el PDF de inscritos.");
        }
    }

    private void agregarCeldaHeader(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(new java.awt.Color(113, 182, 167));
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    // ====================================================================
    // CSV
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public byte[] generarCsv(Integer idActividad, Integer idSolicitante, String rolSolicitante) {
        ListaInscritosResponseDTO lista = obtenerLista(idActividad, idSolicitante, rolSolicitante);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM para que Excel respete acentos
        sb.append("Universidad del Papaloapan - Lista de Inscritos\n");
        sb.append("Evento:,").append(escaparCsv(lista.getNombreEvento())).append('\n');
        sb.append("Fecha:,").append(lista.getFechaEvento()).append('\n');
        sb.append("Horario:,").append(lista.getHoraInicio()).append(" - ").append(lista.getHoraFin()).append('\n');
        sb.append("Total inscritos:,").append(lista.getTotalInscritos()).append("\n\n");
        sb.append("Nombre,Tipo de participante,Fecha de inscripcion\n");

        for (InscritoDTO i : lista.getInscritos()) {
            sb.append(escaparCsv(i.getNombre())).append(',')
                    .append(i.getTipoParticipante().name()).append(',')
                    .append(i.getFechaInscripcion().format(FMT_FECHA)).append('\n');
        }

        log.info("CSV de inscritos generado para actividad={}", idActividad);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escaparCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // ====================================================================
    // Validacion de acceso
    // ====================================================================
    private Actividad obtenerActividadValidada(Integer idActividad, Integer idSolicitante, String rolSolicitante) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        if (!Boolean.TRUE.equals(actividad.getRequiereInscripcion())) {
            log.warn("Consulta de inscritos rechazada: actividad={} no requiere inscripcion", idActividad);
            throw new BusinessException("Esta actividad no requiere inscripcion formal.");
        }

        boolean esAdmin = "ADMIN".equalsIgnoreCase(rolSolicitante);
        boolean esPropietario = actividad.getProfesor() != null
                && actividad.getProfesor().getIdUsuario().equals(idSolicitante);

        if (!esAdmin && !esPropietario) {
            log.warn("Acceso denegado a inscritos: actividad={} solicitante={} rol={}",
                    idActividad, idSolicitante, rolSolicitante);
            throw new BusinessException("No tienes permiso para consultar los inscritos de esta actividad.");
        }

        return actividad;
    }
}