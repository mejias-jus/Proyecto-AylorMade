package modelo;

/**
 * HERENCIA + POLIMORFISMO: el pago en efectivo se aprueba si el monto recibido alcanza
 * y es el unico que calcula vuelto.
 */
public class Efectivo extends MetodoPago {

    private double montoRecibido;

    public Efectivo(double montoRecibido) {
        super("Efectivo");
        this.montoRecibido = 0;
        setMontoRecibido(montoRecibido);
    }

    public double getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(double montoRecibido) {
        if (montoRecibido >= 0) {
            this.montoRecibido = montoRecibido;
        }
    }

    @Override
    public boolean procesarPago(double total) {
        return montoRecibido >= total;
    }

    @Override
    public double calcularVuelto(double total) {
        if (procesarPago(total)) {
            return montoRecibido - total;
        }
        return 0;
    }

    @Override
    public String getDetalle() {
        return "Efectivo (recibido: " + String.format("%.2f", montoRecibido) + ")";
    }
}
