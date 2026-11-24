package logica;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Seguridad de contrasenas con la libreria jBCrypt.
 * Las contrasenas NUNCA se guardan tal cual: se guarda su hash, que no se puede revertir.
 * Cada hash lleva una "sal" aleatoria, asi dos usuarios con la misma contrasena tienen hashes distintos.
 */
public class Seguridad {

    public static final int LARGO_MINIMO_CLAVE = 8;

    // Convierte la contrasena en un hash (lo que se guarda en usuarios.txt).
    public static String encriptar(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt());
    }

    // Compara una contrasena escrita con el hash guardado.
    public static boolean verificar(String clave, String hash) {
        if (clave == null || hash == null || hash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(clave, hash);
        } catch (Exception e) {
            return false; // hash danado o con formato invalido
        }
    }

    // Devuelve "" si la contrasena cumple las reglas; si no, el mensaje de error.
    public static String validarClave(String clave) {
        if (clave == null || clave.length() < LARGO_MINIMO_CLAVE) {
            return "La contraseña debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres.";
        }
        boolean hayLetra = false;
        boolean hayNumero = false;
        for (int i = 0; i < clave.length(); i++) {
            if (Character.isLetter(clave.charAt(i))) {
                hayLetra = true;
            }
            if (Character.isDigit(clave.charAt(i))) {
                hayNumero = true;
            }
        }
        if (!hayLetra || !hayNumero) {
            return "La contraseña debe combinar letras y números.";
        }
        return "";
    }
}
