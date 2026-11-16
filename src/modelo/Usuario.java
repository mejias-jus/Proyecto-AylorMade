package modelo;

/**
 * CLASE PADRE de los usuarios del sistema.
 * ENCAPSULAMIENTO: atributos privados + getters/setters con validacion.
 * HERENCIA: Administrador y Cajero extienden esta clase.
 * POLIMORFISMO: los metodos getRol() y puede...() se sobrescriben en las hijas.
 */
public class Usuario {

    private String nombre;
    private String nombreUsuario;
    private String correo;
    private String contrasenaHash; // aqui se guarda la contrasena ya encriptada (Parte 3)

    public Usuario(String nombre, String nombreUsuario, String correo, String contrasenaHash) {
        this.nombre = "";
        this.nombreUsuario = "";
        this.correo = "";
        this.contrasenaHash = "";
        setNombre(nombre);
        setNombreUsuario(nombreUsuario);
        setCorreo(correo);
        setContrasenaHash(contrasenaHash);
    }

    public String getNombre() {
        return nombre;
    }

    // Si el dato no es valido, el setter no lo acepta y se conserva el valor anterior.
    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        }
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        if (nombreUsuario != null && !nombreUsuario.trim().isEmpty()) {
            this.nombreUsuario = nombreUsuario.trim();
        }
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo != null && correo.contains("@") && correo.contains(".")) {
            this.correo = correo.trim();
        }
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        if (contrasenaHash != null && !contrasenaHash.isEmpty()) {
            this.contrasenaHash = contrasenaHash;
        }
    }

    // ----- Metodos que las clases hijas SOBRESCRIBEN (polimorfismo) -----

    public String getRol() {
        return "Usuario";
    }

    public boolean puedeModificarPrecios() {
        return false;
    }

    public boolean puedeVerReportes() {
        return false;
    }

    public boolean puedeAnularVentas() {
        return false;
    }
}
