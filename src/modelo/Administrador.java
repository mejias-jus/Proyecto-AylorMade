package modelo;

/**
 * HERENCIA: Administrador es un Usuario.
 * POLIMORFISMO: sobrescribe los metodos del padre con su propio comportamiento.
 */
public class Administrador extends Usuario {

    public Administrador(String nombre, String nombreUsuario, String correo, String contrasenaHash) {
        super(nombre, nombreUsuario, correo, contrasenaHash);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }

    @Override
    public boolean puedeModificarPrecios() {
        return true;
    }

    @Override
    public boolean puedeVerReportes() {
        return true;
    }

    @Override
    public boolean puedeAnularVentas() {
        return true;
    }
}
