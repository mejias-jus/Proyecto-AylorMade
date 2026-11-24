package principal;

import datos.ArchivoProductos;
import datos.ArchivoUsuarios;
import datos.ArchivoVentas;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import modelo.Administrador;
import modelo.Cajero;
import modelo.Efectivo;
import modelo.Producto;
import modelo.ProductoCanastaBasica;
import modelo.ProductoPerecedero;
import modelo.RegistroVenta;
import modelo.Usuario;
import modelo.Venta;

/**
 * Prueba SOLO de la Parte 2: comprueba que todo se guarda y se vuelve a leer de los .txt.
 * Usa la carpeta datos_prueba para no mezclarse con los datos reales del sistema.
 */
public class PruebaDatos {

    public static void main(String[] args) throws IOException {

        // Empezar limpio en cada prueba
        Files.deleteIfExists(Paths.get("datos_prueba/usuarios.txt"));
        Files.deleteIfExists(Paths.get("datos_prueba/productos.txt"));
        Files.deleteIfExists(Paths.get("datos_prueba/ventas.txt"));

        ArchivoUsuarios archivoUsuarios = new ArchivoUsuarios("datos_prueba/usuarios.txt");
        ArchivoProductos archivoProductos = new ArchivoProductos("datos_prueba/productos.txt");
        ArchivoVentas archivoVentas = new ArchivoVentas("datos_prueba/ventas.txt");

        // ---- 1. USUARIOS ----
        System.out.println("=== USUARIOS ===");
        ArrayList<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Administrador("Ana Mora", "amora", "ana@correo.com", "hash_admin"));
        usuarios.add(new Cajero("Luis Rojas", "lrojas", "luis@correo.com", "hash_cajero", 2));
        System.out.println("Guardados: " + archivoUsuarios.guardarTodos(usuarios));

        // Se simula "cerrar y volver a abrir el programa": se leen de nuevo desde el archivo
        ArrayList<Usuario> usuariosLeidos = archivoUsuarios.cargar();
        for (int i = 0; i < usuariosLeidos.size(); i++) {
            Usuario u = usuariosLeidos.get(i);
            System.out.println(u.getRol() + " | " + u.getNombreUsuario() + " | " + u.getCorreo() + " | caja " + u.getNumeroCaja());
        }

        // ---- 2. PRODUCTOS ----
        System.out.println("\n=== PRODUCTOS ===");
        ArrayList<Producto> productos = new ArrayList<>();
        productos.add(new Producto("P001", "Detergente", 2500, 20, 5));
        productos.add(new ProductoPerecedero("P002", "Leche 1L", 1200, 30, 10, LocalDate.now().plusDays(2)));
        productos.add(new ProductoCanastaBasica("P003", "Arroz 1kg", 1500, 50, 10));
        System.out.println("Guardados: " + archivoProductos.guardarTodos(productos));

        ArrayList<Producto> productosLeidos = archivoProductos.cargar();
        for (int i = 0; i < productosLeidos.size(); i++) {
            Producto p = productosLeidos.get(i);
            System.out.println(p.getCodigo() + " | " + p.getNombre() + " [" + p.getTipo() + "] stock "
                    + p.getStock() + " | precio final " + String.format("%.2f", p.calcularPrecioFinal()));
        }

        // ---- 3. VENTA: se cobra y se guarda ----
        System.out.println("\n=== VENTA ===");
        Cajero cajero = (Cajero) usuariosLeidos.get(1);
        Venta venta = new Venta(archivoVentas.obtenerSiguienteNumero(), cajero);
        venta.agregarProducto(productosLeidos.get(0), 2);
        venta.agregarProducto(productosLeidos.get(1), 3);
        System.out.println("Cobrada: " + venta.cerrarVenta(new Efectivo(20000)));
        System.out.println("Guardada en ventas.txt: " + archivoVentas.guardarVenta(venta));
        System.out.println("Se actualiza el stock en productos.txt: " + archivoProductos.guardarTodos(productosLeidos));

        // Una venta que NO se cobro no se guarda
        Venta sinCobrar = new Venta(archivoVentas.obtenerSiguienteNumero(), cajero);
        sinCobrar.agregarProducto(productosLeidos.get(2), 1);
        System.out.println("Venta sin cobrar se guarda: " + archivoVentas.guardarVenta(sinCobrar));

        // ---- 4. Volver a abrir: todo sigue ahi ----
        System.out.println("\n=== DESPUES DE 'REINICIAR' ===");
        ArrayList<Producto> productosDespues = new ArchivoProductos("datos_prueba/productos.txt").cargar();
        System.out.println("Stock detergente: " + productosDespues.get(0).getStock() + " (era 20)");
        System.out.println("Stock leche: " + productosDespues.get(1).getStock() + " (era 30)");
        ArrayList<RegistroVenta> ventas = archivoVentas.cargar();
        for (int i = 0; i < ventas.size(); i++) {
            RegistroVenta r = ventas.get(i);
            System.out.println("Venta " + r.getNumero() + " | " + r.getFecha() + " | cajero " + r.getUsuarioCajero()
                    + " | " + r.getMetodoPago() + " | total " + String.format("%.2f", r.getTotal())
                    + " | items " + r.getItems());
        }
        System.out.println("Siguiente numero de venta: " + archivoVentas.obtenerSiguienteNumero());

        // ---- 5. Archivo danado: el programa no se cae ----
        System.out.println("\n=== LINEA DANADA EN productos.txt ===");
        ArrayList<String> basura = new ArrayList<>();
        basura.add("esto no es un producto valido");
        basura.add("General|P009|Producto roto|abc|5|1|");
        Files.write(Paths.get("datos_prueba/productos.txt"), basura, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        System.out.println("Productos validos leidos: " + archivoProductos.cargar().size() + " (las 2 lineas malas se ignoran)");

        // ---- 6. Archivo que no existe ----
        System.out.println("Leer un archivo que no existe: " + new ArchivoProductos("datos_prueba/no_existe.txt").cargar().size() + " productos");
    }
}
