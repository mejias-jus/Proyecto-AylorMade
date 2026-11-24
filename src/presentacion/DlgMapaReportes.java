package presentacion;

import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import logica.SistemaSupermercado;
import modelo.Usuario;

/**
 * Pantalla "Mapa y reportes": una pestana con los reportes (solo para quien tiene permiso)
 * y otra con el mapa de sucursales.
 */
public class DlgMapaReportes extends JDialog {

    public DlgMapaReportes(Frame padre, SistemaSupermercado sistema, Usuario usuario) {
        super(padre, "Mapa y reportes", true);
        setIconImage(Imagenes.imagen("logo.png"));
        setSize(1280, 780);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilo.NEGRITA);
        if (usuario.puedeVerReportes()) {
            pestanas.addTab("Reportes", new PnlReportes(sistema, usuario));
        }
        pestanas.addTab("Mapa de sucursales", new PnlMapa(sistema, usuario));
        add(pestanas, BorderLayout.CENTER);
    }
}
