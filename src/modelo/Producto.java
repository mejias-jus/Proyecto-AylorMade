package modelo;

/**
 * CLASE PADRE de los productos (producto general, gravado con IVA normal).
 * ENCAPSULAMIENTO: atributos privados, el stock solo cambia con metodos que lo validan.
 * HERENCIA: ProductoPerecedero y ProductoCanastaBasica extienden esta clase.
 * POLIMORFISMO: calcularPrecioFinal(), puedeVenderse() y getTipo() se sobrescriben.
 */
public class Producto {

    public static final double IVA = 0.13;

    private String codigo;
    private String nombre;
    private double precioBase;
    private int stock;
    private int stockMinimo;

    public Producto(String codigo, String nombre, double precioBase, int stock, int stockMinimo) {
        this.codigo = "";
        this.nombre = "";
        this.precioBase = 0;
        this.stock = 0;
        this.stockMinimo = 0;
        setCodigo(codigo);
        setNombre(nombre);
        setPrecioBase(precioBase);
        setStock(stock);
        setStockMinimo(stockMinimo);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo != null && !codigo.trim().isEmpty()) {
            this.codigo = codigo.trim();
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        }
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(double precioBase) {
        if (precioBase >= 0) {
            this.precioBase = precioBase;
        }
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock >= 0) {
            this.stock = stock;
        }
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        if (stockMinimo >= 0) {
            this.stockMinimo = stockMinimo;
        }
    }

    // ----- Logica de stock -----

    public boolean hayStock(int cantidad) {
        return cantidad > 0 && cantidad <= stock;
    }

    public boolean descontarStock(int cantidad) {
        if (hayStock(cantidad)) {
            stock = stock - cantidad;
            return true;
        }
        return false;
    }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            stock = stock + cantidad;
        }
    }

    public boolean estaBajoStock() {
        return stock <= stockMinimo;
    }

    // ----- Metodos que las clases hijas SOBRESCRIBEN (polimorfismo) -----

    public double calcularPrecioFinal() {
        return precioBase + precioBase * IVA;
    }

    public boolean puedeVenderse() {
        return stock > 0;
    }

    public String getTipo() {
        return "General";
    }
}
