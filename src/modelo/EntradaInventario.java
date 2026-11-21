package modelo;

/**
 * Registro de un ingreso de mercaderia al inventario (cuantos productos entraron, cuando y quien los recibio).
 * ENCAPSULAMIENTO: atributos privados, solo se pueden consultar.
 */
public class EntradaInventario {

    private String fecha;
    private String codigoProducto;
    private String nombreProducto;
    private int cantidad;
    private String usuario;
    private String proveedor;

    public EntradaInventario(String fecha, String codigoProducto, String nombreProducto,
            int cantidad, String usuario, String proveedor) {
        this.fecha = fecha;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.usuario = usuario;
        this.proveedor = proveedor;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getProveedor() {
        return proveedor;
    }
}
