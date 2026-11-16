package modelo;

/**
 * HERENCIA: producto de la canasta basica.
 * POLIMORFISMO: calcula el precio final con una tarifa de IVA reducida
 * (en lugar del 13% del producto general).
 * Nota: confirmar la tarifa vigente con Hacienda antes de la defensa.
 */
public class ProductoCanastaBasica extends Producto {

    public static final double IVA_REDUCIDO = 0.01;

    public ProductoCanastaBasica(String codigo, String nombre, double precioBase, int stock, int stockMinimo) {
        super(codigo, nombre, precioBase, stock, stockMinimo);
    }

    @Override
    public double calcularPrecioFinal() {
        return getPrecioBase() + getPrecioBase() * IVA_REDUCIDO;
    }

    @Override
    public String getTipo() {
        return "Canasta basica";
    }
}
