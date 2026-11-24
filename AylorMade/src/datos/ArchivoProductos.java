package datos;

import java.time.LocalDate;
import java.util.ArrayList;
import modelo.Producto;
import modelo.ProductoCanastaBasica;
import modelo.ProductoPerecedero;

/**
 * Lee y escribe productos.txt. Una linea por producto:
 * tipo|codigo|nombre|precioBase|stock|stockMinimo|fechaVencimiento
 * La fecha de vencimiento va vacia si el producto no es perecedero.
 */
public class ArchivoProductos extends ArchivoTexto {

    public ArchivoProductos(String ruta) {
        super(ruta);
    }

    public ArrayList<Producto> cargar() {
        ArrayList<Producto> productos = new ArrayList<>();
        ArrayList<String> lineas = leerLineas();
        for (int i = 0; i < lineas.size(); i++) {
            Producto p = convertirLinea(lineas.get(i));
            if (p != null) {
                productos.add(p);
            }
        }
        return productos;
    }

    public boolean guardarTodos(ArrayList<Producto> productos) {
        ArrayList<String> lineas = new ArrayList<>();
        for (int i = 0; i < productos.size(); i++) {
            lineas.add(convertirProducto(productos.get(i)));
        }
        return escribirLineas(lineas);
    }

    public boolean agregar(Producto producto) {
        return agregarLinea(convertirProducto(producto));
    }

    private String convertirProducto(Producto p) {
        return limpiar(p.getTipo()) + SEPARADOR
                + limpiar(p.getCodigo()) + SEPARADOR
                + limpiar(p.getNombre()) + SEPARADOR
                + p.getPrecioBase() + SEPARADOR
                + p.getStock() + SEPARADOR
                + p.getStockMinimo() + SEPARADOR
                + p.getFechaVencimientoTexto();
    }

    // Acepta una fecha normal (2026-12-31) o una relativa: +3 es dentro de 3 dias, -1 es ayer.
    // Las fechas relativas sirven para archivos de ejemplo; al guardar siempre queda la fecha real.
    private LocalDate leerFecha(String texto) {
        String t = texto.trim();
        if (t.startsWith("+") || t.startsWith("-")) {
            return LocalDate.now().plusDays(Integer.parseInt(t.substring(1)) * (t.startsWith("-") ? -1 : 1));
        }
        return LocalDate.parse(t);
    }

    // Devuelve null si la linea no es valida.
    private Producto convertirLinea(String linea) {
        String[] p = separar(linea);
        if (p.length != 7) {
            return null;
        }
        Producto producto = null;
        try {
            double precio = Double.parseDouble(p[3]);
            int stock = Integer.parseInt(p[4]);
            int stockMinimo = Integer.parseInt(p[5]);
            if (p[0].equals("General")) {
                producto = new Producto(p[1], p[2], precio, stock, stockMinimo);
            } else if (p[0].equals("Perecedero")) {
                producto = new ProductoPerecedero(p[1], p[2], precio, stock, stockMinimo, leerFecha(p[6]));
            } else if (p[0].equals("Canasta basica")) {
                producto = new ProductoCanastaBasica(p[1], p[2], precio, stock, stockMinimo);
            }
        } catch (Exception e) {
            return null;
        }
        if (producto == null || producto.getCodigo().isEmpty() || producto.getNombre().isEmpty()) {
            return null;
        }
        return producto;
    }
}
