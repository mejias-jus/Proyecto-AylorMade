package logica;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Escritor de PDF muy sencillo, hecho a mano (no usa librerias de PDF).
 * Un PDF es un archivo con "objetos" numerados: el catalogo, la lista de paginas, las fuentes y, por cada
 * pagina, un flujo de instrucciones de dibujo ("escribir este texto aqui", "pintar este rectangulo").
 * Al final se agrega una tabla (xref) que dice en que byte empieza cada objeto.
 * Usa las fuentes estandar Helvetica y Helvetica-Bold, que todos los lectores de PDF traen.
 * Las coordenadas son en puntos (1 pt = 1/72 de pulgada) y el origen (0, 0) esta ABAJO a la izquierda.
 */
public class PdfSencillo {

    public static final double ANCHO = 595; // hoja A4
    public static final double ALTO = 842;

    // Ancho de cada caracter (de 32 a 126) en milesimas del tamano de la letra, segun las tablas de Helvetica.
    private static final int[] ANCHO_NORMAL = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556,
        278, 278, 584, 584, 584, 556, 1015,
        667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611,
        278, 278, 278, 469, 556, 333,
        556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556, 556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500,
        334, 260, 334, 584};
    private static final int[] ANCHO_NEGRITA = {
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556,
        333, 333, 584, 584, 584, 611, 975,
        722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611,
        333, 278, 333, 584, 556, 333,
        556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611, 611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500,
        389, 280, 389, 584};

    private ArrayList<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder actual = null;
    private byte[] logoJpeg = null;
    private int logoAncho = 0;
    private int logoAlto = 0;

    public void nuevaPagina() {
        actual = new StringBuilder();
        paginas.add(actual);
    }

    public int cantidadPaginas() {
        return paginas.size();
    }

    // Imagen JPEG que se puede dibujar con logo(...) en cualquier pagina.
    public void setLogo(byte[] jpeg, int ancho, int alto) {
        this.logoJpeg = jpeg;
        this.logoAncho = ancho;
        this.logoAlto = alto;
    }

    // ----- Medidas de texto -----

    public double ancho(String texto, boolean negrita, double tamano) {
        int[] tabla = ANCHO_NORMAL;
        if (negrita) {
            tabla = ANCHO_NEGRITA;
        }
        double total = 0;
        for (int i = 0; i < texto.length(); i++) {
            total = total + anchoCaracter(texto.charAt(i), tabla);
        }
        return total * tamano / 1000.0;
    }

    private int anchoCaracter(char c, int[] tabla) {
        if (c >= 32 && c <= 126) {
            return tabla[c - 32];
        }
        // letras con tilde: tienen el mismo ancho que la letra sin tilde
        String sinTilde = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
        char base = sinTilde.charAt(0);
        if (base >= 32 && base <= 126) {
            return tabla[base - 32];
        }
        return 556;
    }

    // ----- Dibujo -----

    public void rect(double x, double y, double ancho, double alto, int rgb) {
        actual.append("q ").append(color(rgb, true)).append(num(x)).append(' ').append(num(y)).append(' ')
                .append(num(ancho)).append(' ').append(num(alto)).append(" re f Q\n");
    }

    public void rectBorde(double x, double y, double ancho, double alto, int rgb, double grosor) {
        actual.append("q ").append(color(rgb, false)).append(num(grosor)).append(" w ")
                .append(num(x)).append(' ').append(num(y)).append(' ')
                .append(num(ancho)).append(' ').append(num(alto)).append(" re S Q\n");
    }

    public void linea(double x1, double y1, double x2, double y2, int rgb, double grosor) {
        actual.append("q ").append(color(rgb, false)).append(num(grosor)).append(" w ")
                .append(num(x1)).append(' ').append(num(y1)).append(" m ")
                .append(num(x2)).append(' ').append(num(y2)).append(" l S Q\n");
    }

    public void texto(double x, double y, String texto, boolean negrita, double tamano, int rgb) {
        String fuente = "/F1";
        if (negrita) {
            fuente = "/F2";
        }
        actual.append("q BT ").append(color(rgb, true)).append(fuente).append(' ').append(num(tamano)).append(" Tf ")
                .append(num(x)).append(' ').append(num(y)).append(" Td ")
                .append(codificar(texto)).append(" Tj ET Q\n");
    }

    public void textoDerecha(double xDerecha, double y, String texto, boolean negrita, double tamano, int rgb) {
        texto(xDerecha - ancho(texto, negrita, tamano), y, texto, negrita, tamano, rgb);
    }

    public void textoCentro(double xCentro, double y, String texto, boolean negrita, double tamano, int rgb) {
        texto(xCentro - ancho(texto, negrita, tamano) / 2, y, texto, negrita, tamano, rgb);
    }

    // Ancho total que ocupa un monto escrito con dinero() (cifra mas simbolo).
    public double anchoDinero(double valor, boolean negrita, double tamano) {
        return ancho(String.format(Locale.US, "%,.2f", valor), negrita, tamano) + tamano * 0.14 + tamano * 0.58;
    }

    // Escribe un monto con el simbolo de colon dibujado a mano (la fuente estandar no trae el simbolo).
    public void dinero(double xDerecha, double y, double valor, boolean negrita, double tamano, int rgb) {
        String cifra = String.format(Locale.US, "%,.2f", valor);
        double anchoCifra = ancho(cifra, negrita, tamano);
        texto(xDerecha - anchoCifra, y, cifra, negrita, tamano, rgb);
        colon(xDerecha - anchoCifra - tamano * 0.14 - tamano * 0.58, y, tamano, rgb);
    }

    // Dibuja el simbolo del colon costarricense: una C con dos rayas inclinadas.
    private void colon(double x, double y, double tamano, int rgb) {
        double cx = x + tamano * 0.30;
        double cy = y + tamano * 0.35;
        double r = tamano * 0.27;
        StringBuilder sb = new StringBuilder("q ").append(color(rgb, false));
        sb.append(num(tamano * 0.085)).append(" w 1 J ");
        // arco de la C: desde 45 grados hasta 315 grados, en tres tramos
        double inicio = Math.toRadians(45);
        double tramo = Math.toRadians(90);
        for (int i = 0; i < 3; i++) {
            double a = inicio + i * tramo;
            double b = a + tramo;
            double k = 4.0 / 3.0 * Math.tan((b - a) / 4);
            double x0 = cx + r * Math.cos(a);
            double y0 = cy + r * Math.sin(a);
            double x3 = cx + r * Math.cos(b);
            double y3 = cy + r * Math.sin(b);
            double x1 = x0 - k * r * Math.sin(a);
            double y1 = y0 + k * r * Math.cos(a);
            double x2 = x3 + k * r * Math.sin(b);
            double y2 = y3 - k * r * Math.cos(b);
            if (i == 0) {
                sb.append(num(x0)).append(' ').append(num(y0)).append(" m ");
            }
            sb.append(num(x1)).append(' ').append(num(y1)).append(' ').append(num(x2)).append(' ')
                    .append(num(y2)).append(' ').append(num(x3)).append(' ').append(num(y3)).append(" c ");
        }
        sb.append("S ");
        // dos rayas inclinadas que cruzan la C
        for (int i = -1; i <= 1; i += 2) {
            double dx = i * tamano * 0.075;
            sb.append(num(cx + dx - tamano * 0.07)).append(' ').append(num(y - tamano * 0.03)).append(" m ")
                    .append(num(cx + dx + tamano * 0.07)).append(' ').append(num(y + tamano * 0.73)).append(" l S ");
        }
        sb.append("Q\n");
        actual.append(sb);
    }

    // Dibuja un codigo QR con vectores (un cuadradito por cada modulo oscuro), sin necesidad de imagenes.
    public void qr(boolean[][] modulos, double x, double y, double lado, int rgb) {
        int n = modulos.length;
        double m = lado / n;
        StringBuilder sb = new StringBuilder("q ").append(color(rgb, true));
        for (int fila = 0; fila < n; fila++) {
            int col = 0;
            while (col < n) {
                if (modulos[fila][col]) {
                    int inicio = col;
                    while (col < n && modulos[fila][col]) {
                        col++;
                    }
                    // se juntan los modulos oscuros seguidos de una fila en un solo rectangulo
                    sb.append(num4(x + inicio * m)).append(' ').append(num4(y + lado - (fila + 1) * m)).append(' ')
                            .append(num4((col - inicio) * m + 0.02)).append(' ').append(num4(m + 0.02)).append(" re ");
                } else {
                    col++;
                }
            }
        }
        sb.append("f Q\n");
        actual.append(sb);
    }

    public void logo(double x, double y, double ancho, double alto) {
        if (logoJpeg == null) {
            return;
        }
        actual.append("q ").append(num(ancho)).append(" 0 0 ").append(num(alto)).append(' ')
                .append(num(x)).append(' ').append(num(y)).append(" cm /Im1 Do Q\n");
    }

    // ----- Armado del archivo -----

    public byte[] generar() throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        boolean hayLogo = logoJpeg != null;
        int idLogo = 5;
        int primerContenido = 5;
        if (hayLogo) {
            primerContenido = 6;
        }
        int total = primerContenido - 1 + 2 * paginas.size();
        int[] posiciones = new int[total + 1];

        salida.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
        salida.write(new byte[]{'%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

        StringBuilder hijas = new StringBuilder();
        for (int i = 0; i < paginas.size(); i++) {
            hijas.append(primerContenido + 2 * i + 1).append(" 0 R ");
        }
        objeto(salida, posiciones, 1, "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.ISO_8859_1));
        objeto(salida, posiciones, 2, ("<< /Type /Pages /Count " + paginas.size() + " /Kids [ " + hijas + "] >>")
                .getBytes(StandardCharsets.ISO_8859_1));
        objeto(salida, posiciones, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"
                .getBytes(StandardCharsets.ISO_8859_1));
        objeto(salida, posiciones, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"
                .getBytes(StandardCharsets.ISO_8859_1));
        if (hayLogo) {
            objeto(salida, posiciones, idLogo, flujo("<< /Type /XObject /Subtype /Image /Width " + logoAncho
                    + " /Height " + logoAlto + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode", logoJpeg));
        }
        String recursos = "/Resources << /Font << /F1 3 0 R /F2 4 0 R >>";
        if (hayLogo) {
            recursos = recursos + " /XObject << /Im1 " + idLogo + " 0 R >>";
        }
        recursos = recursos + " >>";
        for (int i = 0; i < paginas.size(); i++) {
            int idContenido = primerContenido + 2 * i;
            int idPagina = idContenido + 1;
            objeto(salida, posiciones, idContenido, flujo("<<", paginas.get(i).toString().getBytes(StandardCharsets.ISO_8859_1)));
            objeto(salida, posiciones, idPagina, ("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + (int) ANCHO + " "
                    + (int) ALTO + "] " + recursos + " /Contents " + idContenido + " 0 R >>").getBytes(StandardCharsets.ISO_8859_1));
        }

        int inicioTabla = salida.size();
        StringBuilder tabla = new StringBuilder("xref\n0 " + (total + 1) + "\n0000000000 65535 f \n");
        for (int id = 1; id <= total; id++) {
            tabla.append(String.format("%010d 00000 n \n", posiciones[id]));
        }
        tabla.append("trailer\n<< /Size ").append(total + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(inicioTabla).append("\n%%EOF\n");
        salida.write(tabla.toString().getBytes(StandardCharsets.ISO_8859_1));
        return salida.toByteArray();
    }

    private void objeto(ByteArrayOutputStream salida, int[] posiciones, int id, byte[] cuerpo) throws IOException {
        posiciones[id] = salida.size();
        salida.write((id + " 0 obj\n").getBytes(StandardCharsets.ISO_8859_1));
        salida.write(cuerpo);
        salida.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    // Arma un objeto de tipo "flujo": su diccionario (con el largo) seguido de los datos.
    private byte[] flujo(String diccionario, byte[] datos) throws IOException {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        s.write((diccionario + " /Length " + datos.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
        s.write(datos);
        s.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
        return s.toByteArray();
    }

    // ----- Ayudas -----

    private String num(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private String num4(double v) {
        return String.format(Locale.US, "%.4f", v);
    }

    // Color en formato PDF: tres numeros de 0 a 1 seguidos de rg (relleno) o RG (linea).
    private String color(int rgb, boolean relleno) {
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;
        String operador = "RG";
        if (relleno) {
            operador = "rg";
        }
        return String.format(Locale.US, "%.3f %.3f %.3f %s ", r, g, b, operador);
    }

    // Convierte el texto a la forma (texto) del PDF: codificacion Windows-1252 y caracteres especiales con barra.
    private String codificar(String texto) {
        byte[] bytes = texto.getBytes(Charset.forName("windows-1252"));
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            if (v == '(' || v == ')' || v == '\\') {
                sb.append('\\').append((char) v);
            } else if (v < 32 || v > 126) {
                sb.append(String.format("\\%03o", v));
            } else {
                sb.append((char) v);
            }
        }
        return sb.append(")").toString();
    }
}
