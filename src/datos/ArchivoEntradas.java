package datos;

import java.util.ArrayList;
import modelo.EntradaInventario;

/**
 * Lee y escribe entradas.txt (ingresos de mercaderia). Una linea por entrada:
 * fecha|codigoProducto|nombreProducto|cantidad|usuario|proveedor
 */
public class ArchivoEntradas extends ArchivoTexto {

    public ArchivoEntradas(String ruta) {
        super(ruta);
    }

    public boolean guardarEntrada(EntradaInventario e) {
        String linea = limpiar(e.getFecha()) + SEPARADOR
                + limpiar(e.getCodigoProducto()) + SEPARADOR
                + limpiar(e.getNombreProducto()) + SEPARADOR
                + e.getCantidad() + SEPARADOR
                + limpiar(e.getUsuario()) + SEPARADOR
                + limpiar(e.getProveedor());
        return agregarLinea(linea);
    }

    public ArrayList<EntradaInventario> cargar() {
        ArrayList<EntradaInventario> entradas = new ArrayList<>();
        ArrayList<String> lineas = leerLineas();
        for (int i = 0; i < lineas.size(); i++) {
            String[] p = separar(lineas.get(i));
            if (p.length != 6) {
                continue; // linea danada: se ignora
            }
            try {
                entradas.add(new EntradaInventario(p[0], p[1], p[2], Integer.parseInt(p[3]), p[4], p[5]));
            } catch (Exception e) {
                // linea danada: se ignora
            }
        }
        return entradas;
    }
}
