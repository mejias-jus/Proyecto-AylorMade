package modelo;

/**
 * HERENCIA: Cajero es un Usuario y ademas tiene su propio atributo (numeroCaja).
 * No sobrescribe los permisos: hereda los del padre (todos en false).
 */
public class Cajero extends Usuario {

    private int numeroCaja;

    public Cajero(String nombre, String nombreUsuario, String correo, String contrasenaHash, int numeroCaja) {
        super(nombre, nombreUsuario, correo, contrasenaHash);
        this.numeroCaja = 1;
        setNumeroCaja(numeroCaja);
    }

    public int getNumeroCaja() {
        return numeroCaja;
    }

    public void setNumeroCaja(int numeroCaja) {
        if (numeroCaja > 0) {
            this.numeroCaja = numeroCaja;
        }
    }

    @Override
    public String getRol() {
        return "Cajero";
    }
}
