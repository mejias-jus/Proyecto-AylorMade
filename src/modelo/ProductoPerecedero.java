package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * HERENCIA: producto con fecha de vencimiento.
 * POLIMORFISMO: si le quedan 3 dias o menos aplica 30% de descuento,
 * y si ya vencio no se puede vender.
 */
public class ProductoPerecedero extends Producto {

    public static final double DESCUENTO_VENCIMIENTO = 0.30;
    public static final int DIAS_ALERTA = 3;

    private LocalDate fechaVencimiento;

    public ProductoPerecedero(String codigo, String nombre, double precioBase, int stock,
            int stockMinimo, LocalDate fechaVencimiento) {
        super(codigo, nombre, precioBase, stock, stockMinimo);
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento != null) {
            this.fechaVencimiento = fechaVencimiento;
        }
    }

    public int diasParaVencer() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    public boolean estaVencido() {
        return diasParaVencer() < 0;
    }

    public boolean estaPorVencer() {
        int dias = diasParaVencer();
        return dias >= 0 && dias <= DIAS_ALERTA;
    }

    @Override
    public double calcularPrecioFinal() {
        double precio = getPrecioBase();
        if (estaPorVencer()) {
            precio = precio - precio * DESCUENTO_VENCIMIENTO;
        }
        return precio + precio * IVA;
    }

    @Override
    public boolean puedeVenderse() {
        return super.puedeVenderse() && !estaVencido();
    }

    @Override
    public String getFechaVencimientoTexto() {
        return fechaVencimiento.toString();
    }

    @Override
    public String getTipo() {
        return "Perecedero";
    }
}
