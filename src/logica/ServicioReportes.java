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
