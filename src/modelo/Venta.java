package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Una venta completa. RELACIONES: tiene un Cajero, varios ItemVenta y un MetodoPago.
 * ESTABILIDAD: el stock NO se descuenta al agregar productos, solo cuando el pago
 * se aprueba en cerrarVenta(). Si algo falla, el inventario no queda danado.
 */
public class Venta {

    private int numero;
    private String fecha;
    private Cajero cajero;
    private ArrayList<ItemVenta> items;
    private MetodoPago metodoPago;
    private boolean cerrada;

    public Venta(int numero, Cajero cajero) {
        this.numero = numero;
        this.cajero = cajero;
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.items = new ArrayList<>();
        this.metodoPago = null;
        this.cerrada = false;
    }

    public int getNumero() {
        return numero;
    }

    public String getFecha() {
        return fecha;
    }

    public Cajero getCajero() {
        return cajero;
    }

    public ArrayList<ItemVenta> getItems() {
        return items;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public boolean estaCerrada() {
        return cerrada;
    }

    // Cuantas unidades de este producto ya estan en el carrito.
    private int cantidadEnCarrito(Producto producto) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProducto().getCodigo().equals(producto.getCodigo())) {
                return items.get(i).getCantidad();
            }
        }
        return 0;
    }

    public boolean agregarProducto(Producto producto, int cantidad) {
        if (cerrada || producto == null || cantidad <= 0) {
            return false;
        }
        if (!producto.puedeVenderse()) {
            return false;
        }
        int yaEnCarrito = cantidadEnCarrito(producto);
        if (!producto.hayStock(yaEnCarrito + cantidad)) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProducto().getCodigo().equals(producto.getCodigo())) {
                items.get(i).setCantidad(yaEnCarrito + cantidad);
                return true;
            }
        }
        items.add(new ItemVenta(producto, cantidad));
        return true;
    }

    public double calcularTotal() {
        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            total = total + items.get(i).getSubtotal();
        }
        return total;
    }

    public double getVuelto() {
        if (metodoPago == null) {
            return 0;
        }
        return metodoPago.calcularVuelto(calcularTotal());
    }

    public boolean cerrarVenta(MetodoPago metodo) {
        if (cerrada || items.isEmpty() || metodo == null) {
            return false;
        }
        // 1. Verificar que todavia hay stock de todo
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).getProducto().hayStock(items.get(i).getCantidad())) {
                return false;
            }
        }
        // 2. Procesar el pago (polimorfismo: cada metodo valida a su manera)
        if (!metodo.procesarPago(calcularTotal())) {
            return false;
        }
        // 3. Solo ahora se descuenta el inventario
        for (int i = 0; i < items.size(); i++) {
            items.get(i).getProducto().descontarStock(items.get(i).getCantidad());
        }
        this.metodoPago = metodo;
        this.cerrada = true;
        return true;
    }

    // Texto de la factura: sirve para mostrarla en pantalla y luego enviarla por correo.
    public String generarFactura() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== FACTURA N. ").append(numero).append(" ==========\n");
        sb.append("Fecha: ").append(fecha).append("\n");
        sb.append("Cajero: ").append(cajero.getNombre()).append(" (Caja ").append(cajero.getNumeroCaja()).append(")\n");
        sb.append("--------------------------------------\n");
        for (int i = 0; i < items.size(); i++) {
            ItemVenta item = items.get(i);
            sb.append(item.getCantidad()).append(" x ").append(item.getProducto().getNombre())
                    .append("  ").append(String.format("%.2f", item.getSubtotal())).append("\n");
        }
        sb.append("--------------------------------------\n");
        sb.append("TOTAL: ").append(String.format("%.2f", calcularTotal())).append("\n");
        if (metodoPago != null) {
            sb.append("Pago: ").append(metodoPago.getDetalle()).append("\n");
            sb.append("Vuelto: ").append(String.format("%.2f", getVuelto())).append("\n");
        }
        return sb.toString();
    }
}
