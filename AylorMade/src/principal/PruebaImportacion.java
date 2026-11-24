package principal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import logica.SistemaSupermercado;
import modelo.Producto;
import modelo.Usuario;
import modelo.Venta;

/**
 * Prueba de la importacion de los 15 productos de ejemplo (ejemplo/productos_ejemplo.txt).
 * Se ejecuta desde la carpeta raiz del proyecto, donde esta la carpeta "ejemplo".
 */
public class PruebaImportacion {

    private static String res(String error) {
        if (error.isEmpty()) {
            return "OK";
        }
        return "RECHAZADO -> " + error;
    }

    public static void main(String[] args) throws IOException {
        String[] archivos = {"usuarios", "productos", "ventas", "entradas"};
        for (int i = 0; i < archivos.length; i++) {
            Files.deleteIfExists(Paths.get("datos_prueba5/" + archivos[i] + ".txt"));
        }
        SistemaSupermercado sistema = new SistemaSupermercado("datos_prueba5", new CorreoDePrueba());
        sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "Admin123*");
        Usuario admin = sistema.getUsuarios().iniciarSesion("amora", "Admin123*");
        sistema.getUsuarios().registrar(admin, "Cajero", "Luis Rojas", "lrojas", "luis@correo.com", "Caja12345", 1);
        Usuario cajero = sistema.getUsuarios().iniciarSesion("lrojas", "Caja12345");

        System.out.println("Productos al empezar: " + sistema.getProductos().listar().size());
        System.out.println("Cajero intenta importar: " + res(sistema.getProductos().importar(cajero, "ejemplo/productos_ejemplo.txt")));
        System.out.println("Archivo que no existe: " + res(sistema.getProductos().importar(admin, "ejemplo/nada.txt")));
        System.out.println("Admin importa el ejemplo: " + res(sistema.getProductos().importar(admin, "ejemplo/productos_ejemplo.txt"))
                + " | importados: " + sistema.getProductos().getImportados() + ", omitidos: " + sistema.getProductos().getOmitidos());
        System.out.println("Importar otra vez: " + res(sistema.getProductos().importar(admin, "ejemplo/productos_ejemplo.txt"))
                + " | importados: " + sistema.getProductos().getImportados() + ", omitidos: " + sistema.getProductos().getOmitidos());
        System.out.println("Productos ahora: " + sistema.getProductos().listar().size());

        System.out.println("\n=== ALERTAS QUE VERA EL PROFESOR ===");
        ArrayList<Producto> bajos = sistema.getProductos().productosBajoStock();
        for (int i = 0; i < bajos.size(); i++) {
            System.out.println("Stock bajo: " + bajos.get(i).getNombre() + " (" + bajos.get(i).getStock() + ")");
        }
        ArrayList<Producto> porVencer = sistema.getProductos().productosPorVencer();
        for (int i = 0; i < porVencer.size(); i++) {
            System.out.println("Por vencer: " + porVencer.get(i).getNombre() + " (vence " + porVencer.get(i).getFechaVencimientoTexto() + ")");
        }
        ArrayList<Producto> vencidos = sistema.getProductos().productosVencidos();
        for (int i = 0; i < vencidos.size(); i++) {
            System.out.println("Vencido: " + vencidos.get(i).getNombre());
        }

        System.out.println("\n=== UNA VENTA CON LOS PRODUCTOS DE EJEMPLO ===");
        Venta venta = sistema.getVentas().iniciarVenta(cajero);
        System.out.println("Codigo 100001 x3 (arroz): " + res(sistema.getVentas().agregarAlCarrito(venta, "100001", 3)));
        System.out.println("Codigo 100008 x1 (queso por vencer): " + res(sistema.getVentas().agregarAlCarrito(venta, "100008", 1)));
        System.out.println("Codigo 100009 (pan vencido): " + res(sistema.getVentas().agregarAlCarrito(venta, "100009", 1)));
        System.out.println("Total: " + String.format("%.2f", venta.calcularTotal()));
        System.out.println("Fin de la prueba. Entradas de hoy: " + sistema.getReportes().unidadesIngresadas(sistema.getReportes().hoy()) + " unidades");
    }
}
