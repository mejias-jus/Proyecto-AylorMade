package presentacion;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Detecta los clics sobre el mapa y le avisa al panel cuales son las coordenadas del punto.
 * HERENCIA: extiende MouseAdapter y sobrescribe solo mouseClicked.
 */
public class EscuchaMapa extends MouseAdapter {

    private PnlMapa panel;
    private JXMapViewer mapa;

    public EscuchaMapa(PnlMapa panel, JXMapViewer mapa) {
        this.panel = panel;
        this.mapa = mapa;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
            GeoPosition posicion = mapa.convertPointToGeoPosition(e.getPoint());
            panel.alElegirPunto(posicion.getLatitude(), posicion.getLongitude());
        }
    }
}
