package modelo;

/**
 * HERENCIA + POLIMORFISMO: el pago por SINPE Movil se aprueba si el telefono tiene 8 digitos.
 */
public class Sinpe extends MetodoPago {

    private String telefono;

    public Sinpe(String telefono) {
        super("SINPE Movil");
        this.telefono = "";
        setTelefono(telefono);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (esNumerico(telefono, 8)) {
            this.telefono = telefono;
        }
    }

    @Override
    public boolean procesarPago(double total) {
        return esNumerico(telefono, 8);
    }

    @Override
    public String getDetalle() {
        return "SINPE Movil (tel: " + telefono + ")";
    }
}
