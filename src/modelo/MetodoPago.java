package modelo;

/**
 * CLASE PADRE de los metodos de pago.
 * HERENCIA: Efectivo, Tarjeta y Sinpe extienden esta clase.
 * POLIMORFISMO: cada hija sobrescribe procesarPago(), calcularVuelto() y getDetalle().
 */
public class MetodoPago {

    private String nombre;

    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    // Metodo de ayuda que heredan las hijas: revisa que un texto tenga solo digitos y la longitud exacta.
    protected boolean esNumerico(String texto, int longitud) {
        if (texto == null || texto.length() != longitud) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // ----- Metodos que las clases hijas SOBRESCRIBEN (polimorfismo) -----

    public boolean procesarPago(double total) {
        return false;
    }

    public double calcularVuelto(double total) {
        return 0;
    }

    public String getDetalle() {
        return nombre;
    }
}
