package mx.edu.unpa.actividadesapi.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.response.InscritoListaItemResponse;
import mx.edu.unpa.actividadesapi.enums.Rol;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.InscritosService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscritosServiceImpl implements InscritosService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ActividadRepository            actividadRepository;
    private final InscripcionActividadRepository inscripcionRepository;
    private final InscripcionExternoRepository   externoRepository;
    private final UsuarioRepository              usuarioRepository;

    // ── Lista ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InscritoListaItemResponse> obtenerLista(Integer idActividad, Integer idSolicitante) {
        Actividad actividad = validarAcceso(idActividad, idSolicitante);
        return construirLista(actividad);
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdf(Integer idActividad, Integer idSolicitante) {
        Actividad actividad = validarAcceso(idActividad, idSolicitante);
        List<InscritoListaItemResponse> lista = construirLista(actividad);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter   writer   = new PdfWriter(baos);
            PdfDocument pdfDoc   = new PdfDocument(writer);
            Document    document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── Membrete ──────────────────────────────────────────────────────
            document.add(new Paragraph("UNPA — Universidad del Papaloapan")
                    .setFont(bold).setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            document.add(new Paragraph("Gestión de Eventos Universitarios")
                    .setFont(regular).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(14));

            // ── Título del evento ─────────────────────────────────────────────
            document.add(new Paragraph("Lista de inscritos")
                    .setFont(bold).setFontSize(11)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(2));

            document.add(new Paragraph(actividad.getNombre())
                    .setFont(bold).setFontSize(15)
                    .setMarginBottom(4));

            String fecha = actividad.getFechaActividad()
                    .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
                            new java.util.Locale("es")));
            document.add(new Paragraph("Fecha: " + fecha
                    + "   |   Hora: " + actividad.getHoraInicio()
                    + " – " + actividad.getHoraFin())
                    .setFont(regular).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(14));

            // ── Tabla ─────────────────────────────────────────────────────────
            float[] anchos = {1f, 3f, 2f, 2.5f, 2f};
            Table tabla = new Table(UnitValue.createPercentArray(anchos))
                    .useAllAvailableWidth();

            // Encabezados
            for (String header : new String[]{"#", "Nombre", "Tipo", "Identificador", "Inscrito el"}) {
                tabla.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setFont(bold).setFontSize(9))
                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(46, 125, 82))
                        .setFontColor(ColorConstants.WHITE)
                        .setPadding(5));
            }

            // Filas
            boolean alt = false;
            for (InscritoListaItemResponse item : lista) {
                com.itextpdf.kernel.colors.Color fondo = alt
                        ? new com.itextpdf.kernel.colors.DeviceRgb(240, 248, 244)
                        : ColorConstants.WHITE;

                tabla.addCell(celdaTabla(String.valueOf(item.getNumero()), regular, fondo));
                tabla.addCell(celdaTabla(item.getNombre(),          regular, fondo));
                tabla.addCell(celdaTabla(item.getTipoParticipante(),regular, fondo));
                tabla.addCell(celdaTabla(item.getIdentificador(),   regular, fondo));
                tabla.addCell(celdaTabla(item.getFechaInscripcion(),regular, fondo));
                alt = !alt;
            }

            if (lista.isEmpty()) {
                tabla.addCell(new Cell(1, 5)
                        .add(new Paragraph("Sin inscritos aún.")
                                .setFont(regular).setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setPadding(8));
            }

            document.add(tabla);

            // ── Pie ───────────────────────────────────────────────────────────
            document.add(new Paragraph("\nTotal de inscritos: " + lista.size())
                    .setFont(bold).setFontSize(10)
                    .setMarginTop(10));

            document.add(new Paragraph("Generado el "
                    + java.time.LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(regular).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new BusinessException("Error al generar el PDF: " + e.getMessage());
        }
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] generarCsv(Integer idActividad, Integer idSolicitante) {
        Actividad actividad = validarAcceso(idActividad, idSolicitante);
        List<InscritoListaItemResponse> lista = construirLista(actividad);

        StringBuilder sb = new StringBuilder();
        // BOM UTF-8 para que Excel lo abra correctamente
        sb.append('\uFEFF');
        sb.append("Evento:,").append(escapeCsv(actividad.getNombre())).append("\n");
        sb.append("Fecha:,").append(actividad.getFechaActividad()).append("\n");
        sb.append("Total inscritos:,").append(lista.size()).append("\n\n");

        sb.append("#,Nombre,Tipo,Identificador,Fecha inscripción\n");
        for (InscritoListaItemResponse item : lista) {
            sb.append(item.getNumero()).append(",")
                    .append(escapeCsv(item.getNombre())).append(",")
                    .append(escapeCsv(item.getTipoParticipante())).append(",")
                    .append(escapeCsv(item.getIdentificador())).append(",")
                    .append(escapeCsv(item.getFechaInscripcion())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Valida que el solicitante tenga permiso de ver la lista:
     * - Es el profesor propietario del evento, O
     * - Es ADMIN
     */
    private Actividad validarAcceso(Integer idActividad, Integer idSolicitante) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada: " + idActividad));

        boolean esProfesorDueno = actividad.getProfesor().getIdUsuario().equals(idSolicitante);
        boolean esAdmin = usuarioRepository.findById(idSolicitante)
                .map(u -> u.getRol() == Rol.ADMIN)
                .orElse(false);

        if (!esProfesorDueno && !esAdmin) {
            throw new BusinessException(
                    "Solo el docente propietario o un administrador puede ver la lista de inscritos.");
        }

        return actividad;
    }

    private List<InscritoListaItemResponse> construirLista(Actividad actividad) {
        List<InscritoListaItemResponse> lista = new ArrayList<>();
        int num = 1;

        // Internos (usuarios/alumnos)
        for (var ins : inscripcionRepository.findByActividad_IdActividad(actividad.getIdActividad())) {
            String nombre, tipo, identificador;

            if (ins.getAlumno() != null) {
                nombre        = ins.getAlumno().getNombre() + " " + ins.getAlumno().getApellidos();
                tipo          = "Alumno";
                identificador = ins.getAlumno().getMatricula();
            } else {
                nombre        = ins.getUsuario().getNombre() + " " + ins.getUsuario().getApellidos();
                tipo          = "Docente/Staff";
                identificador = ins.getUsuario().getCorreo();
            }

            lista.add(new InscritoListaItemResponse(
                    num++, nombre, tipo, identificador,
                    ins.getFechaInscripcion().format(FMT)));
        }

        // Externos
        for (var ext : externoRepository.findByActividad_IdActividad(actividad.getIdActividad())) {
            lista.add(new InscritoListaItemResponse(
                    num++,
                    ext.getNombre(),
                    "Externo",
                    ext.getCorreo() != null ? ext.getCorreo() : "—",
                    ext.getFechaInscripcion().format(FMT)));
        }

        return lista;
    }

    private Cell celdaTabla(String texto, PdfFont font,
                            com.itextpdf.kernel.colors.Color fondo) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "—").setFont(font).setFontSize(9))
                .setBackgroundColor(fondo)
                .setPadding(4);
    }

    private String escapeCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}