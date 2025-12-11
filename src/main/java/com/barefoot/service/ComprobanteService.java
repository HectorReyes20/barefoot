package com.barefoot.service;

import com.barefoot.model.DetallePedido;
import com.barefoot.model.Pedido;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ComprobanteService {

    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_BOLD = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    // ============================================================
    // GENERAR BOLETA
    // ============================================================
    public void generarBoleta(Pedido pedido, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Header
        agregarEncabezado(document, "BOLETA DE VENTA ELECTRÓNICA", pedido);

        // Datos del cliente
        agregarDatosCliente(document, pedido);

        // Tabla de productos
        agregarTablaProductos(document, pedido);

        // Totales
        agregarTotales(document, pedido);

        // Footer
        agregarFooter(document);

        document.close();

        log.info("Boleta generada exitosamente para pedido: {}", pedido.getNumeroPedido());
    }

    // ============================================================
    // GENERAR FACTURA
    // ============================================================
    public void generarFactura(Pedido pedido, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Header
        agregarEncabezado(document, "FACTURA ELECTRÓNICA", pedido);

        // Datos del cliente (con RUC para factura)
        agregarDatosClienteFactura(document, pedido);

        // Tabla de productos
        agregarTablaProductos(document, pedido);

        // Totales con IGV
        agregarTotalesConIGV(document, pedido);

        // Footer
        agregarFooter(document);

        document.close();

        log.info("Factura generada exitosamente para pedido: {}", pedido.getNumeroPedido());
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    private void agregarEncabezado(Document document, String tipoComprobante, Pedido pedido) throws DocumentException {
        // Logo y datos de la empresa
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(20);

        // Columna izquierda - Info empresa
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);

        Paragraph empresa = new Paragraph("BAREFOOT STORE", FONT_TITLE);
        empresa.setAlignment(Element.ALIGN_LEFT);
        leftCell.addElement(empresa);

        Paragraph ruc = new Paragraph("RUC: 20123456789", FONT_NORMAL);
        leftCell.addElement(ruc);

        Paragraph direccion = new Paragraph("Av. Principal 123, Lima, Perú", FONT_SMALL);
        leftCell.addElement(direccion);

        Paragraph contacto = new Paragraph("Tel: (01) 234-5678 | contacto@barefoot.com", FONT_SMALL);
        leftCell.addElement(contacto);

        headerTable.addCell(leftCell);

        // Columna derecha - Tipo de comprobante
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setBackgroundColor(new BaseColor(245, 245, 245));
        rightCell.setPadding(10);

        Paragraph tipo = new Paragraph(tipoComprobante, FONT_BOLD);
        tipo.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(tipo);

        Paragraph numero = new Paragraph(pedido.getNumeroPedido(), FONT_NORMAL);
        numero.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(numero);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Paragraph fecha = new Paragraph(pedido.getFechaPedido().format(formatter), FONT_SMALL);
        fecha.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(fecha);

        headerTable.addCell(rightCell);

        document.add(headerTable);
    }

    private void agregarDatosCliente(Document document, Pedido pedido) throws DocumentException {
        Paragraph clienteTitle = new Paragraph("DATOS DEL CLIENTE", FONT_BOLD);
        clienteTitle.setSpacingBefore(10);
        clienteTitle.setSpacingAfter(5);
        document.add(clienteTitle);

        PdfPTable clienteTable = new PdfPTable(2);
        clienteTable.setWidthPercentage(100);
        clienteTable.setSpacingAfter(20);

        agregarCeldaInfo(clienteTable, "Cliente:",
                pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido());
        agregarCeldaInfo(clienteTable, "DNI:",
                pedido.getUsuario().getDni() != null ? pedido.getUsuario().getDni() : "No especificado");
        agregarCeldaInfo(clienteTable, "Email:", pedido.getUsuario().getEmail());
        agregarCeldaInfo(clienteTable, "Teléfono:",
                pedido.getUsuario().getTelefono() != null ? pedido.getUsuario().getTelefono() : "No especificado");
        agregarCeldaInfo(clienteTable, "Dirección:", pedido.getDireccionEnvio());

        document.add(clienteTable);
    }

    private void agregarDatosClienteFactura(Document document, Pedido pedido) throws DocumentException {
        Paragraph clienteTitle = new Paragraph("DATOS DEL CLIENTE", FONT_BOLD);
        clienteTitle.setSpacingBefore(10);
        clienteTitle.setSpacingAfter(5);
        document.add(clienteTitle);

        PdfPTable clienteTable = new PdfPTable(2);
        clienteTable.setWidthPercentage(100);
        clienteTable.setSpacingAfter(20);

        agregarCeldaInfo(clienteTable, "Razón Social:",
                pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido());
        agregarCeldaInfo(clienteTable, "RUC:",
                pedido.getUsuario().getRuc() != null ? pedido.getUsuario().getRuc() : "No especificado");
        agregarCeldaInfo(clienteTable, "Email:", pedido.getUsuario().getEmail());
        agregarCeldaInfo(clienteTable, "Teléfono:",
                pedido.getUsuario().getTelefono() != null ? pedido.getUsuario().getTelefono() : "No especificado");
        agregarCeldaInfo(clienteTable, "Dirección Fiscal:", pedido.getDireccionEnvio());

        document.add(clienteTable);
    }

    private void agregarTablaProductos(Document document, Pedido pedido) throws DocumentException {
        Paragraph productosTitle = new Paragraph("DETALLE DE PRODUCTOS", FONT_BOLD);
        productosTitle.setSpacingBefore(10);
        productosTitle.setSpacingAfter(5);
        document.add(productosTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 1.5f, 1.5f, 2});
        table.setSpacingAfter(20);

        // Headers
        agregarCeldaHeader(table, "Item");
        agregarCeldaHeader(table, "Descripción");
        agregarCeldaHeader(table, "Cant.");
        agregarCeldaHeader(table, "P. Unit.");
        agregarCeldaHeader(table, "Subtotal");

        // Productos
        int item = 1;
        for (DetallePedido detalle : pedido.getDetalles()) {
            agregarCeldaDetalle(table, String.valueOf(item++));
            agregarCeldaDetalle(table, detalle.getProducto().getNombre());
            agregarCeldaDetalle(table, String.valueOf(detalle.getCantidad()));
            agregarCeldaDetalle(table, "S/ " + String.format("%.2f", detalle.getPrecioUnitario()));
            agregarCeldaDetalle(table, "S/ " + String.format("%.2f", detalle.getSubtotal()));
        }

        document.add(table);
    }

    private void agregarTotales(Document document, Pedido pedido) throws DocumentException {
        PdfPTable totalesTable = new PdfPTable(2);
        totalesTable.setWidthPercentage(40);
        totalesTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        agregarCeldaTotal(totalesTable, "Subtotal:", "S/ " + String.format("%.2f", pedido.getSubtotal()));
        agregarCeldaTotal(totalesTable, "Envío:", "S/ " + String.format("%.2f", pedido.getCostoEnvio()));

        if (pedido.getDescuento() != null && pedido.getDescuento() > 0) {
            agregarCeldaTotal(totalesTable, "Descuento:",
                    "- S/ " + String.format("%.2f", pedido.getDescuento()));
        }

        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL:", FONT_BOLD));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(10);
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase("S/ " + String.format("%.2f", pedido.getTotal()), FONT_BOLD));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(new BaseColor(240, 240, 240));

        totalesTable.addCell(labelCell);
        totalesTable.addCell(valueCell);

        document.add(totalesTable);
    }

    private void agregarTotalesConIGV(Document document, Pedido pedido) throws DocumentException {
        PdfPTable totalesTable = new PdfPTable(2);
        totalesTable.setWidthPercentage(40);
        totalesTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        double subtotalSinIGV = pedido.getSubtotal() / 1.18;
        double igv = pedido.getSubtotal() - subtotalSinIGV;

        agregarCeldaTotal(totalesTable, "Subtotal (Sin IGV):", "S/ " + String.format("%.2f", subtotalSinIGV));
        agregarCeldaTotal(totalesTable, "IGV (18%):", "S/ " + String.format("%.2f", igv));
        agregarCeldaTotal(totalesTable, "Envío:", "S/ " + String.format("%.2f", pedido.getCostoEnvio()));

        if (pedido.getDescuento() != null && pedido.getDescuento() > 0) {
            agregarCeldaTotal(totalesTable, "Descuento:",
                    "- S/ " + String.format("%.2f", pedido.getDescuento()));
        }

        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL:", FONT_BOLD));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(10);
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase("S/ " + String.format("%.2f", pedido.getTotal()), FONT_BOLD));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(new BaseColor(240, 240, 240));

        totalesTable.addCell(labelCell);
        totalesTable.addCell(valueCell);

        document.add(totalesTable);
    }

    private void agregarFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "\n\nRepresentación impresa de comprobante electrónico\n" +
                        "Puede verificar su comprobante en www.barefoot.com/verificar",
                FONT_SMALL
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    // Helpers para celdas
    private void agregarCeldaInfo(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_BOLD));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_NORMAL));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private void agregarCeldaHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD));
        cell.setBackgroundColor(new BaseColor(52, 152, 219));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
    }

    private void agregarCeldaDetalle(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(cell);
    }

    private void agregarCeldaTotal(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_NORMAL));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(10);
        labelCell.setPaddingBottom(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_NORMAL));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingBottom(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}