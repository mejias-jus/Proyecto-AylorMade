package logica;

import datos.ArchivoTexto;
import datos.ArchivoUsuarios;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.Administrador;
import modelo.Cajero;
import modelo.Usuario;

/**
 * Reglas de negocio de los usuarios: inicio de sesion, registro y permisos.
 * Los metodos que pueden fallar devuelven un String: "" si todo salio bien, o el mensaje de error
 * que la pantalla puede mostrar tal cual.
 * REGLA: el primer usuario del sistema es siempre el Administrador; despues, solo un
 * Administrador puede crear usuarios nuevos (asi nadie se hace administrador por su cuenta).
 */
public class ServicioUsuarios {

    private static final int MINUTOS_CODIGO = 15;
    private static final int MAX_INTENTOS = 3;

    private ArchivoUsuarios archivo;
    private ServicioCorreo correo;
    private ArrayList<Usuario> usuarios;

    // Estado de una recuperacion de contrasena en curso (solo vive en memoria, nunca se guarda).
    private String codigoRecuperacion = "";
    private String usuarioRecuperacion = "";
    private LocalDateTime expiraRecuperacion = null;
    private int intentosRecuperacion = 0;

    public ServicioUsuarios(ArchivoUsuarios archivo, ServicioCorreo correo) {
        this.archivo = archivo;
        this.correo = correo;
        this.usuarios = archivo.cargar();
    }

    public boolean hayUsuarios() {
        return !usuarios.isEmpty();
    }

    public ArrayList<Usuario> listar() {
        return usuarios;
    }

    public Usuario buscar(String nombreUsuario) {
        if (nombreUsuario == null) {
            return null;
        }
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getNombreUsuario().equalsIgnoreCase(nombreUsuario.trim())) {
                return usuarios.get(i);
            }
        }
        return null;
    }

    // Devuelve el usuario si los datos son correctos, o null si no. No dice cual de los dos fallo.
    public Usuario iniciarSesion(String nombreUsuario, String clave) {
        Usuario u = buscar(nombreUsuario);
        if (u == null) {
            return null;
        }
        if (Seguridad.verificar(clave, u.getContrasenaHash())) {
            return u;
        }
        return null;
    }

    public String registrarPrimerAdministrador(String nombre, String nombreUsuario, String correo, String clave) {
        if (hayUsuarios()) {
            return "Ya existe un administrador. Solo un administrador puede crear usuarios.";
        }
        return crear("Administrador", nombre, nombreUsuario, correo, clave, 0);
    }

    public String registrar(Usuario quien, String rol, String nombre, String nombreUsuario,
            String correo, String clave, int numeroCaja) {
        if (quien == null || !quien.puedeGestionarUsuarios()) {
            return "Solo un administrador puede crear usuarios.";
        }
        return crear(rol, nombre, nombreUsuario, correo, clave, numeroCaja);
    }

    public String cambiarClave(Usuario usuario, String claveActual, String claveNueva) {
        if (usuario == null || !Seguridad.verificar(claveActual, usuario.getContrasenaHash())) {
            return "La contraseña actual es incorrecta.";
        }
        String error = Seguridad.validarClave(claveNueva);
        if (!error.isEmpty()) {
            return error;
        }
        String hashViejo = usuario.getContrasenaHash();
        usuario.setContrasenaHash(Seguridad.encriptar(claveNueva));
        if (!archivo.guardarTodos(usuarios)) {
            usuario.setContrasenaHash(hashViejo);
            return "No se pudo guardar el cambio de contraseña.";
        }
        return "";
    }

    public String eliminar(Usuario quien, String nombreUsuario) {
        if (quien == null || !quien.puedeGestionarUsuarios()) {
            return "Solo un administrador puede eliminar usuarios.";
        }
        Usuario objetivo = buscar(nombreUsuario);
        if (objetivo == null) {
            return "El usuario no existe.";
        }
        if (objetivo.getNombreUsuario().equalsIgnoreCase(quien.getNombreUsuario())) {
            return "No puede eliminar su propio usuario.";
        }
        usuarios.remove(objetivo);
        if (!archivo.guardarTodos(usuarios)) {
            usuarios.add(objetivo);
            return "No se pudo guardar el cambio.";
        }
        return "";
    }

    /**
     * Paso 1 de "Olvide mi contrasena": envia un codigo de 6 digitos al correo registrado.
     * Por seguridad no se revela si el usuario existe: si no existe, tambien devuelve "" y no envia nada.
     * La pantalla debe decir: "Si el usuario existe, se envio un codigo al correo registrado".
     */
    public String solicitarCodigoRecuperacion(String usuarioOCorreo) {
        if (usuarioOCorreo == null || usuarioOCorreo.trim().isEmpty()) {
            return "Escriba su usuario o correo.";
        }
        if (!correo.estaConfigurado()) {
            return "El envío de correos no está configurado. " + correo.problemaConfiguracion();
        }
        Usuario u = buscarPorUsuarioOCorreo(usuarioOCorreo);
        if (u == null) {
            return "";
        }
        String codigo = String.format("%06d", new SecureRandom().nextInt(1000000));
        String mensaje = "Hola " + u.getNombre() + ",\n\n"
                + "Recibimos una solicitud para restablecer su contraseña en AylorMade.\n"
                + "Su código de verificación es: " + codigo + "\n\n"
                + "El código vence en " + MINUTOS_CODIGO + " minutos. "
                + "Si usted no lo solicitó, ignore este mensaje.";
        String error = correo.enviar(u.getCorreo(), "AylorMade - Código para restablecer su contraseña", mensaje);
        if (!error.isEmpty()) {
            return error;
        }
        codigoRecuperacion = codigo;
        usuarioRecuperacion = u.getNombreUsuario();
        expiraRecuperacion = LocalDateTime.now().plusMinutes(MINUTOS_CODIGO);
        intentosRecuperacion = 0;
        return "";
    }

    // Paso 2: con el codigo recibido, se escribe la contrasena nueva.
    public String restablecerClave(String usuarioOCorreo, String codigo, String claveNueva) {
        if (codigoRecuperacion.isEmpty() || expiraRecuperacion == null) {
            return "Primero solicite un código de recuperación.";
        }
        if (LocalDateTime.now().isAfter(expiraRecuperacion)) {
            limpiarRecuperacion();
            return "El código venció. Solicite uno nuevo.";
        }
        Usuario u = buscarPorUsuarioOCorreo(usuarioOCorreo);
        if (u == null || !u.getNombreUsuario().equals(usuarioRecuperacion)
                || codigo == null || !codigoRecuperacion.equals(codigo.trim())) {
            intentosRecuperacion++;
            if (intentosRecuperacion >= MAX_INTENTOS) {
                limpiarRecuperacion();
                return "Demasiados intentos fallidos. Solicite un código nuevo.";
            }
            return "Código incorrecto.";
        }
        String errorClave = Seguridad.validarClave(claveNueva);
        if (!errorClave.isEmpty()) {
            return errorClave; // el codigo sigue vigente para volver a intentar
        }
        String hashViejo = u.getContrasenaHash();
        u.setContrasenaHash(Seguridad.encriptar(claveNueva));
        if (!archivo.guardarTodos(usuarios)) {
            u.setContrasenaHash(hashViejo);
            return "No se pudo guardar la nueva contraseña.";
        }
        limpiarRecuperacion();
        return "";
    }

    // ----- Metodos privados de ayuda -----

    private Usuario buscarPorUsuarioOCorreo(String texto) {
        Usuario u = buscar(texto);
        if (u != null) {
            return u;
        }
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getCorreo().equalsIgnoreCase(texto.trim())) {
                return usuarios.get(i);
            }
        }
        return null;
    }

    private void limpiarRecuperacion() {
        codigoRecuperacion = "";
        usuarioRecuperacion = "";
        expiraRecuperacion = null;
        intentosRecuperacion = 0;
    }


    private String crear(String rol, String nombre, String nombreUsuario, String correo, String clave, int numeroCaja) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombreUsuario == null || nombreUsuario.trim().length() < 3) {
            return "El nombre de usuario debe tener al menos 3 caracteres.";
        }
        if (nombreUsuario.contains(" ")) {
            return "El nombre de usuario no puede tener espacios.";
        }
        if (correo == null || !correo.contains("@") || !correo.contains(".")) {
            return "El correo no es válido.";
        }
        if (nombre.contains(ArchivoTexto.SEPARADOR) || nombreUsuario.contains(ArchivoTexto.SEPARADOR)
                || correo.contains(ArchivoTexto.SEPARADOR)) {
            return "Los datos no pueden contener el símbolo " + ArchivoTexto.SEPARADOR;
        }
        if (buscar(nombreUsuario) != null) {
            return "Ese nombre de usuario ya existe.";
        }
        String errorClave = Seguridad.validarClave(clave);
        if (!errorClave.isEmpty()) {
            return errorClave;
        }
        Usuario nuevo;
        String hash = Seguridad.encriptar(clave);
        if (rol.equals("Administrador")) {
            nuevo = new Administrador(nombre, nombreUsuario, correo, hash);
        } else if (rol.equals("Cajero")) {
            if (numeroCaja <= 0) {
                return "El número de caja debe ser mayor que 0.";
            }
            nuevo = new Cajero(nombre, nombreUsuario, correo, hash, numeroCaja);
        } else {
            return "El rol no es válido.";
        }
        usuarios.add(nuevo);
        if (!archivo.agregar(nuevo)) {
            usuarios.remove(nuevo);
            return "No se pudo guardar el usuario en el archivo.";
        }
        return "";
    }
}
