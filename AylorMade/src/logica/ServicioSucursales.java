package logica;

import datos.ArchivoSucursales;
import datos.ArchivoTexto;
import java.util.ArrayList;
import modelo.Sucursal;
import modelo.Usuario;

/**
 * Reglas de negocio de las sucursales (las que se muestran en el mapa).
 * Solo un administrador puede agregar o eliminar sucursales; todos pueden verlas.
 * Los metodos que pueden fallar devuelven "" si salio bien, o el mensaje de error.
 */
public class ServicioSucursales {

    private ArchivoSucursales archivo;
    private ArrayList<Sucursal> sucursales;

    public ServicioSucursales(ArchivoSucursales archivo) {
        this.archivo = archivo;
        this.sucursales = archivo.cargar();
    }

    public ArrayList<Sucursal> listar() {
        return sucursales;
    }

    public Sucursal buscar(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (int i = 0; i < sucursales.size(); i++) {
            if (sucursales.get(i).getNombre().equalsIgnoreCase(nombre.trim())) {
                return sucursales.get(i);
            }
        }
        return null;
    }

    public String agregar(Usuario quien, String nombre, String direccion, double latitud, double longitud, int cajas) {
        if (quien == null || !quien.puedeGestionarUsuarios()) {
            return "Solo un administrador puede agregar sucursales.";
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            return "La dirección es obligatoria.";
        }
        if (nombre.contains(ArchivoTexto.SEPARADOR) || direccion.contains(ArchivoTexto.SEPARADOR)) {
            return "Los datos no pueden contener el símbolo " + ArchivoTexto.SEPARADOR;
        }
        if (buscar(nombre) != null) {
            return "Ya existe una sucursal con ese nombre.";
        }
        if (latitud < -90 || latitud > 90) {
            return "La latitud debe estar entre -90 y 90.";
        }
        if (longitud < -180 || longitud > 180) {
            return "La longitud debe estar entre -180 y 180.";
        }
        if (cajas <= 0) {
            return "El número de cajas debe ser mayor que 0.";
        }
        Sucursal nueva = new Sucursal(nombre, direccion, latitud, longitud, cajas);
        sucursales.add(nueva);
        if (!archivo.agregar(nueva)) {
            sucursales.remove(nueva);
            return "No se pudo guardar la sucursal en el archivo.";
        }
        return "";
    }

    public String eliminar(Usuario quien, String nombre) {
        if (quien == null || !quien.puedeGestionarUsuarios()) {
            return "Solo un administrador puede eliminar sucursales.";
        }
        Sucursal s = buscar(nombre);
        if (s == null) {
            return "La sucursal no existe.";
        }
        sucursales.remove(s);
        if (!archivo.guardarTodos(sucursales)) {
            sucursales.add(s);
            return "No se pudo guardar el cambio.";
        }
        return "";
    }
}
