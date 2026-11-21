package presentacion;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pinta la columna "Estado" de la tabla de productos con colores:
 * rojo = vencido, naranja = por vencer, amarillo = stock bajo, verde = OK.
 * HERENCIA: extiende DefaultTableCellRenderer y sobrescribe como se dibuja la celda.
 */
public class RenderizadorEstado extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
            boolean foco, int fila, int columna) {
        Component c = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
        setHorizontalAlignment(CENTER);
        setFont(Estilo.NEGRITA);
        String texto = String.valueOf(valor);
        if (!seleccionada) {
            setBackground(Color.WHITE);
            if (texto.equals("Vencido")) {
                setForeground(Estilo.ROJO);
            } else if (texto.equals("Por vencer")) {
                setForeground(new Color(0xD35400));
            } else if (texto.equals("Stock bajo")) {
                setForeground(Estilo.AMARILLO);
            } else {
                setForeground(Estilo.VERDE);
            }
        }
        return c;
    }
}
