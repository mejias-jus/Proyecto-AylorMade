package datos;

import java.util.ArrayList;
import modelo.Sucursal;

/**
 * Lee y escribe sucursales.txt. Una linea por sucursal:
 * nombre|direccion|latitud|longitud|numeroCajas
 */
public class ArchivoSucursales extends ArchivoTexto {

    public ArchivoSucursales(String ruta) {
        super(ruta);
    }

    public ArrayList<Sucursal> cargar() {
        ArrayList<Sucursal> sucursales = new ArrayList<>();
        ArrayList<String> lineas = leerLineas();
        for (int i = 0; i < lineas.size(); i++) {
            String[] p = separar(lineas.get(i));
            if (p.length != 5) {
                continue; // linea danada: se ignora
            }
            try {
                Sucursal s = new Sucursal(p[0], p[1], Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                        Integer.parseInt(p[4]));
                if (!s.getNombre().isEmpty() && !s.getDireccion().isEmpty()) {
                    sucursales.add(s);
                }
            } catch (Exception e) {
                // linea danada: se ignora
            }
        }
        return sucursales;
    }

    public boolean guardarTodos(ArrayList<Sucursal> sucursales) {
        ArrayList<String> lineas = new ArrayList<>();
        for (int i = 0; i < sucursales.size(); i++) {
            lineas.add(convertirSucursal(sucursales.get(i)));
        }
        return escribirLineas(lineas);
    }

    public boolean agregar(Sucursal sucursal) {
        return agregarLinea(convertirSucursal(sucursal));
    }

    private String convertirSucursal(Sucursal s) {
        return limpiar(s.getNombre()) + SEPARADOR
                + limpiar(s.getDireccion()) + SEPARADOR
                + s.getLatitud() + SEPARADOR
                + s.getLongitud() + SEPARADOR
                + s.getNumeroCajas();
    }
}
