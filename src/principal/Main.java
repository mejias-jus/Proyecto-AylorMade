package principal;

import javax.swing.SwingUtilities;
import logica.SistemaSupermercado;
import presentacion.Estilo;
import presentacion.FrmLogin;

/**
 * Punto de entrada de AylorMade. Es el archivo que se ejecuta (Run Project).
 * Implementa Runnable para que la pantalla se cree en el hilo de Swing, como se recomienda.
 */
public class Main implements Runnable {

    @Override
    public void run() {
        Estilo.aplicarTema();
        SistemaSupermercado sistema = new SistemaSupermercado();
        new FrmLogin(sistema).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Main());
    }
}
