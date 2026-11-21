package principal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import logica.SistemaSupermercado;
import modelo.Efectivo;
import modelo.Producto;
import modelo.Sinpe;
import modelo.Usuario;
import modelo.Venta;

/**
 * Prueba SOLO de la Parte 3: login, seguridad, inventario, ventas y reportes por consola.
 * Usa la carpeta datos_prueba3 para no mezclarse con los datos reales.
 */
public class PruebaLogica {

    // Convierte el resultado de un servicio ("" = ok) en un texto legible.
    private static String res(String error) {
        if (error.isEmpty()) {
            return "OK";
        }
        return "RECHAZADO -> " + error;
    }

    public static void main(String[] args) throws IOException {
        String[] archivos = {"usuarios", "productos", "ventas", "entradas"};
        for (int i = 0; i < archivos.length; i++) {
            Files.deleteIfExists(Paths.get("datos_prueba3/" + archivos[i] + ".txt"));
        }

        SistemaSupermercado sistema = new SistemaSupermercado("datos_prueba3");

        // ---- 1. PRIMER ADMINISTRADOR Y SEGURIDAD ----
        System.out.println("=== 1. PRIMER ADMINISTRADOR ===");
        System.out.println("Hay usuarios al inicio: " + sistema.getUsuarios().hayUsuarios());
        System.out.println("Clave debil (1234): " + res(sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "1234")));
        System.out.println("Clave solo letras: " + res(sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "abcdefghi")));
        System.out.println("Correo invalido: " + res(sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "anacorreo", "Admin123*")));
        System.out.println("Datos correctos: " + res(sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "Admin123*")));
        System.out.println("Segundo 'primer administrador': " + res(sistema.getUsuarios().registrarPrimerAdministrador("Intruso", "intruso", "i@correo.com", "Intruso123*")));
        System.out.println("Contenido de usuarios.txt (la clave NO aparece):");
        System.out.println("  " + Files.readAllLines(Paths.get("datos_prueba3/usuarios.txt")).get(0));

        // ---- 2. LOGIN ----
        System.out.println("\n=== 2. INICIO DE SESION ===");
        Usuario admin = sistema.getUsuarios().iniciarSesion("amora", "Admin123*");
        System.out.println("Clave correcta: " + (admin != null ? "entra como " + admin.getRol() : "FALLO"));
        System.out.println("Usuario en mayusculas (AMORA): " + (sistema.getUsuarios().iniciarSesion("AMORA", "Admin123*") != null));
        System.out.println("Clave incorrecta: " + (sistema.getUsuarios().iniciarSesion("amora", "otraClave1") != null));
        System.out.println("Usuario inexistente: " + (sistema.getUsuarios().iniciarSesion("nadie", "Admin123*") != null));

        // ---- 3. USUARIOS Y PERMISOS ----
        System.out.println("\n=== 3. CREAR USUARIOS ===");
        System.out.println("Admin crea cajero: " + res(sistema.getUsuarios().registrar(admin, "Cajero", "Luis Rojas", "lrojas", "luis@correo.com", "Caja12345", 2)));
        System.out.println("Usuario repetido: " + res(sistema.getUsuarios().registrar(admin, "Cajero", "Otro", "LROJAS", "o@correo.com", "Caja12345", 3)));
        Usuario cajero = sistema.getUsuarios().iniciarSesion("lrojas", "Caja12345");
        System.out.println("Cajero inicia sesion: " + cajero.getRol() + ", caja " + cajero.getNumeroCaja());
        System.out.println("Cajero intenta crear usuarios: " + res(sistema.getUsuarios().registrar(cajero, "Cajero", "X", "xxx", "x@correo.com", "Caja12345", 3)));
        System.out.println("Admin se elimina a si mismo: " + res(sistema.getUsuarios().eliminar(admin, "amora")));
        System.out.println("Cambiar clave con la actual mala: " + res(sistema.getUsuarios().cambiarClave(cajero, "mala", "NuevaClave1")));
        System.out.println("Cambiar clave correctamente: " + res(sistema.getUsuarios().cambiarClave(cajero, "Caja12345", "NuevaClave1")));
        System.out.println("Entra con la clave vieja: " + (sistema.getUsuarios().iniciarSesion("lrojas", "Caja12345") != null));
        System.out.println("Entra con la clave nueva: " + (sistema.getUsuarios().iniciarSesion("lrojas", "NuevaClave1") != null));

        // ---- 4. PRODUCTOS ----
        System.out.println("\n=== 4. PRODUCTOS ===");
        System.out.println("Cajero intenta agregar producto: " + res(sistema.getProductos().agregar(cajero, "General", "P001", "Detergente", 2500, 20, 5, null)));
        System.out.println("Admin agrega detergente: " + res(sistema.getProductos().agregar(admin, "General", "P001", "Detergente", 2500, 20, 5, null)));
        System.out.println("Admin agrega leche (vence en 2 dias): " + res(sistema.getProductos().agregar(admin, "Perecedero", "P002", "Leche 1L", 1200, 30, 10, LocalDate.now().plusDays(2))));
        System.out.println("Admin agrega arroz: " + res(sistema.getProductos().agregar(admin, "Canasta basica", "P003", "Arroz 1kg", 1500, 3, 10, null)));
        System.out.println("Codigo repetido: " + res(sistema.getProductos().agregar(admin, "General", "p001", "Otro", 100, 1, 1, null)));
        System.out.println("Precio cero: " + res(sistema.getProductos().agregar(admin, "General", "P004", "Gratis", 0, 1, 1, null)));
        System.out.println("Perecedero ya vencido: " + res(sistema.getProductos().agregar(admin, "Perecedero", "P005", "Yogurt", 900, 5, 2, LocalDate.now().minusDays(1))));
        System.out.println("Editar precio del detergente a 2600: " + res(sistema.getProductos().editar(admin, "P001", "Detergente", 2600, 5, null)));
        System.out.println("Productos registrados: " + sistema.getProductos().listar().size());

        // ---- 5. ENTRADAS ----
        System.out.println("\n=== 5. ENTRADA DE MERCADERIA ===");
        System.out.println("Stock arroz antes: " + sistema.getProductos().buscarPorCodigo("P003").getStock());
        System.out.println("Entran 50 arroces: " + res(sistema.getProductos().registrarEntrada(admin, "P003", 50, "Distribuidora Central")));
        System.out.println("Stock arroz despues: " + sistema.getProductos().buscarPorCodigo("P003").getStock());
        System.out.println("Cantidad negativa: " + res(sistema.getProductos().registrarEntrada(admin, "P003", -5, "X")));

        // ---- 6. VENTA ----
        System.out.println("\n=== 6. VENTA ===");
        Venta venta = sistema.getVentas().iniciarVenta(cajero);
        System.out.println("Agregar 2 detergentes (por codigo): " + res(sistema.getVentas().agregarAlCarrito(venta, "P001", 2)));
        System.out.println("Agregar 3 leches: " + res(sistema.getVentas().agregarAlCarrito(venta, "P002", 3)));
        System.out.println("Agregar 5 arroces: " + res(sistema.getVentas().agregarAlCarrito(venta, "P003", 5)));
        System.out.println("Codigo que no existe: " + res(sistema.getVentas().agregarAlCarrito(venta, "ZZZ", 1)));
        System.out.println("Mas unidades que el stock: " + res(sistema.getVentas().agregarAlCarrito(venta, "P001", 100)));
        System.out.println("Quitar los arroces del carrito: " + res(sistema.getVentas().quitarDelCarrito(venta, "P003")));
        System.out.println("Total: " + String.format("%.2f", venta.calcularTotal()));
        System.out.println("Cobrar con 1000 (insuficiente): " + res(sistema.getVentas().cobrar(venta, new Efectivo(1000))));
        System.out.println("Cobrar con SINPE de telefono invalido: " + res(sistema.getVentas().cobrar(venta, new Sinpe("12"))));
        System.out.println("Cobrar con 20000: " + res(sistema.getVentas().cobrar(venta, new Efectivo(20000))));
        System.out.println("Vuelto: " + String.format("%.2f", venta.getVuelto()));
        System.out.println("Cobrar otra vez la misma venta: " + res(sistema.getVentas().cobrar(venta, new Efectivo(20000))));

        Venta venta2 = sistema.getVentas().iniciarVenta(admin);
        sistema.getVentas().agregarAlCarrito(venta2, "P003", 4);
        System.out.println("Venta 2 (la hace el admin) por SINPE: " + res(sistema.getVentas().cobrar(venta2, new Sinpe("88887777"))));

        // ---- 7. PERSISTENCIA: se "reinicia" el sistema ----
        System.out.println("\n=== 7. DESPUES DE 'REINICIAR' ===");
        SistemaSupermercado sistema2 = new SistemaSupermercado("datos_prueba3");
        System.out.println("Usuarios guardados: " + sistema2.getUsuarios().listar().size());
        System.out.println("Stock detergente: " + sistema2.getProductos().buscarPorCodigo("P001").getStock() + " (era 20, se vendieron 2)");
        System.out.println("Stock leche: " + sistema2.getProductos().buscarPorCodigo("P002").getStock() + " (era 30, se vendieron 3)");
        System.out.println("Stock arroz: " + sistema2.getProductos().buscarPorCodigo("P003").getStock() + " (3 + 50 - 4)");
        Usuario adminDeNuevo = sistema2.getUsuarios().iniciarSesion("amora", "Admin123*");
        System.out.println("El admin puede volver a entrar: " + (adminDeNuevo != null));
        System.out.println("Siguiente numero de venta: " + sistema2.getVentas().iniciarVenta(adminDeNuevo).getNumero());

        // ---- 8. REPORTES Y ALERTAS ----
        System.out.println("\n=== 8. REPORTES DE HOY ===");
        String hoy = sistema2.getReportes().hoy();
        System.out.println("Fecha: " + hoy);
        System.out.println("Ventas realizadas: " + sistema2.getReportes().cantidadVentas(hoy));
        System.out.println("Dinero recaudado: " + String.format("%.2f", sistema2.getReportes().totalRecaudado(hoy)));
        System.out.println("  en efectivo: " + String.format("%.2f", sistema2.getReportes().totalPorMetodo(hoy, "Efectivo")));
        System.out.println("  en SINPE: " + String.format("%.2f", sistema2.getReportes().totalPorMetodo(hoy, "SINPE Movil")));
        System.out.println("  vendido por lrojas: " + String.format("%.2f", sistema2.getReportes().totalPorCajero(hoy, "lrojas")));
        System.out.println("Unidades vendidas: " + sistema2.getReportes().unidadesVendidas(hoy));
        System.out.println("Unidades que entraron al inventario: " + sistema2.getReportes().unidadesIngresadas(hoy));

        System.out.println("\n=== 9. ALERTAS ===");
        ArrayList<Producto> bajos = sistema2.getProductos().productosBajoStock();
        for (int i = 0; i < bajos.size(); i++) {
            System.out.println("Stock bajo: " + bajos.get(i).getNombre() + " (" + bajos.get(i).getStock() + ")");
        }
        ArrayList<Producto> porVencer = sistema2.getProductos().productosPorVencer();
        for (int i = 0; i < porVencer.size(); i++) {
            System.out.println("Por vencer: " + porVencer.get(i).getNombre());
        }
        System.out.println("Vencidos: " + sistema2.getProductos().productosVencidos().size());
    }
}
