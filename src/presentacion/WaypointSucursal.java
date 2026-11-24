package presentacion;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Un marcador del mapa que ademas del lugar lleva el nombre de la sucursal.
 * HERENCIA: extiende DefaultWaypoint, la clase de marcador de la libreria JXMapViewer2.
 */
public class WaypointSucursal extends DefaultWaypoint {

    private String nombre;
    private boolean nueva;

    public WaypointSucursal(String nombre, double latitud, double longitud, boolean nueva) {
        super(new GeoPosition(latitud, longitud));
        this.nombre = nombre;
        this.nueva = nueva;
    }

    public String getNombre() {
        return nombre;
    }

    // Verdadero para el punto que el administrador acaba de elegir y todavia no guardo.
    public boolean esNueva() {
        return nueva;
    }
}
