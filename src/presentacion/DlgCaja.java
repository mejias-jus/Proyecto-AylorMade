package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import logica.SistemaSupermercado;
import modelo.Efectivo;
import modelo.ItemVenta;
import modelo.MetodoPago;
import modelo.Producto;
import modelo.Sinpe;
import modelo.Tarjeta;
import modelo.Usuario;
import modelo.Venta;

/**
 * Caja de cobro: se agregan productos por codigo (o escaneando el codigo de barras),
 * se elige el metodo de pago y se cobra. Todas las reglas las aplica la capa de logica;
 * esta pantalla solo muestra los resultados y los mensajes de error.
 */
public class DlgCaja extends JDialog implements ActionListener, ListSelectionListener {

    private SistemaSupermercado sistema;
    private Usuario usuario;
    private Venta venta;

    private JLabel lblVenta = Estilo.titulo("Venta", 22);

    // Catalogo (izquierda)
    private JTextField txtBuscar = new JTextField(14);
    private JButton btnBuscar = Estilo.boton("Buscar", Estilo.AZUL);
    private DefaultTableModel modeloCatalogo = new DefaultTableModel(new String[]{"Código", "Producto", "Precio", "Stock"}, 0);
    private JTable tablaCatalogo = new JTable(modeloCatalogo);

    // Carrito (centro)
    private JTextField txtCodigo = new JTextField(14);
    private JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private JButton btnAgregar = Estilo.boton("Agregar", Estilo.VERDE);
    private DefaultTableModel modeloCarrito = new DefaultTableModel(new String[]{"Código", "Producto", "Cant.", "Precio", "Subtotal"}, 0);
    private JTable tablaCarrito = new JTable(modeloCarrito);
    private JButton btnQuitar = Estilo.boton("Quitar seleccionado", Estilo.GRIS);
    private JButton btnCancelar = Estilo.boton("Cancelar venta", Estilo.ROJO);
    private JLabel lblMensaje = Estilo.mensaje();

    // Cobro (derecha)
    private JLabel lblTotal = new JLabel("₡0.00");
    private JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "SINPE Móvil"});
    private JLabel lblPago = Estilo.etiqueta("MONTO RECIBIDO (₡)");
    private JTextField txtPago = new JTextField(12);
    private JTextField txtCorreoCliente = new JTextField(12);
    private JButton btnCobrar = Estilo.boton("COBRAR", Estilo.VERDE);

    public DlgCaja(Frame padre, SistemaSupermercado sistema, Usuario usuario) {
        super(padre, "Caja de cobro", true);
        this.sistema = sistema;
        this.usuario = usuario;
        setIconImage(Imagenes.imagen("logo.png"));
        setSize(1240, 720);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(14, 0));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.FONDO);
        encabezado.setBorder(new EmptyBorder(16, 22, 4, 22));
        encabezado.add(lblVenta, BorderLayout.WEST);
        JLabel lblCajero = new JLabel("Atiende: " + usuario.getNombre());
        lblCajero.setForeground(Estilo.GRIS);
        lblCajero.setFont(Estilo.NEGRITA);
        encabezado.add(lblCajero, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(14, 0));
        cuerpo.setBackground(Estilo.FONDO);
        cuerpo.setBorder(new EmptyBorder(8, 22, 18, 22));
        cuerpo.add(crearCatalogo(), BorderLayout.WEST);
        cuerpo.add(crearCarrito(), BorderLayout.CENTER);
        cuerpo.add(crearCobro(), BorderLayout.EAST);
        add(cuerpo, BorderLayout.CENTER);

        btnBuscar.addActionListener(this);
        txtBuscar.addActionListener(this);
        txtCodigo.addActionListener(this);
        btnAgregar.addActionListener(this);
        btnQuitar.addActionListener(this);
        btnCancelar.addActionListener(this);
        cmbMetodo.addActionListener(this);
        btnCobrar.addActionListener(this);
        txtPago.addActionListener(this);
        tablaCatalogo.getSelectionModel().addListSelectionListener(this);

        mostrarCatalogo();
        nuevaVenta();
    }

    // ----- Construccion de la pantalla -----

    private JPanel crearCatalogo() {
        JPanel p = Estilo.tarjeta();
        p.setLayout(new BorderLayout(0, 10));
        p.setPreferredSize(new Dimension(390, 100));
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        arriba.setOpaque(false);
        Estilo.estilizarCampo(txtBuscar);
        arriba.add(txtBuscar);
        arriba.add(btnBuscar);
        JPanel cabecera = new JPanel(new BorderLayout(0, 8));
        cabecera.setOpaque(false);
        cabecera.add(Estilo.titulo("Productos", 17), BorderLayout.NORTH);
        cabecera.add(arriba, BorderLayout.CENTER);
        p.add(cabecera, BorderLayout.NORTH);

        Estilo.estilizarTabla(tablaCatalogo);
        tablaCatalogo.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaCatalogo.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaCatalogo.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaCatalogo.getColumnModel().getColumn(3).setPreferredWidth(45);
        JScrollPane scroll = new JScrollPane(tablaCatalogo);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        p.add(scroll, BorderLayout.CENTER);
        JLabel ayuda = Estilo.etiqueta("Elija un producto para copiar su código.");
        p.add(ayuda, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearCarrito() {
        JPanel p = Estilo.tarjeta();
        p.setLayout(new BorderLayout(0, 10));

        JPanel entrada = new JPanel(new GridBagLayout());
        entrada.setOpaque(false);
        Estilo.estilizarCampo(txtCodigo);
        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtCodigo.setToolTipText("Escriba el código o escanee el código de barras y presione Enter");
        spnCantidad.setPreferredSize(new Dimension(70, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 4, 10);
        c.gridy = 0;
        c.gridx = 0;
        entrada.add(Estilo.etiqueta("CÓDIGO / CÓDIGO DE BARRAS"), c);
        c.gridx = 1;
        entrada.add(Estilo.etiqueta("CANT."), c);
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        entrada.add(txtCodigo, c);
        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        entrada.add(spnCantidad, c);
        c.gridx = 2;
        c.insets = new Insets(0, 0, 4, 0);
        entrada.add(btnAgregar, c);

        JPanel cabecera = new JPanel(new BorderLayout(0, 8));
        cabecera.setOpaque(false);
        cabecera.add(Estilo.titulo("Carrito", 17), BorderLayout.NORTH);
        cabecera.add(entrada, BorderLayout.CENTER);
        lblMensaje.setFont(Estilo.NEGRITA);
        lblMensaje.setPreferredSize(new Dimension(10, 24));
        cabecera.add(lblMensaje, BorderLayout.SOUTH);
        p.add(cabecera, BorderLayout.NORTH);

        Estilo.estilizarTabla(tablaCarrito);
        int[] anchos = {70, 220, 50, 90, 100};
        for (int i = 0; i < anchos.length; i++) {
            tablaCarrito.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        JScrollPane scroll = new JScrollPane(tablaCarrito);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        p.add(scroll, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnQuitar);
        botones.add(btnCancelar);
        p.add(botones, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearCobro() {
        JPanel col = Estilo.columna(22);
        col.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), new EmptyBorder(22, 22, 22, 22)));
        col.setPreferredSize(new Dimension(310, 100));

        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblTotal.setForeground(Estilo.VERDE);
        Estilo.estilizarCampo(txtPago);
        Estilo.estilizarCampo(txtCorreoCliente);
        txtCorreoCliente.setToolTipText("Si lo escribe, la factura se envía a este correo");
        txtPago.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCobrar.setBorder(new EmptyBorder(16, 18, 16, 18));

        Estilo.agregar(col, Estilo.titulo("Cobro", 17), 0);
        Estilo.agregar(col, Estilo.etiqueta("TOTAL A PAGAR"), 18);
        Estilo.agregar(col, lblTotal, 2);
        Estilo.agregar(col, Estilo.etiqueta("MÉTODO DE PAGO"), 22);
        Estilo.agregar(col, cmbMetodo, 6);
        Estilo.agregar(col, lblPago, 16);
        Estilo.agregar(col, txtPago, 6);
        Estilo.agregar(col, Estilo.etiqueta("CORREO DEL CLIENTE (OPCIONAL)"), 16);
        Estilo.agregar(col, txtCorreoCliente, 6);
        Estilo.agregar(col, Estilo.etiqueta("Se le envía la factura a este correo."), 4);
        Estilo.agregar(col, btnCobrar, 22);
        col.add(Box.createVerticalGlue());
        return col;
    }

    // ----- Datos -----

    private void nuevaVenta() {
        venta = sistema.getVentas().iniciarVenta(usuario);
        lblVenta.setText("Venta N.º " + venta.getNumero());
        txtCodigo.setText("");
        txtPago.setText("");
        txtCorreoCliente.setText("");
        spnCantidad.setValue(1);
        lblMensaje.setText(" ");
        mostrarCarrito();
        txtCodigo.requestFocusInWindow();
    }

    private void mostrarCatalogo() {
        modeloCatalogo.setRowCount(0);
        String texto = txtBuscar.getText().trim().toLowerCase();
        ArrayList<Producto> todos = sistema.getProductos().listar();
        for (int i = 0; i < todos.size(); i++) {
            Producto p = todos.get(i);
            if (texto.isEmpty() || p.getCodigo().toLowerCase().contains(texto) || p.getNombre().toLowerCase().contains(texto)) {
                modeloCatalogo.addRow(new Object[]{p.getCodigo(), p.getNombre(), Estilo.dinero(p.calcularPrecioFinal()), p.getStock()});
            }
        }
    }

    private void mostrarCarrito() {
        modeloCarrito.setRowCount(0);
        ArrayList<ItemVenta> items = venta.getItems();
        for (int i = 0; i < items.size(); i++) {
            ItemVenta item = items.get(i);
            modeloCarrito.addRow(new Object[]{item.getProducto().getCodigo(), item.getProducto().getNombre(),
                item.getCantidad(), Estilo.dinero(item.getProducto().calcularPrecioFinal()), Estilo.dinero(item.getSubtotal())});
        }
        lblTotal.setText(Estilo.dinero(venta.calcularTotal()));
    }

    // ----- Eventos -----

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || tablaCatalogo.getSelectedRow() < 0) {
            return;
        }
        int fila = tablaCatalogo.convertRowIndexToModel(tablaCatalogo.getSelectedRow());
        txtCodigo.setText((String) modeloCatalogo.getValueAt(fila, 0));
        txtCodigo.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == btnBuscar || origen == txtBuscar) {
            mostrarCatalogo();
        } else if (origen == btnAgregar || origen == txtCodigo) {
            agregarAlCarrito();
        } else if (origen == btnQuitar) {
            quitarDelCarrito();
        } else if (origen == btnCancelar) {
            int respuesta = JOptionPane.showConfirmDialog(this, "¿Cancelar esta venta y vaciar el carrito?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (respuesta == JOptionPane.YES_OPTION) {
                nuevaVenta();
            }
        } else if (origen == cmbMetodo) {
            cambiarMetodo();
        } else if (origen == btnCobrar || origen == txtPago) {
            cobrar();
        }
    }

    private void agregarAlCarrito() {
        int cantidad = (Integer) spnCantidad.getValue();
        String error = sistema.getVentas().agregarAlCarrito(venta, txtCodigo.getText(), cantidad);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            txtCodigo.selectAll();
            txtCodigo.requestFocusInWindow();
            return;
        }
        lblMensaje.setText(" ");
        txtCodigo.setText("");
        spnCantidad.setValue(1);
        tablaCatalogo.clearSelection();
        mostrarCarrito();
        txtCodigo.requestFocusInWindow();
    }

    private void quitarDelCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) {
            Estilo.mensajeError(lblMensaje, "Elija un producto del carrito para quitarlo.");
            return;
        }
        String codigo = (String) modeloCarrito.getValueAt(fila, 0);
        String error = sistema.getVentas().quitarDelCarrito(venta, codigo);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        lblMensaje.setText(" ");
        mostrarCarrito();
    }

    private void cambiarMetodo() {
        String metodo = (String) cmbMetodo.getSelectedItem();
        if (metodo.equals("Efectivo")) {
            lblPago.setText("MONTO RECIBIDO (₡)");
        } else if (metodo.equals("Tarjeta")) {
            lblPago.setText("ÚLTIMOS 4 DÍGITOS DE LA TARJETA");
        } else {
            lblPago.setText("TELÉFONO SINPE (8 DÍGITOS)");
        }
        txtPago.setText("");
        txtPago.requestFocusInWindow();
    }

    // Crea el metodo de pago segun lo elegido. Devuelve null si el monto de efectivo no es un numero.
    private MetodoPago crearMetodoPago() {
        String metodo = (String) cmbMetodo.getSelectedItem();
        String dato = txtPago.getText().trim();
        if (metodo.equals("Efectivo")) {
            try {
                return new Efectivo(Double.parseDouble(dato.replace(",", ".")));
            } catch (NumberFormatException ex) {
                return null;
            }
        } else if (metodo.equals("Tarjeta")) {
            return new Tarjeta(dato);
        }
        return new Sinpe(dato);
    }

    private void cobrar() {
        MetodoPago metodo = crearMetodoPago();
        if (metodo == null) {
            Estilo.mensajeError(lblMensaje, "Escriba el monto recibido como número.");
            return;
        }
        String errorCorreo = sistema.getVentas().validarCorreoCliente(txtCorreoCliente.getText());
        if (!errorCorreo.isEmpty()) {
            Estilo.mensajeError(lblMensaje, errorCorreo);
            return;
        }
        String error = sistema.getVentas().cobrar(venta, metodo);
        if (venta.estaCerrada()) {
            // La venta se cobro y ya quedo guardada. Despues se intenta enviar la factura por correo.
            String estadoCorreo = "";
            boolean correoEnviado = true;
            String correoCliente = txtCorreoCliente.getText().trim();
            if (!correoCliente.isEmpty()) {
                Estilo.mensajeExito(lblMensaje, "Enviando la factura...");
                lblMensaje.paintImmediately(lblMensaje.getBounds());
                setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                String errorEnvio = sistema.getVentas().enviarFactura(venta, correoCliente);
                setCursor(java.awt.Cursor.getDefaultCursor());
                if (errorEnvio.isEmpty()) {
                    estadoCorreo = "Factura enviada a " + correoCliente + ".";
                } else {
                    correoEnviado = false;
                    estadoCorreo = "La venta quedó registrada, pero no se pudo enviar la factura. " + errorEnvio;
                }
            }
            mostrarFactura(estadoCorreo, correoEnviado);
            if (!error.isEmpty()) {
                JOptionPane.showMessageDialog(this, error, "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            mostrarCatalogo();
            nuevaVenta();
        } else {
            Estilo.mensajeError(lblMensaje, error);
        }
    }

    private void mostrarFactura(String estadoCorreo, boolean correoEnviado) {
        JTextArea area = new JTextArea(venta.generarFactura());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(430, 320));
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(scroll, BorderLayout.CENTER);
        if (!estadoCorreo.isEmpty()) {
            JLabel lblEstado = new JLabel("<html><body style='width:400px'>" + estadoCorreo + "</body></html>");
            lblEstado.setFont(Estilo.NEGRITA);
            if (correoEnviado) {
                lblEstado.setForeground(Estilo.VERDE);
            } else {
                lblEstado.setForeground(Estilo.ROJO);
            }
            panel.add(lblEstado, BorderLayout.SOUTH);
        }
        JOptionPane.showMessageDialog(this, panel, "Venta cobrada", JOptionPane.INFORMATION_MESSAGE);
    }
}
