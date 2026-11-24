package presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 * Dibuja cada marcador del mapa: un circulo de color con el nombre de la sucursal al lado.
 * POLIMORFISMO: implementa WaypointRenderer y define como se pinta cada marcador.
 */
public class RenderizadorSucursal implements WaypointRenderer<WaypointSucursal> {

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer mapa, WaypointSucursal marcador) {
        Point2D punto = mapa.getTileFactory().geoToPixel(marcador.getPosition(), mapa.getZoom());
        int x = (int) punto.getX();
        int y = (int) punto.getY();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = Estilo.NARANJA;
        if (marcador.esNueva()) {
            color = Estilo.VERDE;
        }
        // punta del marcador y circulo
        g2.setColor(color);
        g2.fillPolygon(new int[]{x - 7, x + 7, x}, new int[]{y - 14, y - 14, y}, 3);
        g2.fillOval(x - 12, y - 36, 24, 24);
        g2.setColor(Color.WHITE);
        g2.fillOval(x - 5, y - 29, 10, 10);

        // etiqueta con el nombre
        g2.setFont(Estilo.NEGRITA.deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();
        String texto = marcador.getNombre();
        int ancho = fm.stringWidth(texto) + 12;
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(x + 16, y - 33, ancho, 20, 8, 8);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x + 16, y - 33, ancho, 20, 8, 8);
        g2.setColor(Estilo.AZUL);
        g2.drawString(texto, x + 22, y - 18);
        g2.dispose();
    }
}
