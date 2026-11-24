package datos;

import java.util.ArrayList;
import modelo.ItemVenta;
import modelo.RegistroVenta;
import modelo.Venta;

/**
 * Lee y escribe ventas.txt. Una linea por venta:
 * numero|fecha|usuarioCajero|metodoPago|total|vuelto|codigo:cantidad:precioUnitario,codigo:cantidad:precioUnitario
 * ESTABILIDAD: cada venta se agrega al final del archivo en el momento de cobrar,
 * sin reescribir lo anterior, asi un cierre inesperado no hace perder las ventas ya hechas.
 */
public class ArchivoVentas extends ArchivoTexto {

    public ArchivoVentas(String ruta) {
        super(ruta);
    }

    // Solo guarda ventas ya cobradas (cerradas).
    public boolean guardarVenta(Venta venta) {
        if (venta == null || !venta.estaCerrada()) {
            return false;
        }
        String items = "";
        for (int i = 0; i < venta.getItems().size(); i++) {
            ItemVenta item = venta.getItems().get(i);
            if (i > 0) {
                items = items + ",";
            }
            items = items + limpiar(item.getProducto().getCodigo()) + ":" + item.getCantidad()
                    + ":" + item.getProducto().calcularPrecioFinal();
        }
        String linea = venta.getNumero() + SEPARADOR
                + limpiar(venta.getFecha()) + SEPARADOR
                + limpiar(venta.getCajero().getNombreUsuario()) + SEPARADOR
                + limpiar(venta.getMetodoPago().getNombre()) + SEPARADOR
                + venta.calcularTotal() + SEPARADOR
                + venta.getVuelto() + SEPARADOR
                + items;
        return agregarLinea(linea);
    }

    public ArrayList<RegistroVenta> cargar() {
        ArrayList<RegistroVenta> ventas = new ArrayList<>();
        ArrayList<String> lineas = leerLineas();
        for (int i = 0; i < lineas.size(); i++) {
            RegistroVenta r = convertirLinea(lineas.get(i));
            if (r != null) {
                ventas.add(r);
            }
        }
        return ventas;
    }

    // Numero que le toca a la proxima venta (el mayor guardado + 1).
    public int obtenerSiguienteNumero() {
        ArrayList<RegistroVenta> ventas = cargar();
        int mayor = 0;
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getNumero() > mayor) {
                mayor = ventas.get(i).getNumero();
            }
        }
        return mayor + 1;
    }

    private RegistroVenta convertirLinea(String linea) {
        String[] p = separar(linea);
        if (p.length != 7) {
            return null;
        }
        try {
            return new RegistroVenta(Integer.parseInt(p[0]), p[1], p[2], p[3],
                    Double.parseDouble(p[4]), Double.parseDouble(p[5]), p[6]);
        } catch (Exception e) {
            return null;
        }
    }
}
