package principal;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import logica.ServicioCorreo;
import logica.ServicioFactura;
import logica.SistemaSupermercado;
import modelo.Cajero;
import modelo.Efectivo;
import modelo.Producto;
import modelo.Tarjeta;
import modelo.Usuario;
import modelo.Venta;

/**
 * Prueba de la factura en PDF con codigo QR y de su envio como adjunto (sin enviar correos reales).
 * Los PDF quedan en la carpeta datos_prueba7/facturas.
 */
public class PruebaFactura {

    public static void main(String[] args) throws Exception {
        String[] archivos = {"usuarios", "productos", "ventas", "entradas", "sucursales"};
        for (int i = 0; i < archivos.length; i++) {
            Files.deleteIfExists(Paths.get("datos_prueba7/" + archivos[i] + ".txt"));
        }
        CorreoDePrueba correo = new CorreoDePrueba();
        SistemaSupermercado sistema = new SistemaSupermercado("datos_prueba7", correo);
        sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "Admin123*");
        Usuario admin = sistema.getUsuarios().iniciarSesion("amora", "Admin123*");
        sistema.getUsuarios().registrar(admin, "Cajero", "Luis Rojas", "lrojas", "luis@correo.com", "Caja12345", 1);
        Usuario cajero = sistema.getUsuarios().iniciarSesion("lrojas", "Caja12345");
        sistema.getProductos().importar(admin, "ejemplo/productos_ejemplo.txt");

        System.out.println("=== FACTURA NORMAL ===");
        Venta venta = sistema.getVentas().iniciarVenta(cajero);
        sistema.getVentas().agregarAlCarrito(venta, "100001", 3);
        sistema.getVentas().agregarAlCarrito(venta, "100007", 2);
        sistema.getVentas().agregarAlCarrito(venta, "100011", 1);
        sistema.getVentas().agregarAlCarrito(venta, "100009", 1); // vencido: no entra
        System.out.println("PDF antes de cobrar: " + sistema.getVentas().generarPdf(venta));
        System.out.println("Cobro: " + sistema.getVentas().cobrar(venta, new Efectivo(20000)).isEmpty());
        File pdf = sistema.getVentas().generarPdf(venta);
        System.out.println("PDF creado: " + pdf.getPath() + " (" + pdf.length() + " bytes)");
        System.out.println("Enviar con correo malo: " + sistema.getVentas().enviarFactura(venta, "mal"));
        System.out.println("Enviar con correo bueno: '" + sistema.getVentas().enviarFactura(venta, "cliente@gmail.com") + "'");
        System.out.println("  adjunto capturado: " + correo.getUltimoAdjunto());
        System.out.println("  texto del correo:\n    " + correo.getUltimoMensaje().replace("\n", "\n    "));

        System.out.println("\n=== FACTURA LARGA (varias paginas, nombres largos, tildes) ===");
        Cajero luis = new Cajero("Luis Rojas Ñandú", "lrojas", "luis@correo.com", "x", 3);
        Venta grande = new Venta(77, luis);
        for (int i = 1; i <= 48; i++) {
            String nombre = "Producto de prueba número " + i + " con descripción muy larga para probar el corte";
            if (i % 3 != 0) {
                nombre = "Café, azúcar y piña N.º " + i + " (500 g)";
            }
            grande.agregarProducto(new Producto("L" + i, nombre, 1000 + i * 137.5, 100, 5), 1 + i % 4);
        }
        grande.cerrarVenta(new Tarjeta("4821"));
        File pdfGrande = new ServicioFactura("datos_prueba7/facturas").guardarPdf(grande);
        System.out.println("PDF grande: " + pdfGrande.getPath() + " (" + pdfGrande.length() + " bytes)");

        System.out.println("\n=== CORREO CON ADJUNTO (se arma, no se envia) ===");
        ServicioCorreo real = new ServicioCorreo(new datos.ArchivoConfiguracion("datos_prueba7/no_existe.txt"));
        MimeMessage mensaje = real.construirMensaje(Session.getInstance(new Properties()), "aylor@gmail.com",
                "cliente@gmail.com", "AylorMade - Factura N.º 1", "Hola, adjuntamos su factura.", pdf);
        mensaje.saveChanges();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mensaje.writeTo(bytes);
        Files.write(Paths.get("datos_prueba7/correo_armado.eml"), bytes.toByteArray());
        System.out.println("Correo armado: " + bytes.size() + " bytes -> datos_prueba7/correo_armado.eml");
    }
}
