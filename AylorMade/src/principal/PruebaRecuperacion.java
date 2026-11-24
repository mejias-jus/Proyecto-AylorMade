package principal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import datos.ArchivoConfiguracion;
import logica.ServicioCorreo;
import logica.SistemaSupermercado;

/**
 * Prueba de la recuperacion de contrasena y del servicio de correo (sin enviar correos reales).
 */
public class PruebaRecuperacion {

    private static String res(String error) {
        if (error.isEmpty()) {
            return "OK";
        }
        return "RECHAZADO -> " + error;
    }

    private static String leerCodigo(String mensaje) {
        int pos = mensaje.indexOf("es: ") + 4;
        return mensaje.substring(pos, pos + 6);
    }

    public static void main(String[] args) throws IOException {
        Files.deleteIfExists(Paths.get("datos_prueba4/usuarios.txt"));
        CorreoDePrueba correo = new CorreoDePrueba();
        SistemaSupermercado sistema = new SistemaSupermercado("datos_prueba4", correo);
        sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "Admin123*");

        System.out.println("=== RECUPERAR CONTRASENA ===");
        System.out.println("Pedir codigo sin escribir nada: " + res(sistema.getUsuarios().solicitarCodigoRecuperacion("")));
        System.out.println("Usuario que no existe (no se revela): " + res(sistema.getUsuarios().solicitarCodigoRecuperacion("nadie")) + " | correos enviados: '" + correo.getUltimoDestinatario() + "'");
        System.out.println("Restablecer sin pedir codigo: " + res(sistema.getUsuarios().restablecerClave("amora", "123456", "NuevaClave1")));

        System.out.println("Pedir codigo con el usuario: " + res(sistema.getUsuarios().solicitarCodigoRecuperacion("amora")));
        System.out.println("  se envio a: " + correo.getUltimoDestinatario());
        String codigo = leerCodigo(correo.getUltimoMensaje());
        System.out.println("  mensaje:\n" + correo.getUltimoMensaje().replace("\n", "\n    "));

        System.out.println("Codigo incorrecto (intento 1): " + res(sistema.getUsuarios().restablecerClave("amora", "000000", "NuevaClave1")));
        System.out.println("Codigo incorrecto (intento 2): " + res(sistema.getUsuarios().restablecerClave("amora", "000001", "NuevaClave1")));
        System.out.println("Codigo incorrecto (intento 3): " + res(sistema.getUsuarios().restablecerClave("amora", "000002", "NuevaClave1")));
        System.out.println("Ahora hasta el codigo bueno ya no sirve: " + res(sistema.getUsuarios().restablecerClave("amora", codigo, "NuevaClave1")));

        System.out.println("\nPedir codigo nuevo usando el CORREO en vez del usuario: " + res(sistema.getUsuarios().solicitarCodigoRecuperacion("ANA@correo.com")));
        codigo = leerCodigo(correo.getUltimoMensaje());
        System.out.println("Codigo bueno pero clave debil: " + res(sistema.getUsuarios().restablecerClave("amora", codigo, "123")));
        System.out.println("Codigo bueno y clave nueva: " + res(sistema.getUsuarios().restablecerClave("amora", codigo, "NuevaClave1")));
        System.out.println("Entra con la clave vieja: " + (sistema.getUsuarios().iniciarSesion("amora", "Admin123*") != null));
        System.out.println("Entra con la clave nueva: " + (sistema.getUsuarios().iniciarSesion("amora", "NuevaClave1") != null));
        System.out.println("Reusar el mismo codigo: " + res(sistema.getUsuarios().restablecerClave("amora", codigo, "OtraClave22")));

        // ---- Servicio de correo REAL (sin internet): debe fallar con un mensaje, sin caerse ----
        System.out.println("\n=== SERVICIO DE CORREO REAL ===");
        ServicioCorreo sinConfigurar = new ServicioCorreo(new ArchivoConfiguracion("datos_prueba4/no_existe.txt"));
        System.out.println("Sin archivo de configuracion, configurado: " + sinConfigurar.estaConfigurado());
        System.out.println("Enviar sin configurar: " + res(sinConfigurar.enviar("a@b.com", "Hola", "Texto")));

        ArrayList<String> config = new ArrayList<>();
        config.add("# configuracion falsa para probar el manejo de errores");
        config.add("servidor=localhost");
        config.add("puerto=2525");
        config.add("usuario=prueba@gmail.com");
        config.add("clave=abcd efgh ijkl mnop");
        Files.write(Paths.get("datos_prueba4/correo_falso.txt"), config, StandardCharsets.UTF_8);
        ServicioCorreo conFalso = new ServicioCorreo(new ArchivoConfiguracion("datos_prueba4/correo_falso.txt"));
        System.out.println("Con configuracion falsa, configurado: " + conFalso.estaConfigurado());
        String errorEnvio = conFalso.enviar("cliente@gmail.com", "Factura", "Texto");
        System.out.println("Enviar a un servidor que no responde: " + (errorEnvio.isEmpty() ? "OK" : "ERROR CONTROLADO (el programa sigue funcionando)"));
        System.out.println("Direccion invalida: " + res(conFalso.enviar("sinarroba", "Factura", "Texto")));
    }
}
