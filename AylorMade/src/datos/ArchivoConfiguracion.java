package datos;

import java.util.ArrayList;

/**
 * Lee un archivo de configuracion con lineas "clave=valor" (por ejemplo config/correo.txt).
 * Las lineas que empiezan con # son comentarios y se ignoran.
 */
public class ArchivoConfiguracion extends ArchivoTexto {

    public ArchivoConfiguracion(String ruta) {
        super(ruta);
    }

    // Devuelve el valor de la clave, o "" si no existe.
    public String obtener(String clave) {
        ArrayList<String> lineas = leerLineas();
        for (int i = 0; i < lineas.size(); i++) {
            int posicion = lineas.get(i).indexOf('=');
            if (posicion > 0 && lineas.get(i).substring(0, posicion).trim().equalsIgnoreCase(clave)) {
                return lineas.get(i).substring(posicion + 1).trim();
            }
        }
        return "";
    }
}
