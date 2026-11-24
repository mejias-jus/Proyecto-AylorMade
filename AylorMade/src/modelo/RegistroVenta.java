package modelo;

/**
 * Una venta ya guardada en ventas.txt. Se usa para reportes y para saber el siguiente numero de venta.
 * ENCAPSULAMIENTO: atributos privados, solo se pueden consultar.
 */
public class RegistroVenta {

    private int numero;
    private String fecha;
    private String usuarioCajero;
    private String metodoPago;
    private double total;
    private double vuelto;
    private String items; // formato: codigo:cantidad:precioUnitario,codigo:cantidad:precioUnitario

    public RegistroVenta(int numero, String fecha, String usuarioCajero, String metodoPago,
            double total, double vuelto, String items) {
        this.numero = numero;
        this.fecha = fecha;
        this.usuarioCajero = usuarioCajero;
        this.metodoPago = metodoPago;
        this.total = total;
        this.vuelto = vuelto;
        this.items = items;
    }

    public int getNumero() {
        return numero;
    }

    public String getFecha() {
        return fecha;
    }

    public String getUsuarioCajero() {
        return usuarioCajero;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public double getTotal() {
        return total;
    }

    public double getVuelto() {
        return vuelto;
    }

    public String getItems() {
        return items;
    }
}
