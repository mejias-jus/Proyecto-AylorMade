package presentacion;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Carga las imagenes de la carpeta src/imagenes (logo e iconos).
 * Si una imagen no se encuentra, devuelve null y la pantalla sigue funcionando sin ella.
 */
public class Imagenes {

    public static Image imagen(String nombre) {
        URL url = Imagenes.class.getResource("/imagenes/" + nombre);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    public static ImageIcon icono(String nombre, int tamano) {
        Image img = imagen(nombre);
        if (img == null) {
            return null;
        }
        return new ImageIcon(img.getScaledInstance(tamano, tamano, Image.SCALE_SMOOTH));
    }
}
