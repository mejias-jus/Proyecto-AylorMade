package logica;

import datos.ArchivoConfiguracion;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Envio de correos con la libreria Jakarta Mail (por Gmail).
 * El usuario y la contrasena del correo NO estan en el codigo: se leen de config/correo.txt.
 * Los metodos devuelven "" si salio bien, o el mensaje de error.
 */
public class ServicioCorreo {

    private ArchivoConfiguracion configuracion;

    public ServicioCorreo(ArchivoConfiguracion configuracion) {
        this.configuracion = configuracion;
    }

    // Verdadero si el archivo de configuracion tiene servidor, puerto, usuario y clave.
    public boolean estaConfigurado() {
        return !configuracion.obtener("servidor").isEmpty()
                && !configuracion.obtener("puerto").isEmpty()
                && !configuracion.obtener("usuario").isEmpty()
                && !configuracion.obtener("clave").isEmpty();
    }

    public String enviar(String destinatario, String asunto, String mensaje) {
        if (!estaConfigurado()) {
            return "El envío de correos no está configurado (falta completar config/correo.txt).";
        }
        if (destinatario == null || !destinatario.contains("@")) {
            return "El correo del destinatario no es válido.";
        }
        String servidor = configuracion.obtener("servidor");
        String puerto = configuracion.obtener("puerto");
        String usuario = configuracion.obtener("usuario");
        // Gmail muestra la contrasena de aplicacion con espacios; aqui se quitan.
        String clave = configuracion.obtener("clave").replace(" ", "");

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", servidor);
        propiedades.put("mail.smtp.port", puerto);
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        propiedades.put("mail.smtp.connectiontimeout", "10000");
        propiedades.put("mail.smtp.timeout", "15000");
        propiedades.put("mail.smtp.writetimeout", "15000");

        Transport transporte = null;
        try {
            Session sesion = Session.getInstance(propiedades);
            MimeMessage correo = new MimeMessage(sesion);
            correo.setFrom(new InternetAddress(usuario, "AylorMade"));
            correo.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            correo.setSubject(asunto, "UTF-8");
            correo.setText(mensaje, "UTF-8");

            transporte = sesion.getTransport("smtp");
            transporte.connect(servidor, usuario, clave);
            transporte.sendMessage(correo, correo.getAllRecipients());
            return "";
        } catch (Exception e) {
            return "No se pudo enviar el correo. Revise la conexión a internet y la configuración. (" + e.getMessage() + ")";
        } finally {
            if (transporte != null) {
                try {
                    transporte.close();
                } catch (Exception e) {
                    // no importa si falla al cerrar
                }
            }
        }
    }
}
