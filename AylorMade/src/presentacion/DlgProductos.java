package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import logica.ServicioProductos;
import logica.SistemaSupermercado;
import modelo.Producto;
import modelo.Usuario;

/**
 * Pantalla de gestion de productos (CRUD): ver, buscar, agregar, editar, eliminar,
 * registrar entradas de mercaderia e importar productos desde un archivo.
 * Los usuarios sin permiso (cajeros) solo pueden consultar.
 */
public class DlgProductos extends JDialog implements ActionListener, ListSelectionListener {

    private SistemaSupermercado sistema;
    private Usuario usuario;
    private boolean puedeEditar;

    private JTextField txtBuscar = new JTextField(22);
    private JComboBox<String> cmbFiltro = new JComboBox<>(new String[]{"Todos", "Stock bajo", "Por vencer", "Vencidos"});
    private JButton btnBuscar = Estilo.boton("Buscar", Estilo.AZUL);
    private JLabel lblCantidad = new JLabel(" ");

    private DefaultTableModel modeloTabla = new DefaultTableModel(new String[]{
        "Código", "Nombre", "Tipo", "Precio base", "Precio final", "Stock", "Mínimo", "Vence", "Estado"}, 0);
    private JTable tabla = new JTable(modeloTabla);

    private JComboBox<String> cmbTipo = new JComboBox<>(new String[]{
        ServicioProductos.TIPO_GENERAL, ServicioProductos.TIPO_CANASTA, ServicioProductos.TIPO_PERECEDERO});
    private JTextField txtCodigo = new JTextField(10);
    private JTextField txtNombre = new JTextField(18);
    private JTextField txtPrecio = new JTextField(8);
    private JTextField txtStock = new JTextField(6);
    private JTextField txtMinimo = new JTextField(6);
    private JTextField txtVencimiento = new JTextField(9);

    private JButton btnAgregar = Estilo.boton("Agregar producto", Estilo.VERDE);
    private JButton btnGuardar = Estilo.boton("Guardar cambios", Estilo.AZUL);
    private JButton btnEliminar = Estilo.boton("Eliminar", Estilo.ROJO);
    private JButton btnLimpiar = Estilo.boton("Limpiar formulario", Estilo.GRIS);
    private JButton btnEntrada = Estilo.boton("Registrar entrada", Estilo.NARANJA);
    private JButton btnImportar = Estilo.boton("Importar desde archivo", Estilo.AZUL_CLARO);
    private JLabel lblMensaje = Estilo.mensaje();

    public DlgProductos(Frame padre, SistemaSupermercado sistema, Usuario usuario) {
        super(padre, "Productos e inventario", true);
        this.sistema = sistema;
        this.usuario = usuario;
        this.puedeEditar = usuario.puedeModificarPrecios();
        setIconImage(Imagenes.imagen("logo.png"));
        setSize(1180, 720);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Estilo.FONDO);

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        if (puedeEditar) {
            add(crearFormulario(), BorderLayout.SOUTH);
        }

        btnBuscar.addActionListener(this);
        txtBuscar.addActionListener(this);
        cmbFiltro.addActionListener(this);
        tabla.getSelectionModel().addListSelectionListener(this);

        limpiarFormulario();
        mostrarProductos();
    }

    // ----- Construccion de la pantalla -----

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.FONDO);
        barra.setBorder(new EmptyBorder(18, 22, 8, 22));
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);
        izquierda.add(Estilo.titulo("Productos", 24));
        izquierda.add(new JLabel("   "));
        Estilo.estilizarCampo(txtBuscar);
        txtBuscar.setToolTipText("Escriba parte del código o del nombre");
        izquierda.add(txtBuscar);
        izquierda.add(cmbFiltro);
        izquierda.add(btnBuscar);
        barra.add(izquierda, BorderLayout.WEST);
        lblCantidad.setFont(Estilo.NEGRITA);
        lblCantidad.setForeground(Estilo.GRIS);
        barra.add(lblCantidad, BorderLayout.EAST);
        return barra;
    }

    private JScrollPane crearTabla() {
        Estilo.estilizarTabla(tabla);
        int[] anchos = {80, 270, 110, 100, 100, 60, 60, 100, 100};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.getColumnModel().getColumn(8).setCellRenderer(new RenderizadorEstado());
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 22, 0, 22), BorderFactory.createLineBorder(Estilo.BORDE)));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setOpaque(false);
        return scroll;
    }

    private JPanel crearFormulario() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setBackground(Estilo.FONDO);
        contenedor.setBorder(new EmptyBorder(12, 22, 16, 22));

        JPanel form = Estilo.tarjeta();
        form.setLayout(new GridBagLayout());
        JTextField[] campos = {txtCodigo, txtNombre, txtPrecio, txtStock, txtMinimo, txtVencimiento};
        for (int i = 0; i < campos.length; i++) {
            Estilo.estilizarCampo(campos[i]);
        }
        txtVencimiento.setToolTipText("Formato AAAA-MM-DD (por ejemplo 2026-12-31)");
        agregarCampo(form, "TIPO", cmbTipo, 0, 1.0);
        agregarCampo(form, "CÓDIGO", txtCodigo, 1, 1.0);
        agregarCampo(form, "NOMBRE", txtNombre, 2, 3.0);
        agregarCampo(form, "PRECIO BASE (₡)", txtPrecio, 3, 1.0);
        agregarCampo(form, "STOCK INICIAL", txtStock, 4, 1.0);
        agregarCampo(form, "STOCK MÍNIMO", txtMinimo, 5, 1.0);
        agregarCampo(form, "VENCE (AAAA-MM-DD)", txtVencimiento, 6, 1.4);
        cmbTipo.addActionListener(this);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnAgregar);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(new JLabel("   "));
        botones.add(btnEntrada);
        botones.add(btnImportar);
        lblMensaje.setFont(Estilo.NEGRITA);
        lblMensaje.setPreferredSize(new Dimension(10, 24));

        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.setOpaque(false);
        abajo.add(botones, BorderLayout.NORTH);
        abajo.add(lblMensaje, BorderLayout.SOUTH);
        contenedor.add(form, BorderLayout.NORTH);
        contenedor.add(abajo, BorderLayout.CENTER);

        btnAgregar.addActionListener(this);
        btnGuardar.addActionListener(this);
        btnEliminar.addActionListener(this);
        btnLimpiar.addActionListener(this);
        btnEntrada.addActionListener(this);
        btnImportar.addActionListener(this);
        return contenedor;
    }

    private void agregarCampo(JPanel panel, String etiqueta, JComponent campo, int columna, double peso) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.weightx = peso;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 10);
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(Estilo.etiqueta(etiqueta), c);
        c.gridy = 1;
        panel.add(campo, c);
    }

    // ----- Datos de la tabla -----

    private void mostrarProductos() {
        modeloTabla.setRowCount(0);
        String texto = txtBuscar.getText().trim().toLowerCase();
        String filtro = (String) cmbFiltro.getSelectedItem();
        ArrayList<Producto> todos = sistema.getProductos().listar();
        int mostrados = 0;
        for (int i = 0; i < todos.size(); i++) {
            Producto p = todos.get(i);
            boolean coincide = texto.isEmpty() || p.getCodigo().toLowerCase().contains(texto)
                    || p.getNombre().toLowerCase().contains(texto);
            if (filtro.equals("Stock bajo") && !p.estaBajoStock()) {
                coincide = false;
            }
            if (filtro.equals("Por vencer") && !p.estaPorVencer()) {
                coincide = false;
            }
            if (filtro.equals("Vencidos") && !p.estaVencido()) {
                coincide = false;
            }
            if (coincide) {
                modeloTabla.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getTipo(),
                    Estilo.dinero(p.getPrecioBase()), Estilo.dinero(p.calcularPrecioFinal()),
                    p.getStock(), p.getStockMinimo(), p.getFechaVencimientoTexto(), estadoDe(p)});
                mostrados++;
            }
        }
        lblCantidad.setText(mostrados + " de " + todos.size() + " productos");
    }

    private String estadoDe(Producto p) {
        if (p.estaVencido()) {
            return "Vencido";
        }
        if (p.estaPorVencer()) {
            return "Por vencer";
        }
        if (p.estaBajoStock()) {
            return "Stock bajo";
        }
        return "OK";
    }

    private Producto productoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        String codigo = (String) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        return sistema.getProductos().buscarPorCodigo(codigo);
    }

    // ----- Formulario -----

    // Modo "agregar": todos los campos libres y solo el boton Agregar activo.
    private void limpiarFormulario() {
        if (!puedeEditar) {
            return;
        }
        tabla.clearSelection();
        cmbTipo.setSelectedIndex(0);
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        txtMinimo.setText("");
        txtVencimiento.setText("");
        cmbTipo.setEnabled(true);
        txtCodigo.setEnabled(true);
        txtStock.setEnabled(true);
        txtVencimiento.setEnabled(false);
        btnAgregar.setEnabled(true);
        btnGuardar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnEntrada.setEnabled(false);
    }

    // Modo "editar": se cargan los datos del producto elegido. El codigo, el tipo y el stock no se editan aqui.
    private void cargarFormulario(Producto p) {
        cmbTipo.setSelectedItem(p.getTipo());
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecioBase()));
        txtStock.setText(String.valueOf(p.getStock()));
        txtMinimo.setText(String.valueOf(p.getStockMinimo()));
        txtVencimiento.setText(p.getFechaVencimientoTexto());
        cmbTipo.setEnabled(false);
        txtCodigo.setEnabled(false);
        txtStock.setEnabled(false);
        txtVencimiento.setEnabled(p.getTipo().equals(ServicioProductos.TIPO_PERECEDERO));
        btnAgregar.setEnabled(false);
        btnGuardar.setEnabled(true);
        btnEliminar.setEnabled(true);
        btnEntrada.setEnabled(true);
        lblMensaje.setText(" ");
    }

    // Convierte el texto a numero; si no es valido muestra el error y devuelve -1.
    private double leerNumero(JTextField campo, String nombreCampo) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            Estilo.mensajeError(lblMensaje, "El campo \"" + nombreCampo + "\" debe ser un número.");
            return -1;
        }
    }

    // Lee la fecha del formulario. Devuelve null si esta vacia; si es invalida avisa y devuelve null con error.
    private LocalDate leerFecha(boolean[] valida) {
        valida[0] = true;
        String texto = txtVencimiento.getText().trim();
        if (texto.isEmpty() || !txtVencimiento.isEnabled()) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (Exception e) {
            valida[0] = false;
            Estilo.mensajeError(lblMensaje, "La fecha debe tener el formato AAAA-MM-DD (por ejemplo 2026-12-31).");
            return null;
        }
    }

    // ----- Eventos -----

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || !puedeEditar) {
            return;
        }
        Producto p = productoSeleccionado();
        if (p != null) {
            cargarFormulario(p);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == btnBuscar || origen == txtBuscar || origen == cmbFiltro) {
            mostrarProductos();
        } else if (origen == cmbTipo) {
            txtVencimiento.setEnabled(cmbTipo.isEnabled()
                    && cmbTipo.getSelectedItem().equals(ServicioProductos.TIPO_PERECEDERO));
        } else if (origen == btnLimpiar) {
            limpiarFormulario();
            lblMensaje.setText(" ");
        } else if (origen == btnAgregar) {
            agregarProducto();
        } else if (origen == btnGuardar) {
            guardarCambios();
        } else if (origen == btnEliminar) {
            eliminarProducto();
        } else if (origen == btnEntrada) {
            registrarEntrada();
        } else if (origen == btnImportar) {
            importarProductos();
        }
    }

    private void agregarProducto() {
        lblMensaje.setText(" ");
        double precio = leerNumero(txtPrecio, "Precio base");
        double stock = leerNumero(txtStock, "Stock inicial");
        double minimo = leerNumero(txtMinimo, "Stock mínimo");
        if (precio < 0 || stock < 0 || minimo < 0) {
            if (lblMensaje.getText().trim().isEmpty()) {
                Estilo.mensajeError(lblMensaje, "Los números no pueden ser negativos.");
            }
            return;
        }
        boolean[] valida = new boolean[1];
        LocalDate fecha = leerFecha(valida);
        if (!valida[0]) {
            return;
        }
        String error = sistema.getProductos().agregar(usuario, (String) cmbTipo.getSelectedItem(),
                txtCodigo.getText(), txtNombre.getText(), precio, (int) stock, (int) minimo, fecha);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mostrarProductos();
        limpiarFormulario();
        Estilo.mensajeExito(lblMensaje, "Producto agregado correctamente.");
    }

    private void guardarCambios() {
        Producto p = productoSeleccionado();
        if (p == null) {
            return;
        }
        lblMensaje.setText(" ");
        double precio = leerNumero(txtPrecio, "Precio base");
        double minimo = leerNumero(txtMinimo, "Stock mínimo");
        if (precio < 0 || minimo < 0) {
            if (lblMensaje.getText().trim().isEmpty()) {
                Estilo.mensajeError(lblMensaje, "Los números no pueden ser negativos.");
            }
            return;
        }
        boolean[] valida = new boolean[1];
        LocalDate fecha = leerFecha(valida);
        if (!valida[0]) {
            return;
        }
        String error = sistema.getProductos().editar(usuario, p.getCodigo(), txtNombre.getText(), precio, (int) minimo, fecha);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mostrarProductos();
        limpiarFormulario();
        Estilo.mensajeExito(lblMensaje, "Cambios guardados.");
    }

    private void eliminarProducto() {
        Producto p = productoSeleccionado();
        if (p == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar el producto \"" + p.getNombre() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        String error = sistema.getProductos().eliminar(usuario, p.getCodigo());
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mostrarProductos();
        limpiarFormulario();
        Estilo.mensajeExito(lblMensaje, "Producto eliminado.");
    }

    private void registrarEntrada() {
        Producto p = productoSeleccionado();
        if (p == null) {
            return;
        }
        String textoCantidad = JOptionPane.showInputDialog(this,
                "¿Cuántas unidades de \"" + p.getNombre() + "\" entraron?", "Registrar entrada", JOptionPane.QUESTION_MESSAGE);
        if (textoCantidad == null) {
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad.trim());
        } catch (NumberFormatException ex) {
            Estilo.mensajeError(lblMensaje, "La cantidad debe ser un número entero.");
            return;
        }
        String proveedor = JOptionPane.showInputDialog(this, "Proveedor (opcional):", "Registrar entrada", JOptionPane.QUESTION_MESSAGE);
        if (proveedor == null) {
            proveedor = "";
        }
        String error = sistema.getProductos().registrarEntrada(usuario, p.getCodigo(), cantidad, proveedor);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mostrarProductos();
        limpiarFormulario();
        Estilo.mensajeExito(lblMensaje, "Entrada registrada: +" + cantidad + " unidades de " + p.getNombre() + ".");
    }

    private void importarProductos() {
        JFileChooser selector = new JFileChooser(new File(System.getProperty("user.dir")));
        selector.setDialogTitle("Elija el archivo de productos (.txt)");
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String error = sistema.getProductos().importar(usuario, selector.getSelectedFile().getAbsolutePath());
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mostrarProductos();
        limpiarFormulario();
        Estilo.mensajeExito(lblMensaje, "Se importaron " + sistema.getProductos().getImportados()
                + " productos. Omitidos: " + sistema.getProductos().getOmitidos() + ".");
    }
}
