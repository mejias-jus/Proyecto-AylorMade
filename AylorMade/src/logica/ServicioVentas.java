package logica;

import datos.ArchivoVentas;
import modelo.MetodoPago;
import modelo.Producto;
import modelo.Usuario;
import modelo.Venta;

/**
 * Reglas de negocio de la caja: armar el carrito y cobrar.
 * ORDEN DE ESTABILIDAD al cobrar: 1) se aprueba el pago y se descuenta el stock en memoria,
 * 2) la venta se guarda en ventas.txt, 3) se guarda el inventario en productos.txt.
 * Los metodos que pueden fallar devuelven un String: "" si todo salio bien, o el mensaje de error.
 */
public class ServicioVentas {

    private ArchivoVentas archivoVentas;
    private ServicioProductos servicioProductos;
    private ServicioCorreo correo;

    public ServicioVentas(ArchivoVentas archivoVentas, ServicioProductos servicioProductos, ServicioCorreo correo) {
        this.archivoVentas = archivoVentas;
        this.servicioProductos = servicioProductos;
        this.correo = correo;
    }

    public Venta iniciarVenta(Usuario vendedor) {
        if (vendedor == null) {
            return null;
        }
        return new Venta(archivoVentas.obtenerSiguienteNumero(), vendedor);
    }

    // Busca el producto por su codigo (o codigo de barras) y lo agrega al carrito.
    public String agregarAlCarrito(Venta venta, String codigo, int cantidad) {
        if (venta == null || venta.estaCerrada()) {
            return "No hay una venta abierta.";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que 0.";
        }
        Producto p = servicioProductos.buscarPorCodigo(codigo);
        if (p == null) {
            return "No existe un producto con ese código.";
        }
        if (p.estaVencido()) {
            return "El producto está vencido y no se puede vender.";
        }
        if (!p.puedeVenderse()) {
            return "El producto está agotado.";
        }
        if (!venta.agregarProducto(p, cantidad)) {
            return "No hay suficiente stock. Disponible: " + p.getStock();
        }
        return "";
    }

    public String quitarDelCarrito(Venta venta, String codigo) {
        if (venta == null || !venta.quitarProducto(codigo)) {
            return "El producto no está en el carrito.";
        }
        return "";
    }

    // El correo del cliente es opcional: "" es valido. Si se escribe, debe tener forma de correo.
    public String validarCorreoCliente(String correoCliente) {
        if (correoCliente == null || correoCliente.trim().isEmpty()) {
            return "";
        }
        String c = correoCliente.trim();
        if (!c.contains("@") || !c.contains(".") || c.contains(" ")) {
            return "El correo del cliente no es válido.";
        }
        return "";
    }

    /**
     * Envia la factura de una venta ya cobrada al correo del cliente (por Gmail, con Jakarta Mail).
     * Si falla, la venta NO se pierde: ya quedo guardada antes de intentar enviar el correo.
     */
    public String enviarFactura(Venta venta, String correoCliente) {
        if (venta == null || !venta.estaCerrada()) {
            return "La venta todavía no se ha cobrado.";
        }
        String error = validarCorreoCliente(correoCliente);
        if (!error.isEmpty()) {
            return error;
        }
        String mensaje = "Gracias por su compra en AylorMade.\n\n" + venta.generarFactura() + "\nVuelva pronto.";
        return correo.enviar(correoCliente.trim(), "AylorMade - Factura N. " + venta.getNumero(), mensaje);
    }

    public String cobrar(Venta venta, MetodoPago metodo) {
        if (venta == null || venta.estaCerrada()) {
            return "No hay una venta abierta.";
        }
        if (venta.getItems().isEmpty()) {
            return "El carrito está vacío.";
        }
        if (metodo == null) {
            return "Debe elegir un método de pago.";
        }
        if (!metodo.procesarPago(venta.calcularTotal())) {
            return "El pago no fue aprobado: revise el monto o los datos del método de pago.";
        }
        if (!venta.cerrarVenta(metodo)) {
            return "No se pudo cobrar: el stock cambió. Revise el carrito.";
        }
        boolean ventaGuardada = archivoVentas.guardarVenta(venta);
        boolean inventarioGuardado = servicioProductos.guardarCambios();
        if (!ventaGuardada || !inventarioGuardado) {
            return "La venta se cobró, pero hubo un problema al guardar los archivos. Avise al administrador.";
        }
        return "";
    }
}
