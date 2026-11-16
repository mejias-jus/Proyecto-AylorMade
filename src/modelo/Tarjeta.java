package modelo;

/**
 * HERENCIA + POLIMORFISMO: el pago con tarjeta se aprueba si los ultimos 4 digitos son validos.
 * Hereda calcularVuelto() del padre (siempre 0).
 */
public class Tarjeta extends MetodoPago {

    private String ultimosDigitos;

    public Tarjeta(String ultimosDigitos) {
        super("Tarjeta");
        this.ultimosDigitos = "";
        setUltimosDigitos(ultimosDigitos);
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public void setUltimosDigitos(String ultimosDigitos) {
        if (esNumerico(ultimosDigitos, 4)) {
            this.ultimosDigitos = ultimosDigitos;
        }
    }

    @Override
    public boolean procesarPago(double total) {
        return esNumerico(ultimosDigitos, 4);
    }

    @Override
    public String getDetalle() {
        return "Tarjeta (terminada en " + ultimosDigitos + ")";
    }
}
