package logica;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import modelo.ItemVenta;
import modelo.Venta;

/**
 * Crea la factura en PDF de una venta cobrada, con el logo y un codigo QR (libreria ZXing),
 * y la guarda en la carpeta de facturas. El dibujo del PDF lo hace PdfSencillo.
 * La factura sale en una o varias paginas segun la cantidad de productos.
 */
public class ServicioFactura {

    private static final int AZUL = 0x1E3A5F;
    private static final int NARANJA = 0xF28C28;
    private static final int GRIS = 0x607080;
    private static final int TEXTO = 0x24303C;
    private static final int FILA_CLARA = 0xF3F6F9;
    private static final int BORDE = 0xD5DBE1;
    private static final int BLANCO = 0xFFFFFF;

    private static final double MARGEN = 40;
    private static final double ALTO_FILA = 22;
    private static final double X_CANTIDAD = 50;
    private static final double X_DESCRIPCION = 95;
    private static final double X_PRECIO = 440;
    private static final double X_SUBTOTAL = 553;
    private static final double LIMITE_INFERIOR = 70; // las filas no se dibujan por debajo de esta altura

    private String carpeta;

    public ServicioFactura(String carpeta) {
        this.carpeta = carpeta;
    }

    // Guarda el PDF de la venta y devuelve el archivo, o null si la venta no esta cobrada o no se pudo guardar.
    public File guardarPdf(Venta venta) {
        if (venta == null || !venta.estaCerrada()) {
            return null;
        }
        try {
            File carpetaFacturas = new File(carpeta);
            carpetaFacturas.mkdirs();
            File archivo = new File(carpetaFacturas, String.format("factura_%06d.pdf", venta.getNumero()));
            Files.write(archivo.toPath(), generarPdf(venta));
            return archivo;
        } catch (IOException e) {
            System.out.println("Error creando la factura PDF: " + e.getMessage());
            return null;
        }
    }

    public byte[] generarPdf(Venta venta) throws IOException {
        PdfSencillo pdf = new PdfSencillo();
        BufferedImage logo = leerLogo();
        if (logo != null) {
            ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
            ImageIO.write(logo, "jpg", jpeg);
            pdf.setLogo(jpeg.toByteArray(), logo.getWidth(), logo.getHeight());
        }
        boolean[][] qr = crearQr(textoDelQr(venta));
        int numero = venta.getNumero();

        // ----- Primera pagina: encabezado, datos de la venta y codigo QR -----
        pdf.nuevaPagina();
        pdf.rect(0, PdfSencillo.ALTO - 112, PdfSencillo.ANCHO, 112, AZUL);
        pdf.logo(MARGEN, PdfSencillo.ALTO - 96, 68, 68);
        pdf.texto(MARGEN + 82, PdfSencillo.ALTO - 58, "AylorMade", true, 28, BLANCO);
        pdf.texto(MARGEN + 82, PdfSencillo.ALTO - 78, "Cobro rápido, inventario al día", false, 11, 0xC9D6E8);
        pdf.textoDerecha(PdfSencillo.ANCHO - MARGEN, PdfSencillo.ALTO - 44, "FACTURA", false, 12, 0xC9D6E8);
        pdf.textoDerecha(PdfSencillo.ANCHO - MARGEN, PdfSencillo.ALTO - 72, "N.º " + numero, true, 24, BLANCO);
        pdf.rect(0, PdfSencillo.ALTO - 116, PdfSencillo.ANCHO, 4, NARANJA);

        double y = PdfSencillo.ALTO - 150;
        datoInfo(pdf, MARGEN, y, "FECHA Y HORA", venta.getFecha());
        String atendio = venta.getCajero().getNombre();
        if (venta.getCajero().getNumeroCaja() > 0) {
            atendio = atendio + "  ·  Caja " + venta.getCajero().getNumeroCaja();
        }
        datoInfo(pdf, MARGEN, y - 44, "ATENDIÓ", atendio);
        datoInfo(pdf, MARGEN + 230, y, "N.º DE FACTURA", String.format("%06d", numero));
        datoInfo(pdf, MARGEN + 230, y - 44, "ARTÍCULOS", String.valueOf(unidades(venta)) + " unidades");

        // QR dentro de un cuadro blanco con borde
        double ladoQr = 92;
        double xQr = PdfSencillo.ANCHO - MARGEN - ladoQr - 8;
        double yQr = PdfSencillo.ALTO - 236;
        pdf.rectBorde(xQr - 8, yQr - 8, ladoQr + 16, ladoQr + 16, BORDE, 1);
        pdf.qr(qr, xQr, yQr, ladoQr, AZUL);
        pdf.textoCentro(xQr + ladoQr / 2, yQr - 20, "Escanee para ver los datos", false, 7.5, GRIS);

        // ----- Tabla de productos (puede seguir en mas paginas) -----
        double yFila = PdfSencillo.ALTO - 276;
        encabezadoTabla(pdf, yFila);
        yFila = yFila - 24;
        ArrayList<ItemVenta> items = venta.getItems();
        int pagina = 1;
        for (int i = 0; i < items.size(); i++) {
            if (yFila - ALTO_FILA < LIMITE_INFERIOR) {
                pie(pdf, numero, pagina);
                pdf.nuevaPagina();
                pagina++;
                encabezadoContinuacion(pdf, numero);
                yFila = PdfSencillo.ALTO - 80;
                encabezadoTabla(pdf, yFila);
                yFila = yFila - 24;
            }
            ItemVenta item = items.get(i);
            if (i % 2 == 0) {
                pdf.rect(MARGEN, yFila - ALTO_FILA, PdfSencillo.ANCHO - 2 * MARGEN, ALTO_FILA, FILA_CLARA);
            }
            double base = yFila - 15;
            pdf.texto(X_CANTIDAD, base, String.valueOf(item.getCantidad()), true, 10, TEXTO);
            pdf.texto(X_DESCRIPCION, base, ajustar(pdf, item.getProducto().getNombre(), X_PRECIO - 85 - X_DESCRIPCION), false, 10, TEXTO);
            pdf.dinero(X_PRECIO, base, item.getProducto().calcularPrecioFinal(), false, 10, TEXTO);
            pdf.dinero(X_SUBTOTAL, base, item.getSubtotal(), true, 10, TEXTO);
            yFila = yFila - ALTO_FILA;
        }
        pdf.linea(MARGEN, yFila - 2, PdfSencillo.ANCHO - MARGEN, yFila - 2, BORDE, 1);

        // ----- Totales (si no caben en esta pagina, pasan a una nueva) -----
        if (yFila - 190 < 40) {
            pie(pdf, numero, pagina);
            pdf.nuevaPagina();
            pagina++;
            encabezadoContinuacion(pdf, numero);
            yFila = PdfSencillo.ALTO - 80;
        }
        double yTotal = yFila - 22;
        pdf.rect(305, yTotal - 70, 250, 66, 0xE8F0FA);
        pdf.texto(317, yTotal - 28, "TOTAL A PAGAR", true, 11, AZUL);
        pdf.dinero(543, yTotal - 58, venta.calcularTotal(), true, 22, AZUL);

        pdf.texto(MARGEN, yTotal - 14, "MÉTODO DE PAGO", true, 8, GRIS);
        String detalle = "";
        if (venta.getMetodoPago() != null) {
            detalle = venta.getMetodoPago().getDetalle();
        }
        pdf.texto(MARGEN, yTotal - 30, ajustar(pdf, detalle, 250), false, 11, TEXTO);
        if (venta.getVuelto() > 0) {
            pdf.texto(MARGEN, yTotal - 52, "VUELTO", true, 8, GRIS);
            pdf.dinero(MARGEN + pdf.anchoDinero(venta.getVuelto(), true, 14), yTotal - 66, venta.getVuelto(), true, 14, TEXTO);
        }

        pdf.textoCentro(PdfSencillo.ANCHO / 2, yTotal - 112, "¡Gracias por su compra!", true, 14, AZUL);
        pdf.textoCentro(PdfSencillo.ANCHO / 2, yTotal - 130,
                "Comprobante de venta de AylorMade. No sustituye a la factura electrónica oficial.", false, 8, GRIS);
        pie(pdf, numero, pagina);
        return pdf.generar();
    }

    // ----- Partes de la factura -----

    private void datoInfo(PdfSencillo pdf, double x, double y, String etiqueta, String valor) {
        pdf.texto(x, y, etiqueta, true, 8, GRIS);
        pdf.texto(x, y - 17, ajustar(pdf, valor, 215), false, 13, TEXTO);
    }

    private void encabezadoTabla(PdfSencillo pdf, double yArriba) {
        pdf.rect(MARGEN, yArriba - 24, PdfSencillo.ANCHO - 2 * MARGEN, 24, AZUL);
        double base = yArriba - 16;
        pdf.texto(X_CANTIDAD, base, "CANT.", true, 8.5, BLANCO);
        pdf.texto(X_DESCRIPCION, base, "DESCRIPCIÓN", true, 8.5, BLANCO);
        pdf.textoDerecha(X_PRECIO, base, "PRECIO UNIT.", true, 8.5, BLANCO);
        pdf.textoDerecha(X_SUBTOTAL, base, "SUBTOTAL", true, 8.5, BLANCO);
    }

    private void encabezadoContinuacion(PdfSencillo pdf, int numero) {
        pdf.rect(0, PdfSencillo.ALTO - 50, PdfSencillo.ANCHO, 50, AZUL);
        pdf.texto(MARGEN, PdfSencillo.ALTO - 32, "AylorMade", true, 18, BLANCO);
        pdf.textoDerecha(PdfSencillo.ANCHO - MARGEN, PdfSencillo.ALTO - 31,
                "Factura N.º " + numero + " (continuación)", false, 11, 0xC9D6E8);
        pdf.rect(0, PdfSencillo.ALTO - 54, PdfSencillo.ANCHO, 4, NARANJA);
    }

    private void pie(PdfSencillo pdf, int numero, int pagina) {
        pdf.textoCentro(PdfSencillo.ANCHO / 2, 28, "AylorMade  ·  Factura N.º " + numero + "  ·  Página " + pagina,
                false, 8, GRIS);
    }

    // Corta el texto con "..." si no cabe en el ancho indicado.
    private String ajustar(PdfSencillo pdf, String texto, double anchoMaximo) {
        if (pdf.ancho(texto, false, 10) <= anchoMaximo || pdf.ancho(texto, false, 13) <= anchoMaximo) {
            return texto;
        }
        String corto = texto;
        while (corto.length() > 1 && pdf.ancho(corto + "...", false, 13) > anchoMaximo) {
            corto = corto.substring(0, corto.length() - 1);
        }
        return corto + "...";
    }

    private int unidades(Venta venta) {
        int total = 0;
        for (int i = 0; i < venta.getItems().size(); i++) {
            total = total + venta.getItems().get(i).getCantidad();
        }
        return total;
    }

    // ----- Codigo QR y logo -----

    // Lo que se ve al escanear el QR con el telefono.
    private String textoDelQr(Venta venta) {
        return "AylorMade\n"
                + "Factura N. " + venta.getNumero() + "\n"
                + "Fecha: " + venta.getFecha() + "\n"
                + "Total: CRC " + String.format(java.util.Locale.US, "%.2f", venta.calcularTotal()) + "\n"
                + "Atendio: " + venta.getCajero().getNombreUsuario();
    }

    // Convierte el texto en una matriz de cuadritos (true = oscuro) usando la libreria ZXing.
    private boolean[][] crearQr(String texto) throws IOException {
        try {
            Map<EncodeHintType, Object> pistas = new HashMap<>();
            pistas.put(EncodeHintType.MARGIN, 0);
            pistas.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            pistas.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 1, 1, pistas);
            boolean[][] modulos = new boolean[matriz.getHeight()][matriz.getWidth()];
            for (int fila = 0; fila < matriz.getHeight(); fila++) {
                for (int col = 0; col < matriz.getWidth(); col++) {
                    modulos[fila][col] = matriz.get(col, fila);
                }
            }
            return modulos;
        } catch (Exception e) {
            throw new IOException("No se pudo crear el código QR: " + e.getMessage());
        }
    }

    // Lee el logo de la carpeta de imagenes y lo pone sobre el color azul del encabezado.
    private BufferedImage leerLogo() {
        try {
            URL url = ServicioFactura.class.getResource("/imagenes/logo.png");
            if (url == null) {
                return null;
            }
            BufferedImage original = ImageIO.read(url);
            BufferedImage resultado = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resultado.createGraphics();
            g.setColor(new Color(AZUL));
            g.fillRect(0, 0, original.getWidth(), original.getHeight());
            g.drawImage(original, 0, 0, null);
            g.dispose();
            return resultado;
        } catch (Exception e) {
            return null;
        }
    }
}
