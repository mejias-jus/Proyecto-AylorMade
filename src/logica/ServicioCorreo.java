package logica;

import datos.ArchivoConfiguracion;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.File;
import java.nio.file.Files;
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
        return problemaConfiguracion().isEmpty();
    }

    // Explica que falta en la configuracion ("" si todo esta bien). Sirve para que el mensaje diga la causa exacta.
    public String problemaConfiguracion() {
        if (!configuracion.existe()) {
            return "No se encontró el archivo " + configuracion.getRutaCompleta()
                    + " (revise que se llame correo.txt y no correo.txt.txt).";
        }
        String[] campos = {"servidor", "puerto", "usuario", "clave"};
        String faltan = "";
        for (int i = 0; i < campos.length; i++) {
            if (configuracion.obtener(campos[i]).isEmpty()) {
                if (!faltan.isEmpty()) {
                    faltan = faltan + ", ";
                }
                faltan = faltan + campos[i];
            }
        }
        if (faltan.isEmpty()) {
            return "";
        }
        return "En " + configuracion.getRutaCompleta() + " falta completar: " + faltan + ".";
    }

    public String enviar(String destinatario, String asunto, String mensaje) {
        return enviarMensaje(destinatario, asunto, mensaje, null);
    }

    // Igual que enviar, pero con un archivo adjunto (por ejemplo la factura en PDF).
    public String enviarConAdjunto(String destinatario, String asunto, String mensaje, File adjunto) {
        return enviarMensaje(destinatario, asunto, mensaje, adjunto);
    }

    private String enviarMensaje(String destinatario, String asunto, String mensaje, File adjunto) {
        if (!estaConfigurado()) {
            return "El envío de correos no está configurado. " + problemaConfiguracion();
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
            MimeMessage correo = construirMensaje(sesion, usuario, destinatario, asunto, mensaje, adjunto);
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

    /**
     * Arma el correo: el texto y, si se indica, un archivo adjunto (un PDF). No lo envia.
     * Un correo con adjunto es "multipart": una parte con el texto y otra con el archivo.
     */
    public MimeMessage construirMensaje(Session sesion, String remitente, String destinatario, String asunto,
            String texto, File adjunto) throws Exception {
        MimeMessage correo = new MimeMessage(sesion);
        correo.setFrom(new InternetAddress(remitente, "AylorMade"));
        correo.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        correo.setSubject(asunto, "UTF-8");
        if (adjunto == null) {
            correo.setText(texto, "UTF-8");
            return correo;
        }
        MimeBodyPart parteTexto = new MimeBodyPart();
        parteTexto.setText(texto, "UTF-8");
        MimeBodyPart parteArchivo = new MimeBodyPart();
        parteArchivo.setDataHandler(new jakarta.activation.DataHandler(
                new ByteArrayDataSource(Files.readAllBytes(adjunto.toPath()), "application/pdf")));
        parteArchivo.setFileName(adjunto.getName());
        MimeMultipart partes = new MimeMultipart();
        partes.addBodyPart(parteTexto);
        partes.addBodyPart(parteArchivo);
        correo.setContent(partes);
        return correo;
    }
}
