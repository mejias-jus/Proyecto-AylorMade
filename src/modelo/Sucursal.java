package modelo;

/**
 * Sucursal de un supermercado. Se usara en la pantalla del mapa (latitud y longitud).
 * ENCAPSULAMIENTO: las coordenadas solo se aceptan si estan en un rango valido.
 */
public class Sucursal {

    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private int numeroCajas;

    public Sucursal(String nombre, String direccion, double latitud, double longitud, int numeroCajas) {
        this.nombre = "";
        this.direccion = "";
        this.latitud = 0;
        this.longitud = 0;
        this.numeroCajas = 1;
        setNombre(nombre);
        setDireccion(direccion);
        setLatitud(latitud);
        setLongitud(longitud);
        setNumeroCajas(numeroCajas);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        }
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            this.direccion = direccion.trim();
        }
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        if (latitud >= -90 && latitud <= 90) {
            this.latitud = latitud;
        }
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        if (longitud >= -180 && longitud <= 180) {
            this.longitud = longitud;
        }
    }

    public int getNumeroCajas() {
        return numeroCajas;
    }

    public void setNumeroCajas(int numeroCajas) {
        if (numeroCajas > 0) {
            this.numeroCajas = numeroCajas;
        }
    }

    // Permite que el mismo sistema atienda negocios pequenos y grandes.
    public String getTipo() {
        if (numeroCajas >= 5) {
            return "Supermercado grande";
        }
        return "Minisuper";
    }
}
