package datos;

import java.util.ArrayList;
import modelo.Administrador;
import modelo.Cajero;
import modelo.Usuario;

/**
 * Lee y escribe usuarios.txt. Una linea por usuario:
 * rol|nombre|nombreUsuario|correo|contrasenaHash|numeroCaja
 * POLIMORFISMO: para guardar no importa si es Administrador o Cajero, se usan getRol() y getNumeroCaja().
 */
public class ArchivoUsuarios extends ArchivoTexto {

    public ArchivoUsuarios(String ruta) {
        super(ruta);
    }

    public ArrayList<Usuario> cargar() {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        ArrayList<String> lineas = leerLineas();
        for (int i = 0; i < lineas.size(); i++) {
            Usuario u = convertirLinea(lineas.get(i));
            if (u != null) {
                usuarios.add(u);
            } // si la linea esta danada, se ignora y el programa sigue funcionando
        }
        return usuarios;
    }

    public boolean guardarTodos(ArrayList<Usuario> usuarios) {
        ArrayList<String> lineas = new ArrayList<>();
        for (int i = 0; i < usuarios.size(); i++) {
            lineas.add(convertirUsuario(usuarios.get(i)));
        }
        return escribirLineas(lineas);
    }

    public boolean agregar(Usuario usuario) {
        return agregarLinea(convertirUsuario(usuario));
    }

    private String convertirUsuario(Usuario u) {
        return limpiar(u.getRol()) + SEPARADOR
                + limpiar(u.getNombre()) + SEPARADOR
                + limpiar(u.getNombreUsuario()) + SEPARADOR
                + limpiar(u.getCorreo()) + SEPARADOR
                + limpiar(u.getContrasenaHash()) + SEPARADOR
                + u.getNumeroCaja();
    }

    // Devuelve null si la linea no es valida.
    private Usuario convertirLinea(String linea) {
        String[] p = separar(linea);
        if (p.length != 6) {
            return null;
        }
        Usuario u = null;
        try {
            if (p[0].equals("Administrador")) {
                u = new Administrador(p[1], p[2], p[3], p[4]);
            } else if (p[0].equals("Cajero")) {
                u = new Cajero(p[1], p[2], p[3], p[4], Integer.parseInt(p[5]));
            }
        } catch (Exception e) {
            return null;
        }
        // Los setters ignoran datos invalidos, asi que se revisa que los datos clave quedaron cargados.
        if (u == null || u.getNombreUsuario().isEmpty() || u.getContrasenaHash().isEmpty()) {
            return null;
        }
        return u;
    }
}
