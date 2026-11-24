package principal;

import datos.ArchivoConfiguracion;
import java.io.File;
import javax.swing.JOptionPane;
import logica.ServicioCorreo;

/**
 * Diagnostico del correo: muestra que archivo de configuracion se esta leyendo, que datos encontro
 * (sin mostrar la clave) y envia un correo de prueba REAL. Ejecutelo con "Run File".
 */
public class PruebaEnvioReal {

    public static void main(String[] args) {
        String ruta = "config/correo.txt";
        File archivo = new File(ruta);
        System.out.println("=== DIAGNOSTICO DEL CORREO ===");
        System.out.println("Carpeta desde la que se ejecuta el programa: " + System.getProperty("user.dir"));
        System.out.println("El programa busca el archivo en: " + archivo.getAbsolutePath());
        System.out.println("¿El archivo existe?: " + (archivo.exists() ? "SI" : "NO  <-- aqui esta el problema: cree config/correo.txt en esa carpeta"));

        ArchivoConfiguracion config = new ArchivoConfiguracion(ruta);
        System.out.println("servidor = '" + config.obtener("servidor") + "'  (debe ser smtp.gmail.com)");
        System.out.println("puerto   = '" + config.obtener("puerto") + "'  (debe ser 587)");
        System.out.println("usuario  = '" + config.obtener("usuario") + "'  (la cuenta de Gmail que ENVIA)");
        int largoClave = config.obtener("clave").replace(" ", "").length();
        System.out.println("clave    = " + (largoClave == 0 ? "VACIA" : largoClave + " caracteres") + "  (la contraseña de aplicación tiene 16)");

        ServicioCorreo correo = new ServicioCorreo(config);
        System.out.println("¿El sistema lo considera configurado?: " + correo.estaConfigurado());
        if (!correo.estaConfigurado()) {
            System.out.println("Corrija lo que dice arriba y vuelva a ejecutar.");
            return;
        }

        String destino;
        if (args.length > 0) {
            destino = args[0];
        } else {
            destino = JOptionPane.showInputDialog("Correo donde quiere recibir la prueba:");
        }
        if (destino == null || destino.trim().isEmpty()) {
            System.out.println("No se escribió un correo de destino.");
            return;
        }
        System.out.println("Enviando a " + destino + " ...");
        String error = correo.enviar(destino.trim(), "AylorMade - Prueba de correo", "Si usted lee esto, el envío de correos funciona.");
        if (error.isEmpty()) {
            System.out.println("RESULTADO: ENVIADO. Revise la bandeja de entrada y la carpeta de spam.");
        } else {
            System.out.println("RESULTADO: FALLÓ -> " + error);
        }
    }
}
