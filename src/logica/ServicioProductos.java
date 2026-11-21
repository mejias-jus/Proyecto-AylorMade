package logica;

import datos.ArchivoEntradas;
import datos.ArchivoProductos;
import datos.ArchivoTexto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import modelo.EntradaInventario;
import modelo.Producto;
import modelo.ProductoCanastaBasica;
import modelo.ProductoPerecedero;
import modelo.Usuario;

/**
 * Reglas de negocio del inventario: CRUD de productos, entradas de mercaderia y alertas.
 * Los metodos que pueden fallar devuelven un String: "" si todo salio bien, o el mensaje de error.
 * Solo los usuarios con permiso (puedeModificarPrecios) pueden cambiar el inventario.
 */
public class ServicioProductos {

    public static final String TIPO_GENERAL = "General";
    public static final String TIPO_PERECEDERO = "Perecedero";
    public static final String TIPO_CANASTA = "Canasta basica";

    private ArchivoProductos archivoProductos;
    private ArchivoEntradas archivoEntradas;
    private ArrayList<Producto> productos;
    private int importados;
    private int omitidos;

    public ServicioProductos(ArchivoProductos archivoProductos, ArchivoEntradas archivoEntradas) {
        this.archivoProductos = archivoProductos;
        this.archivoEntradas = archivoEntradas;
        this.productos = archivoProductos.cargar();
    }

    public ArrayList<Producto> listar() {
        return productos;
    }

    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equalsIgnoreCase(codigo.trim())) {
                return productos.get(i);
            }
        }
        return null;
    }

    // Para una caja de busqueda: productos cuyo nombre contiene el texto.
    public ArrayList<Producto> buscarPorNombre(String texto) {
        ArrayList<Producto> resultado = new ArrayList<>();
        if (texto == null) {
            return resultado;
        }
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getNombre().toLowerCase().contains(texto.trim().toLowerCase())) {
                resultado.add(productos.get(i));
            }
        }
        return resultado;
    }

    public String agregar(Usuario quien, String tipo, String codigo, String nombre, double precio,
            int stock, int stockMinimo, LocalDate vencimiento) {
        if (quien == null || !quien.puedeModificarPrecios()) {
            return "Solo un administrador puede agregar productos.";
        }
        if (codigo == null || codigo.trim().isEmpty()) {
            return "El código es obligatorio.";
        }
        if (buscarPorCodigo(codigo) != null) {
            return "Ya existe un producto con ese código.";
        }
        String error = validarDatos(nombre, precio, stockMinimo);
        if (!error.isEmpty()) {
            return error;
        }
        if (codigo.contains(ArchivoTexto.SEPARADOR)) {
            return "Los datos no pueden contener el símbolo " + ArchivoTexto.SEPARADOR;
        }
        if (stock < 0) {
            return "El stock no puede ser negativo.";
        }
        Producto nuevo;
        if (tipo.equals(TIPO_PERECEDERO)) {
            if (vencimiento == null) {
                return "El producto perecedero necesita fecha de vencimiento.";
            }
            if (vencimiento.isBefore(LocalDate.now())) {
                return "La fecha de vencimiento ya pasó.";
            }
            nuevo = new ProductoPerecedero(codigo, nombre, precio, stock, stockMinimo, vencimiento);
        } else if (tipo.equals(TIPO_CANASTA)) {
            nuevo = new ProductoCanastaBasica(codigo, nombre, precio, stock, stockMinimo);
        } else if (tipo.equals(TIPO_GENERAL)) {
            nuevo = new Producto(codigo, nombre, precio, stock, stockMinimo);
        } else {
            return "El tipo de producto no es válido.";
        }
        productos.add(nuevo);
        if (!archivoProductos.agregar(nuevo)) {
            productos.remove(nuevo);
            return "No se pudo guardar el producto en el archivo.";
        }
        if (stock > 0) {
            registrarEnHistorial(quien, nuevo, stock, "Stock inicial");
        }
        return "";
    }

    // El codigo y el stock no se editan aqui: el stock cambia solo con entradas y ventas.
    public String editar(Usuario quien, String codigo, String nombre, double precio,
            int stockMinimo, LocalDate vencimiento) {
        if (quien == null || !quien.puedeModificarPrecios()) {
            return "Solo un administrador puede editar productos.";
        }
        Producto p = buscarPorCodigo(codigo);
        if (p == null) {
            return "El producto no existe.";
        }
        String error = validarDatos(nombre, precio, stockMinimo);
        if (!error.isEmpty()) {
            return error;
        }
        p.setNombre(nombre);
        p.setPrecioBase(precio);
        p.setStockMinimo(stockMinimo);
        if (vencimiento != null) {
            p.setFechaVencimiento(vencimiento); // solo afecta a los perecederos
        }
        if (!guardarCambios()) {
            recargar();
            return "No se pudo guardar el cambio.";
        }
        return "";
    }

    public String eliminar(Usuario quien, String codigo) {
        if (quien == null || !quien.puedeModificarPrecios()) {
            return "Solo un administrador puede eliminar productos.";
        }
        Producto p = buscarPorCodigo(codigo);
        if (p == null) {
            return "El producto no existe.";
        }
        productos.remove(p);
        if (!guardarCambios()) {
            recargar();
            return "No se pudo guardar el cambio.";
        }
        return "";
    }

    // Registra mercaderia que llega: sube el stock y deja constancia en entradas.txt.
    public String registrarEntrada(Usuario quien, String codigo, int cantidad, String proveedor) {
        if (quien == null || !quien.puedeModificarPrecios()) {
            return "Solo un administrador puede registrar entradas.";
        }
        Producto p = buscarPorCodigo(codigo);
        if (p == null) {
            return "El producto no existe.";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que 0.";
        }
        if (proveedor != null && proveedor.contains(ArchivoTexto.SEPARADOR)) {
            return "Los datos no pueden contener el símbolo " + ArchivoTexto.SEPARADOR;
        }
        p.aumentarStock(cantidad);
        if (!guardarCambios()) {
            recargar();
            return "No se pudo guardar el cambio.";
        }
        if (!registrarEnHistorial(quien, p, cantidad, proveedor)) {
            return "El stock se actualizó, pero no se pudo registrar la entrada en el historial.";
        }
        return "";
    }

    public int getImportados() {
        return importados;
    }

    public int getOmitidos() {
        return omitidos;
    }

    /**
     * Importa productos desde un archivo con el mismo formato de productos.txt.
     * Los codigos que ya existen se omiten. Despues de llamarlo, getImportados() y getOmitidos()
     * dicen cuantos productos se cargaron y cuantas lineas se dejaron por repetidas o invalidas.
     */
    public String importar(Usuario quien, String ruta) {
        importados = 0;
        omitidos = 0;
        if (quien == null || !quien.puedeModificarPrecios()) {
            return "Solo un administrador puede importar productos.";
        }
        if (ruta == null || !java.nio.file.Files.exists(java.nio.file.Paths.get(ruta))) {
            return "No se encontró el archivo.";
        }
        ArchivoProductos origen = new ArchivoProductos(ruta);
        int totalLineas = origen.contarLineas();
        ArrayList<Producto> leidos = origen.cargar();
        ArrayList<Producto> nuevos = new ArrayList<>();
        for (int i = 0; i < leidos.size(); i++) {
            if (buscarPorCodigo(leidos.get(i).getCodigo()) == null) {
                productos.add(leidos.get(i));
                nuevos.add(leidos.get(i));
            }
        }
        if (nuevos.isEmpty()) {
            omitidos = totalLineas;
            if (totalLineas == 0) {
                return "El archivo no tiene productos.";
            }
            return "No se importó ningún producto: todos ya existen o tienen datos inválidos.";
        }
        if (!guardarCambios()) {
            recargar();
            return "No se pudieron guardar los productos importados.";
        }
        for (int i = 0; i < nuevos.size(); i++) {
            if (nuevos.get(i).getStock() > 0) {
                registrarEnHistorial(quien, nuevos.get(i), nuevos.get(i).getStock(), "Importacion");
            }
        }
        importados = nuevos.size();
        omitidos = totalLineas - importados;
        return "";
    }

    // ----- Alertas -----

    public ArrayList<Producto> productosBajoStock() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).estaBajoStock()) {
                resultado.add(productos.get(i));
            }
        }
        return resultado;
    }

    public ArrayList<Producto> productosPorVencer() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).estaPorVencer()) {
                resultado.add(productos.get(i));
            }
        }
        return resultado;
    }

    public ArrayList<Producto> productosVencidos() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).estaVencido()) {
                resultado.add(productos.get(i));
            }
        }
        return resultado;
    }

    // Guarda todo el inventario en productos.txt (se usa tambien despues de cada venta).
    public boolean guardarCambios() {
        return archivoProductos.guardarTodos(productos);
    }

    // ----- Metodos privados de ayuda -----

    private String validarDatos(String nombre, double precio, int stockMinimo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.contains(ArchivoTexto.SEPARADOR)) {
            return "Los datos no pueden contener el símbolo " + ArchivoTexto.SEPARADOR;
        }
        if (precio <= 0) {
            return "El precio debe ser mayor que 0.";
        }
        if (stockMinimo < 0) {
            return "El stock mínimo no puede ser negativo.";
        }
        return "";
    }

    // Vuelve a leer productos.txt para dejar la lista igual al archivo (si algo fallo al guardar).
    private void recargar() {
        ArrayList<Producto> delArchivo = archivoProductos.cargar();
        productos.clear();
        productos.addAll(delArchivo);
    }

    private boolean registrarEnHistorial(Usuario quien, Producto p, int cantidad, String proveedor) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String prov = "";
        if (proveedor != null) {
            prov = proveedor;
        }
        return archivoEntradas.guardarEntrada(new EntradaInventario(fecha, p.getCodigo(), p.getNombre(),
                cantidad, quien.getNombreUsuario(), prov));
    }
}
