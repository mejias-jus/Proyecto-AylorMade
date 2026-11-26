package principal;

import datos.ArchivoConfiguracion;
import java.io.File;
import logica.ServicioCorreo;

/**
 * Correo FALSO para las pruebas: no envia nada, solo guarda lo que se habria enviado.
 * POLIMORFISMO: es un ServicioCorreo y sobrescribe estaConfigurado() y enviar().
 */
public class CorreoDePrueba extends ServicioCorreo {

    private String ultimoDestinatario = "";
    private String ultimoMensaje = "";
    private String ultimoAdjunto = "";

    public CorreoDePrueba() {
        super(new ArchivoConfiguracion("datos_prueba4/no_existe.txt"));
    }

    public String getUltimoDestinatario() {
        return ultimoDestinatario;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public String getUltimoAdjunto() {
        return ultimoAdjunto;
    }

    @Override
    public String enviarConAdjunto(String destinatario, String asunto, String mensaje, File adjunto) {
        ultimoAdjunto = adjunto.getName() + " (" + adjunto.length() + " bytes)";
        return enviar(destinatario, asunto, mensaje);
    }

    @Override
    public boolean estaConfigurado() {
        return true;
    }

    @Override
    public String enviar(String destinatario, String asunto, String mensaje) {
        ultimoDestinatario = destinatario;
        ultimoMensaje = mensaje;
        return "";
    }
}
