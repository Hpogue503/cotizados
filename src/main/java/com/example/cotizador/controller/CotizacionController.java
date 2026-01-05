package com.example.cotizador.controller;

import com.example.cotizador.dto.CotizacionItem;
import com.example.cotizador.entity.Producto;
import com.example.cotizador.repository.ProductoRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cotizar")
public class CotizacionController {

    private final ProductoRepository productoRepo;
    private final List<CotizacionItem> carrito = new ArrayList<>();

    public CotizacionController(ProductoRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    @GetMapping
    public String vista(Model model) {
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", calcularTotal());
        return "cotizacion";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Long productoId,
                          @RequestParam int cantidad) {

        Producto p = productoRepo.findById(productoId).orElseThrow();

        CotizacionItem item = carrito.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            item = new CotizacionItem();
            item.setProductoId(p.getId());
            item.setCodigo(p.getCodigo());
            item.setDescripcion(p.getDescripcion());
            item.setPrecioUnitario(p.getPrecio());
            item.setCantidad(cantidad);
            carrito.add(item);
        } else {
            item.setCantidad(item.getCantidad() + cantidad);
        }

        item.recalcularSubtotal();
        return "redirect:/cotizar";
    }

    @GetMapping("/pdf")
    public void generarPdf(HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=cotizacion.pdf");

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        /* ===== HEADER ===== */
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3, 2});

        PdfPCell logo = new PdfPCell(new Phrase("LOGO", boldFont));
        logo.setFixedHeight(50);
        logo.setBorder(Rectangle.BOX);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell title = new PdfPCell();
        title.setBorder(Rectangle.NO_BORDER);
        title.addElement(new Paragraph("COTIZACIÓN", titleFont));
        title.addElement(new Paragraph("Fecha: " + java.time.LocalDate.now(), normalFont));
        title.setHorizontalAlignment(Element.ALIGN_RIGHT);

        header.addCell(logo);
        header.addCell(title);
        document.add(header);

        document.add(new Paragraph("\n"));

        /* ===== CLIENT ===== */
        document.add(new Paragraph("Cliente: Consumidor Final", boldFont));
        document.add(new Paragraph("\n"));

        /* ===== DETAIL TABLE ===== */
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1.5f, 2, 2});

        table.addCell(headerCell("Descripción"));
        table.addCell(headerCell("Cantidad"));
        table.addCell(headerCell("Precio Unit."));
        table.addCell(headerCell("Subtotal"));

        for (CotizacionItem i : carrito) {
            table.addCell(bodyCell(i.getDescripcion()));
            table.addCell(bodyCell(String.valueOf(i.getCantidad()), Element.ALIGN_RIGHT));
            table.addCell(bodyCell(format(i.getPrecioUnitario()), Element.ALIGN_RIGHT));
            table.addCell(bodyCell(format(i.getSubtotal()), Element.ALIGN_RIGHT));
        }

        document.add(table);

        /* ===== TOTAL ===== */
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        totalTable.addCell(bodyCell("TOTAL", Element.ALIGN_RIGHT));
        totalTable.addCell(bodyCell(format(calcularTotal()), Element.ALIGN_RIGHT));

        document.add(totalTable);

        document.close();
    }
    private BigDecimal calcularTotal() {
        return carrito.stream()
                .map(CotizacionItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Long productoId) {

        carrito.removeIf(i -> i.getProductoId().equals(productoId));

        return "redirect:/cotizar";
    }

    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam Long productoId,
                                     @RequestParam int cantidad) {

        carrito.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .ifPresent(i -> {
                    if (cantidad <= 0) {
                        carrito.remove(i);
                    } else {
                        i.setCantidad(cantidad);
                        i.recalcularSubtotal();
                    }
                });

        return "redirect:/cotizar";
    }

    @PostMapping("/limpiar")
    public String limpiarCotizacion() {
        carrito.clear();
        return "redirect:/cotizar";
    }
    @GetMapping("/pdf-preview")
    public String previewPdf(Model model) {

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", calcularTotal());
        model.addAttribute("fecha", java.time.LocalDate.now());

        return "cotizacion-pdf";
    }

    private PdfPCell headerCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell bodyCell(String text) {
        return bodyCell(text, Element.ALIGN_LEFT);
    }

    private PdfPCell bodyCell(String text, int align) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 11);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private String format(BigDecimal value) {
        return "$ " + new java.text.DecimalFormat("#,##0.00").format(value);
    }
}