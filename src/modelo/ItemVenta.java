package modelo;

/**
 * Una linea de la venta: un producto y la cantidad que se lleva.
 * RELACION entre objetos: ItemVenta "tiene un" Producto.
 */
public class ItemVenta {

    private Producto producto;
    private int cantidad;

    public ItemVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = 1;
        setCantidad(cantidad);
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad > 0) {
            this.cantidad = cantidad;
        }
    }

    // POLIMORFISMO en accion: cada producto calcula su propio precio final.
    public double getSubtotal() {
        return producto.calcularPrecioFinal() * cantidad;
    }
}
