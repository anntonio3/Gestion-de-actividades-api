package mx.edu.unpa.actividadesapi.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import mx.edu.unpa.actividadesapi.dto.response.ActividadInscripcionResumenDTO;
import mx.edu.unpa.actividadesapi.dto.response.InscritoDTO;
import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de consulta de inscritos (US-28).
 * Unifica inscripciones internas (alumno/docente) y externas (US-24)
 * en una sola lista, y genera PDF/CSV descargables.
 *
 * Fusion de dos implementaciones (Vianey/Bere): se conserva la
 * validacion de requiereInscripcion y el esquema de autorizacion
 * basado en el JWT (esAdmin ya viene resuelto desde el controller),
 * y se incorporan del otro desarrollador el identificador de contacto,
 * la numeracion de fila y el diseño de tabla con colores alternos.
 */
@Service
public class InscritosServiceImpl implements InscritosService {

    private static final Logger log = LoggerFactory.getLogger(InscritosServiceImpl.class);
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_FECHA_LARGA =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es"));

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
    public ListaInscritosResponseDTO obtenerLista(Integer idActividad, Integer idSolicitante, boolean esAdmin) {
        log.info("Consultando lista de inscritos actividad={} solicitante={} esAdmin={}",
                idActividad, idSolicitante, esAdmin);

        Actividad actividad = obtenerActividadValidada(idActividad, idSolicitante, esAdmin);
        List<InscritoDTO> inscritos = construirLista(actividad);

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
    public byte[] generarPdf(Integer idActividad, Integer idSolicitante, boolean esAdmin) {
        ListaInscritosResponseDTO lista = obtenerLista(idActividad, idSolicitante, esAdmin);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.LETTER, 40, 40, 60, 40);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            // ── Membrete con logo ────────────────────────────────────────────
            Font fuenteTitulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font fuenteSub = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.GRAY);

            try {
                ClassPathResource logoResource = new ClassPathResource("assets/he-logo-unpa.png");
                try (InputStream is = logoResource.getInputStream()) {
                    Image logo = Image.getInstance(is.readAllBytes());
                    logo.scaleToFit(140, 60);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    documento.add(logo);
                }
            } catch (Exception ex) {
                log.warn("No se pudo cargar el logo del membrete, se omite: {}", ex.getMessage());
            }

            Paragraph membrete = new Paragraph("Universidad del Papaloapan", fuenteTitulo);
            membrete.setAlignment(Element.ALIGN_CENTER);
            documento.add(membrete);

            Paragraph subtitulo = new Paragraph("Gestion de Eventos Universitarios - Lista de Inscritos", fuenteSub);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(16);
            documento.add(subtitulo);

            // ── Datos del evento ──────────────────────────────────────────────
            Font fuenteEventoLabel = new Font(Font.HELVETICA, 10, Font.BOLD, java.awt.Color.GRAY);
            Font fuenteEventoNombre = new Font(Font.HELVETICA, 15, Font.BOLD);
            Font fuenteEventoDatos = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.GRAY);

            documento.add(new Paragraph("EVENTO", fuenteEventoLabel));
            documento.add(new Paragraph(lista.getNombreEvento(), fuenteEventoNombre));

            String fechaLarga = lista.getFechaEvento().format(FMT_FECHA_LARGA);
            Paragraph datosEvento = new Paragraph(
                    "Fecha: " + fechaLarga + "   |   Hora: " + lista.getHoraInicio() + " - " + lista.getHoraFin(),
                    fuenteEventoDatos);
            datosEvento.setSpacingAfter(14);
            documento.add(datosEvento);

            // ── Tabla ─────────────────────────────────────────────────────────
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{0.6f, 3f, 1.6f, 2.2f, 1.8f});

            Font fuenteHeader = new Font(Font.HELVETICA, 9, Font.BOLD, java.awt.Color.WHITE);
            for (String header : new String[]{"#", "Nombre", "Tipo", "Identificador", "Inscrito el"}) {
                agregarCeldaHeader(tabla, header, fuenteHeader);
            }

            Font fuenteCelda = new Font(Font.HELVETICA, 9, Font.NORMAL);
            java.awt.Color colorAlterno = new java.awt.Color(240, 248, 244);
            boolean alterna = false;

            for (InscritoDTO i : lista.getInscritos()) {
                java.awt.Color fondo = alterna ? colorAlterno : java.awt.Color.WHITE;
                tabla.addCell(celda(String.valueOf(i.getNumero()), fuenteCelda, fondo));
                tabla.addCell(celda(i.getNombre(), fuenteCelda, fondo));
                tabla.addCell(celda(etiquetaTipo(i.getTipoParticipante()), fuenteCelda, fondo));
                tabla.addCell(celda(i.getIdentificador() != null ? i.getIdentificador() : "—", fuenteCelda, fondo));
                tabla.addCell(celda(i.getFechaInscripcion().format(FMT_FECHA), fuenteCelda, fondo));
                alterna = !alterna;
            }

            if (lista.getInscritos().isEmpty()) {
                PdfPCell vacia = new PdfPCell(new Phrase("Sin inscritos aún.", fuenteCelda));
                vacia.setColspan(5);
                vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                vacia.setPadding(8);
                tabla.addCell(vacia);
            }

            documento.add(tabla);

            // ── Pie ───────────────────────────────────────────────────────────
            Font fuenteTotal = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font fuenteGenerado = new Font(Font.HELVETICA, 8, Font.NORMAL, java.awt.Color.GRAY);

            Paragraph total = new Paragraph("\nTotal de inscritos: " + lista.getTotalInscritos(), fuenteTotal);
            total.setSpacingBefore(10);
            documento.add(total);

            String generadoEl = java.time.LocalDateTime.now().format(FMT_FECHA);
            documento.add(new Paragraph("Generado el " + generadoEl, fuenteGenerado));

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
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private PdfPCell celda(String texto, Font fuente, java.awt.Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "—", fuente));
        celda.setBackgroundColor(fondo);
        celda.setPadding(5);
        return celda;
    }

    // ====================================================================
    // CSV
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public byte[] generarCsv(Integer idActividad, Integer idSolicitante, boolean esAdmin) {
        ListaInscritosResponseDTO lista = obtenerLista(idActividad, idSolicitante, esAdmin);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM para que Excel respete acentos
        sb.append("Universidad del Papaloapan - Lista de Inscritos\n");
        sb.append("Evento:,").append(escaparCsv(lista.getNombreEvento())).append('\n');
        sb.append("Fecha:,").append(lista.getFechaEvento().format(FMT_FECHA_LARGA)).append('\n');
        sb.append("Horario:,").append(lista.getHoraInicio()).append(" - ").append(lista.getHoraFin()).append('\n');
        sb.append("Total inscritos:,").append(lista.getTotalInscritos()).append("\n\n");
        sb.append("#,Nombre,Tipo,Identificador,Fecha de inscripcion\n");

        for (InscritoDTO i : lista.getInscritos()) {
            sb.append(i.getNumero()).append(',')
                    .append(escaparCsv(i.getNombre())).append(',')
                    .append(escaparCsv(etiquetaTipo(i.getTipoParticipante()))).append(',')
                    .append(escaparCsv(i.getIdentificador() != null ? i.getIdentificador() : "—")).append(',')
                    .append(i.getFechaInscripcion().format(FMT_FECHA)).append('\n');
        }

        log.info("CSV de inscritos generado para actividad={}", idActividad);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ====================================================================
    // Listado para ADMIN: todas las actividades aprobadas con inscripcion
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public List<ActividadInscripcionResumenDTO> listarActividadesConInscripcion() {
        log.info("Listando actividades aprobadas con inscripcion para dashboard ADMIN");

        List<Actividad> actividades = actividadRepository
                .findByEstadoAndRequiereInscripcionTrueOrderByFechaActividadAsc(EstadoActividad.APROBADA);

        List<ActividadInscripcionResumenDTO> resumen = actividades.stream()
                .map(a -> {
                    int internos = inscripcionRepository.countByActividad_IdActividad(a.getIdActividad());
                    int externos = externoRepository.countByActividad_IdActividad(a.getIdActividad());
                    String nombreProfesor = a.getProfesor() != null
                            ? a.getProfesor().getNombre() + " " + a.getProfesor().getApellidos()
                            : "—";

                    return new ActividadInscripcionResumenDTO(
                            a.getIdActividad(),
                            a.getNombre(),
                            nombreProfesor,
                            a.getFechaActividad(),
                            a.getHoraInicio(),
                            a.getHoraFin(),
                            internos + externos
                    );
                })
                .toList();

        log.info("Dashboard ADMIN: {} actividades con inscripcion encontradas", resumen.size());
        return resumen;
    }

    private String escaparCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // ====================================================================
    // Construccion de la lista unificada (interno + externo)
    // ====================================================================
    private List<InscritoDTO> construirLista(Actividad actividad) {
        List<InscritoDTO> lista = new ArrayList<>();
        int numero = 1;

        for (InscripcionActividad i : inscripcionRepository
                .findByActividad_IdActividadOrderByFechaInscripcionAsc(actividad.getIdActividad())) {

            if (i.getAlumno() != null) {
                String nombreCompleto = i.getAlumno().getNombre() + " " + i.getAlumno().getApellidos();
                lista.add(new InscritoDTO(numero++, nombreCompleto, TipoParticipante.ALUMNO,
                        i.getAlumno().getMatricula(), i.getFechaInscripcion()));
            } else if (i.getUsuario() != null) {
                String nombreCompleto = i.getUsuario().getNombre() + " " + i.getUsuario().getApellidos();
                lista.add(new InscritoDTO(numero++, nombreCompleto, TipoParticipante.DOCENTE,
                        i.getUsuario().getCorreo(), i.getFechaInscripcion()));
            }
        }

        for (InscripcionExterno e : externoRepository
                .findByActividad_IdActividadOrderByFechaInscripcionAsc(actividad.getIdActividad())) {
            lista.add(new InscritoDTO(numero++, e.getNombre(), TipoParticipante.EXTERNO,
                    e.getCorreo() != null ? e.getCorreo() : "—", e.getFechaInscripcion()));
        }

        return lista;
    }

    private String etiquetaTipo(TipoParticipante tipo) {
        return switch (tipo) {
            case ALUMNO -> "Alumno";
            case DOCENTE -> "Docente/Staff";
            case EXTERNO -> "Externo";
        };
    }

    // ====================================================================
    // Validacion de acceso
    // ====================================================================
    private Actividad obtenerActividadValidada(Integer idActividad, Integer idSolicitante, boolean esAdmin) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        if (!Boolean.TRUE.equals(actividad.getRequiereInscripcion())) {
            log.warn("Consulta de inscritos rechazada: actividad={} no requiere inscripcion", idActividad);
            throw new BusinessException("Esta actividad no requiere inscripcion formal.");
        }

        boolean esPropietario = actividad.getProfesor() != null
                && actividad.getProfesor().getIdUsuario().equals(idSolicitante);

        if (!esAdmin && !esPropietario) {
            log.warn("Acceso denegado a inscritos: actividad={} solicitante={}", idActividad, idSolicitante);
            throw new BusinessException("No tienes permiso para consultar los inscritos de esta actividad.");
        }

        return actividad;
    }
}