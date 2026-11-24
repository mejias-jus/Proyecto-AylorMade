package principal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import logica.SistemaSupermercado;
import modelo.Efectivo;
import modelo.Sucursal;
import modelo.Usuario;
import modelo.Venta;

/**
 * Prueba de la Parte 5 (sin pantallas): factura por correo, reportes por producto y sucursales.
 * Se ejecuta desde la carpeta raiz del proyecto (donde esta la carpeta "ejemplo").
 */
public class PruebaParte5 {

    private static String res(String error) {
        if (error.isEmpty()) {
            return "OK";
        }
        return "RECHAZADO -> " + error;
    }

    public static void main(String[] args) throws IOException {
        String[] archivos = {"usuarios", "productos", "ventas", "entradas", "sucursales"};
        for (int i = 0; i < archivos.length; i++) {
            Files.deleteIfExists(Paths.get("datos_prueba6/" + archivos[i] + ".txt"));
        }
        CorreoDePrueba correo = new CorreoDePrueba();
        SistemaSupermercado sistema = new SistemaSupermercado("datos_prueba6", correo);
        sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "Admin123*");
        Usuario admin = sistema.getUsuarios().iniciarSesion("amora", "Admin123*");
        sistema.getUsuarios().registrar(admin, "Cajero", "Luis Rojas", "lrojas", "luis@correo.com", "Caja12345", 1);
        Usuario cajero = sistema.getUsuarios().iniciarSesion("lrojas", "Caja12345");
        sistema.getProductos().importar(admin, "ejemplo/productos_ejemplo.txt");

        System.out.println("=== FACTURA POR CORREO ===");
        Venta venta = sistema.getVentas().iniciarVenta(cajero);
        System.out.println("Enviar antes de cobrar: " + res(sistema.getVentas().enviarFactura(venta, "cliente@gmail.com")));
        sistema.getVentas().agregarAlCarrito(venta, "100001", 3);
        sistema.getVentas().agregarAlCarrito(venta, "100006", 2);
        sistema.getVentas().cobrar(venta, new Efectivo(10000));
        System.out.println("Correo vacio (es opcional): " + res(sistema.getVentas().validarCorreoCliente("")));
        System.out.println("Correo mal escrito: " + res(sistema.getVentas().validarCorreoCliente("cliente@")));
        System.out.println("Correo con espacio: " + res(sistema.getVentas().validarCorreoCliente("cli ente@gmail.com")));
        System.out.println("Enviar factura: " + res(sistema.getVentas().enviarFactura(venta, " cliente@gmail.com ")));
        System.out.println("  destinatario: '" + correo.getUltimoDestinatario() + "'");
        System.out.println("  mensaje:\n    " + correo.getUltimoMensaje().replace("\n", "\n    "));

        Venta venta2 = sistema.getVentas().iniciarVenta(admin);
        sistema.getVentas().agregarAlCarrito(venta2, "100001", 5);
        sistema.getVentas().agregarAlCarrito(venta2, "100011", 1);
        sistema.getVentas().cobrar(venta2, new Efectivo(20000));

        System.out.println("\n=== PRODUCTOS VENDIDOS HOY ===");
        String hoy = sistema.getReportes().hoy();
        ArrayList<String[]> filas = sistema.getReportes().productosVendidos(hoy, sistema.getProductos());
        for (int i = 0; i < filas.size(); i++) {
            System.out.println(filas.get(i)[0] + " | " + filas.get(i)[1] + " | " + filas.get(i)[2] + " uds | "
                    + String.format("%.2f", Double.parseDouble(filas.get(i)[3])));
        }
        System.out.println("Total recaudado: " + String.format("%.2f", sistema.getReportes().totalRecaudado(hoy)));

        System.out.println("\n=== SUCURSALES ===");
        System.out.println("Cajero intenta agregar: " + res(sistema.getSucursales().agregar(cajero, "Centro", "San Jose", 9.93, -84.08, 4)));
        System.out.println("Admin agrega Centro: " + res(sistema.getSucursales().agregar(admin, "AylorMade Centro", "Avenida Central, San José", 9.9341, -84.0877, 8)));
        System.out.println("Admin agrega Escazu: " + res(sistema.getSucursales().agregar(admin, "AylorMade Escazú", "Escazú centro", 9.9189, -84.1395, 3)));
        System.out.println("Nombre repetido: " + res(sistema.getSucursales().agregar(admin, "aylormade centro", "Otra", 9.9, -84.0, 2)));
        System.out.println("Latitud invalida: " + res(sistema.getSucursales().agregar(admin, "Raro", "Otra", 123, -84.0, 2)));
        System.out.println("Sin cajas: " + res(sistema.getSucursales().agregar(admin, "Raro", "Otra", 9.9, -84.0, 0)));
        ArrayList<Sucursal> lista = new SistemaSupermercado("datos_prueba6", correo).getSucursales().listar();
        for (int i = 0; i < lista.size(); i++) {
            System.out.println("Guardada: " + lista.get(i).getNombre() + " | " + lista.get(i).getLatitud() + ", "
                    + lista.get(i).getLongitud() + " | " + lista.get(i).getTipo());
        }
        System.out.println("Eliminar Escazu: " + res(sistema.getSucursales().eliminar(admin, "AylorMade Escazú")));
        System.out.println("Quedan: " + sistema.getSucursales().listar().size());
    }
}
