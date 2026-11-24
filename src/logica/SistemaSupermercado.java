package logica;

import datos.ArchivoConfiguracion;
import datos.ArchivoEntradas;
import datos.ArchivoProductos;
import datos.ArchivoSucursales;
import datos.ArchivoUsuarios;
import datos.ArchivoVentas;

/**
 * Punto de entrada de la capa de logica: crea los archivos y los servicios una sola vez.
 * Las pantallas reciben este objeto y piden lo que necesitan:
 * sistema.getUsuarios(), sistema.getProductos(), sistema.getVentas(), sistema.getReportes().
 */
public class SistemaSupermercado {

    private ServicioCorreo correo;
    private ServicioUsuarios usuarios;
    private ServicioProductos productos;
    private ServicioVentas ventas;
    private ServicioReportes reportes;
    private ServicioSucursales sucursales;

    public SistemaSupermercado() {
        this("datos");
    }

    // La carpeta indica donde viven los .txt (la prueba usa una carpeta aparte).
    public SistemaSupermercado(String carpeta) {
        this(carpeta, new ServicioCorreo(new ArchivoConfiguracion("config/correo.txt")));
    }

    // Permite indicar otro servicio de correo (las pruebas usan uno falso que no envia nada).
    public SistemaSupermercado(String carpeta, ServicioCorreo correo) {
        ArchivoUsuarios archivoUsuarios = new ArchivoUsuarios(carpeta + "/usuarios.txt");
        ArchivoProductos archivoProductos = new ArchivoProductos(carpeta + "/productos.txt");
        ArchivoVentas archivoVentas = new ArchivoVentas(carpeta + "/ventas.txt");
        ArchivoEntradas archivoEntradas = new ArchivoEntradas(carpeta + "/entradas.txt");
        ArchivoSucursales archivoSucursales = new ArchivoSucursales(carpeta + "/sucursales.txt");

        this.correo = correo;
        this.usuarios = new ServicioUsuarios(archivoUsuarios, correo);
        this.productos = new ServicioProductos(archivoProductos, archivoEntradas);
        this.ventas = new ServicioVentas(archivoVentas, productos, correo);
        this.reportes = new ServicioReportes(archivoVentas, archivoEntradas);
        this.sucursales = new ServicioSucursales(archivoSucursales);
    }

    public ServicioCorreo getCorreo() {
        return correo;
    }

    public ServicioUsuarios getUsuarios() {
        return usuarios;
    }

    public ServicioProductos getProductos() {
        return productos;
    }

    public ServicioVentas getVentas() {
        return ventas;
    }

    public ServicioSucursales getSucursales() {
        return sucursales;
    }

    public ServicioReportes getReportes() {
        return reportes;
    }
}
