package logica;

import datos.ArchivoEntradas;
import datos.ArchivoVentas;
import java.time.LocalDate;
import java.util.ArrayList;
import modelo.EntradaInventario;
import modelo.RegistroVenta;

/**
 * Calculos para el dashboard y los reportes. Siempre leen los archivos al momento de consultar,
 * asi los numeros reflejan lo que de verdad quedo guardado.
 * Las fechas se pasan como texto "yyyy-MM-dd" (por ejemplo "2026-12-01").
 */
public class ServicioReportes {

    private ArchivoVentas archivoVentas;
    private ArchivoEntradas archivoEntradas;

    public ServicioReportes(ArchivoVentas archivoVentas, ArchivoEntradas archivoEntradas) {
        this.archivoVentas = archivoVentas;
        this.archivoEntradas = archivoEntradas;
    }

    public String hoy() {
        return LocalDate.now().toString();
    }

    public ArrayList<RegistroVenta> ventasDelDia(String fecha) {
        ArrayList<RegistroVenta> resultado = new ArrayList<>();
        ArrayList<RegistroVenta> todas = archivoVentas.cargar();
        for (int i = 0; i < todas.size(); i++) {
            if (todas.get(i).getFecha().startsWith(fecha)) {
                resultado.add(todas.get(i));
            }
        }
        return resultado;
    }

    public double totalRecaudado(String fecha) {
        double total = 0;
        ArrayList<RegistroVenta> ventas = ventasDelDia(fecha);
        for (int i = 0; i < ventas.size(); i++) {
            total = total + ventas.get(i).getTotal();
        }
        return total;
    }

    public double totalRecaudadoHistorico() {
        double total = 0;
        ArrayList<RegistroVenta> todas = archivoVentas.cargar();
        for (int i = 0; i < todas.size(); i++) {
            total = total + todas.get(i).getTotal();
        }
        return total;
    }

    public int cantidadVentas(String fecha) {
        return ventasDelDia(fecha).size();
    }

    public double totalPorMetodo(String fecha, String metodoPago) {
        double total = 0;
        ArrayList<RegistroVenta> ventas = ventasDelDia(fecha);
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getMetodoPago().equalsIgnoreCase(metodoPago)) {
                total = total + ventas.get(i).getTotal();
            }
        }
        return total;
    }

    public double totalPorCajero(String fecha, String usuarioCajero) {
        double total = 0;
        ArrayList<RegistroVenta> ventas = ventasDelDia(fecha);
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getUsuarioCajero().equalsIgnoreCase(usuarioCajero)) {
                total = total + ventas.get(i).getTotal();
            }
        }
        return total;
    }

    // Suma las unidades de todos los productos vendidos ese dia (lee el detalle codigo:cantidad:precio).
    public int unidadesVendidas(String fecha) {
        int unidades = 0;
        ArrayList<RegistroVenta> ventas = ventasDelDia(fecha);
        for (int i = 0; i < ventas.size(); i++) {
            String[] lineas = ventas.get(i).getItems().split(",");
            for (int j = 0; j < lineas.length; j++) {
                String[] partes = lineas[j].split(":");
                if (partes.length == 3) {
                    try {
                        unidades = unidades + Integer.parseInt(partes[1]);
                    } catch (NumberFormatException e) {
                        // detalle danado: se ignora
                    }
                }
            }
        }
        return unidades;
    }

    /**
     * Resumen de lo vendido ese dia por producto, del que mas se vendio al que menos.
     * Cada fila es {codigo, nombre, unidades, total vendido}.
     */
    public ArrayList<String[]> productosVendidos(String fecha, ServicioProductos productos) {
        ArrayList<String> codigos = new ArrayList<>();
        ArrayList<Integer> unidades = new ArrayList<>();
        ArrayList<Double> totales = new ArrayList<>();
        ArrayList<RegistroVenta> ventas = ventasDelDia(fecha);
        for (int i = 0; i < ventas.size(); i++) {
            String[] lineas = ventas.get(i).getItems().split(",");
            for (int j = 0; j < lineas.length; j++) {
                String[] partes = lineas[j].split(":");
                if (partes.length != 3) {
                    continue;
                }
                try {
                    int cantidad = Integer.parseInt(partes[1]);
                    double subtotal = cantidad * Double.parseDouble(partes[2]);
                    int posicion = codigos.indexOf(partes[0]);
                    if (posicion < 0) {
                        codigos.add(partes[0]);
                        unidades.add(cantidad);
                        totales.add(subtotal);
                    } else {
                        unidades.set(posicion, unidades.get(posicion) + cantidad);
                        totales.set(posicion, totales.get(posicion) + subtotal);
                    }
                } catch (NumberFormatException e) {
                    // detalle danado: se ignora
                }
            }
        }
        // Se ordena de mayor a menor cantidad vendida (metodo de seleccion).
        for (int i = 0; i < codigos.size() - 1; i++) {
            int mayor = i;
            for (int j = i + 1; j < codigos.size(); j++) {
                if (unidades.get(j) > unidades.get(mayor)) {
                    mayor = j;
                }
            }
            if (mayor != i) {
                String c = codigos.get(i);
                codigos.set(i, codigos.get(mayor));
                codigos.set(mayor, c);
                int u = unidades.get(i);
                unidades.set(i, unidades.get(mayor));
                unidades.set(mayor, u);
                double t = totales.get(i);
                totales.set(i, totales.get(mayor));
                totales.set(mayor, t);
            }
        }
        ArrayList<String[]> resultado = new ArrayList<>();
        for (int i = 0; i < codigos.size(); i++) {
            String nombre = codigos.get(i);
            if (productos != null && productos.buscarPorCodigo(codigos.get(i)) != null) {
                nombre = productos.buscarPorCodigo(codigos.get(i)).getNombre();
            }
            resultado.add(new String[]{codigos.get(i), nombre, String.valueOf(unidades.get(i)), String.valueOf(totales.get(i))});
        }
        return resultado;
    }

    // Cuantos productos entraron al inventario ese dia.
    public int unidadesIngresadas(String fecha) {
        int unidades = 0;
        ArrayList<EntradaInventario> entradas = archivoEntradas.cargar();
        for (int i = 0; i < entradas.size(); i++) {
            if (entradas.get(i).getFecha().startsWith(fecha)) {
                unidades = unidades + entradas.get(i).getCantidad();
            }
        }
        return unidades;
    }

    public ArrayList<EntradaInventario> entradasDelDia(String fecha) {
        ArrayList<EntradaInventario> resultado = new ArrayList<>();
        ArrayList<EntradaInventario> todas = archivoEntradas.cargar();
        for (int i = 0; i < todas.size(); i++) {
            if (todas.get(i).getFecha().startsWith(fecha)) {
                resultado.add(todas.get(i));
            }
        }
        return resultado;
    }
}
