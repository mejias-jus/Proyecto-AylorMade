package principal;

import java.time.LocalDate;
import modelo.Administrador;
import modelo.Cajero;
import modelo.Efectivo;
import modelo.MetodoPago;
import modelo.Producto;
import modelo.ProductoCanastaBasica;
import modelo.ProductoPerecedero;
import modelo.Sinpe;
import modelo.Sucursal;
import modelo.Tarjeta;
import modelo.Usuario;
import modelo.Venta;

/**
 * Clase de prueba SOLO para la Parte 1: comprueba el modelo por consola, sin pantallas.
 * Cuando hagamos la interfaz grafica ya no se necesita.
 */
public class PruebaModelo {

    public static void main(String[] args) {

        // ---- Usuarios: herencia y polimorfismo ----
        Administrador admin = new Administrador("Ana Mora", "amora", "ana@correo.com", "hash_admin");
        Cajero cajero = new Cajero("Luis Rojas", "lrojas", "luis@correo.com", "hash_cajero", 2);

        Usuario[] usuarios = {admin, cajero};
        System.out.println("=== USUARIOS ===");
        for (int i = 0; i < usuarios.length; i++) {
            System.out.println(usuarios[i].getNombre() + " | rol: " + usuarios[i].getRol()
                    + " | modifica precios: " + usuarios[i].puedeModificarPrecios()
                    + " | ve reportes: " + usuarios[i].puedeVerReportes());
        }

        // ---- Productos: polimorfismo en el precio final ----
        Producto detergente = new Producto("P001", "Detergente", 2500, 20, 5);
        ProductoPerecedero leche = new ProductoPerecedero("P002", "Leche 1L", 1200, 30, 10, LocalDate.now().plusDays(2));
        ProductoCanastaBasica arroz = new ProductoCanastaBasica("P003", "Arroz 1kg", 1500, 50, 10);
        ProductoPerecedero yogurtVencido = new ProductoPerecedero("P004", "Yogurt", 900, 10, 3, LocalDate.now().minusDays(1));

        Producto[] productos = {detergente, leche, arroz, yogurtVencido};
        System.out.println("\n=== PRODUCTOS (precio base -> precio final) ===");
        for (int i = 0; i < productos.length; i++) {
            System.out.println(productos[i].getNombre() + " [" + productos[i].getTipo() + "] "
                    + productos[i].getPrecioBase() + " -> " + String.format("%.2f", productos[i].calcularPrecioFinal())
                    + " | se puede vender: " + productos[i].puedeVenderse());
        }

        // ---- Venta completa con pago en efectivo ----
        System.out.println("\n=== VENTA 1 (efectivo) ===");
        Venta venta = new Venta(1, cajero);
        System.out.println("Agregar 2 detergentes: " + venta.agregarProducto(detergente, 2));
        System.out.println("Agregar 3 leches: " + venta.agregarProducto(leche, 3));
        System.out.println("Agregar 1 arroz: " + venta.agregarProducto(arroz, 1));
        System.out.println("Agregar 100 arroces (no hay stock): " + venta.agregarProducto(arroz, 100));
        System.out.println("Agregar yogurt vencido: " + venta.agregarProducto(yogurtVencido, 1));
        System.out.println("Stock detergente ANTES de cobrar: " + detergente.getStock());

        MetodoPago pagoMalo = new Efectivo(1000);
        System.out.println("Cobrar con monto insuficiente: " + venta.cerrarVenta(pagoMalo));
        System.out.println("Stock detergente despues del cobro fallido: " + detergente.getStock());

        MetodoPago pago = new Efectivo(20000);
        System.out.println("Cobrar con 20000: " + venta.cerrarVenta(pago));
        System.out.println("Stock detergente DESPUES de cobrar: " + detergente.getStock());
        System.out.println();
        System.out.println(venta.generarFactura());

        // ---- Venta con otros metodos de pago (polimorfismo) ----
        System.out.println("=== VENTA 2 (tarjeta) y VENTA 3 (SINPE) ===");
        Venta venta2 = new Venta(2, cajero);
        venta2.agregarProducto(arroz, 5);
        System.out.println("Tarjeta valida: " + venta2.cerrarVenta(new Tarjeta("4821")));

        Venta venta3 = new Venta(3, cajero);
        venta3.agregarProducto(arroz, 2);
        System.out.println("SINPE con telefono invalido: " + venta3.cerrarVenta(new Sinpe("123")));
        System.out.println("SINPE con telefono valido: " + venta3.cerrarVenta(new Sinpe("88887777")));

        // ---- Sucursales ----
        System.out.println("\n=== SUCURSALES ===");
        Sucursal s1 = new Sucursal("Super Centro", "San Jose centro", 9.9281, -84.0907, 8);
        Sucursal s2 = new Sucursal("Minisuper La Esquina", "Barrio Escalante", 9.9340, -84.0680, 2);
        System.out.println(s1.getNombre() + " -> " + s1.getTipo());
        System.out.println(s2.getNombre() + " -> " + s2.getTipo());
    }
}
