package presentacion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

/**
 * Colores, fuentes y metodos para crear componentes con el mismo aspecto en todas las pantallas
 * de AylorMade. Asi el diseno se cambia en un solo lugar.
 */
public class Estilo {

    public static final Color AZUL = new Color(0x1E3A5F);
    public static final Color AZUL_CLARO = new Color(0x2F5D8C);
    public static final Color NARANJA = new Color(0xF28C28);
    public static final Color FONDO = new Color(0xF4F6F8);
    public static final Color VERDE = new Color(0x2E7D32);
    public static final Color ROJO = new Color(0xC62828);
    public static final Color AMARILLO = new Color(0xB26A00);
    public static final Color GRIS = new Color(0x607080);
    public static final Color BORDE = new Color(0xD5DBE1);

    public static final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font NEGRITA = new Font("Segoe UI", Font.BOLD, 14);

    // Se llama una vez al iniciar el programa: cambia la fuente por defecto de los componentes.
    public static void aplicarTema() {
        String[] claves = {"Label.font", "TextField.font", "PasswordField.font", "ComboBox.font",
            "Table.font", "TextArea.font", "CheckBox.font", "Spinner.font", "List.font",
            "OptionPane.messageFont", "TitledBorder.font", "TextField.font"};
        for (int i = 0; i < claves.length; i++) {
            UIManager.put(claves[i], FUENTE);
        }
        UIManager.put("Button.font", NEGRITA);
        UIManager.put("OptionPane.buttonFont", NEGRITA);
        UIManager.put("TableHeader.font", NEGRITA);
    }

    public static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFont(NEGRITA);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Boton de texto sin fondo (para enlaces como "Olvido su contrasena").
    public static JButton enlace(String texto) {
        JButton b = new JButton(texto);
        b.setForeground(AZUL_CLARO);
        b.setFont(FUENTE);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel titulo(String texto, int tamano) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, tamano));
        l.setForeground(AZUL);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(GRIS);
        return l;
    }

    public static void estilizarCampo(JTextComponent c) {
        c.setFont(FUENTE);
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1), new EmptyBorder(6, 8, 6, 8)));
    }

    // Panel blanco con borde fino, como una tarjeta.
    public static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    // Panel en columna (de arriba hacia abajo) para formularios.
    public static JPanel columna(int margen) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(margen, margen, margen, margen));
        return p;
    }

    // Agrega un componente a una columna, alineado a la izquierda y con un espacio antes.
    public static void agregar(JPanel columna, JComponent c, int espacioAntes) {
        if (espacioAntes > 0) {
            columna.add(javax.swing.Box.createRigidArea(new Dimension(0, espacioAntes)));
        }
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        columna.add(c);
    }

    public static void estilizarTabla(JTable t) {
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE6EAEE));
        t.setSelectionBackground(new Color(0xD6E6F5));
        t.setSelectionForeground(AZUL);
        t.setFillsViewportHeight(true);
        t.setDefaultEditor(Object.class, null); // celdas no editables
        JTableHeader h = t.getTableHeader();
        h.setBackground(AZUL);
        h.setForeground(Color.WHITE);
        h.setFont(NEGRITA);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));
        h.setReorderingAllowed(false);
    }

    // Etiqueta para mostrar mensajes de error o exito: reserva espacio para dos lineas.
    public static JLabel mensaje() {
        JLabel l = new JLabel(" ");
        l.setFont(FUENTE);
        l.setVerticalAlignment(SwingConstants.TOP);
        l.setPreferredSize(new Dimension(10, 40));
        return l;
    }

    // El texto va dentro de <html> para que se parta en dos lineas si es largo.
    public static void mensajeError(JLabel l, String texto) {
        l.setForeground(ROJO);
        l.setText("<html>" + texto + "</html>");
    }

    public static void mensajeExito(JLabel l, String texto) {
        l.setForeground(VERDE);
        l.setText("<html>" + texto + "</html>");
    }

    public static String dinero(double valor) {
        return "₡" + String.format(Locale.US, "%,.2f", valor);
    }
}
