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
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import logica.SistemaSupermercado;
import modelo.Usuario;

/**
 * Gestion de usuarios (solo para el administrador): crear cajeros u otros administradores
 * y eliminar usuarios. Las contraseñas se encriptan en la capa de logica.
 */
public class DlgUsuarios extends JDialog implements ActionListener {

    private SistemaSupermercado sistema;
    private Usuario usuario;

    private DefaultTableModel modeloTabla = new DefaultTableModel(new String[]{"Usuario", "Nombre", "Correo", "Rol", "Caja"}, 0);
    private JTable tabla = new JTable(modeloTabla);

    private JTextField txtNombre = new JTextField(16);
    private JTextField txtUsuario = new JTextField(12);
    private JTextField txtCorreo = new JTextField(18);
    private JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Cajero", "Administrador"});
    private JSpinner spnCaja = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private JPasswordField txtClave = new JPasswordField(12);
    private JPasswordField txtConfirmar = new JPasswordField(12);
    private JButton btnCrear = Estilo.boton("Crear usuario", Estilo.VERDE);
    private JButton btnEliminar = Estilo.boton("Eliminar seleccionado", Estilo.ROJO);
    private JLabel lblMensaje = Estilo.mensaje();

    public DlgUsuarios(Frame padre, SistemaSupermercado sistema, Usuario usuario) {
        super(padre, "Usuarios", true);
        this.sistema = sistema;
        this.usuario = usuario;
        setIconImage(Imagenes.imagen("logo.png"));
        setSize(1020, 640);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Estilo.FONDO);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.FONDO);
        barra.setBorder(new EmptyBorder(18, 22, 8, 22));
        barra.add(Estilo.titulo("Usuarios del sistema", 24), BorderLayout.WEST);
        barra.add(btnEliminar, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        Estilo.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 22, 0, 22), BorderFactory.createLineBorder(Estilo.BORDE)));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
        add(crearFormulario(), BorderLayout.SOUTH);

        btnCrear.addActionListener(this);
        btnEliminar.addActionListener(this);
        cmbRol.addActionListener(this);
        mostrarUsuarios();
    }

    private JPanel crearFormulario() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setBackground(Estilo.FONDO);
        contenedor.setBorder(new EmptyBorder(12, 22, 16, 22));
        JPanel form = Estilo.tarjeta();
        form.setLayout(new GridBagLayout());
        JTextField[] campos = {txtNombre, txtUsuario, txtCorreo, txtClave, txtConfirmar};
        for (int i = 0; i < campos.length; i++) {
            Estilo.estilizarCampo(campos[i]);
        }
        agregarCampo(form, "NOMBRE COMPLETO", txtNombre, 0, 2.0);
        agregarCampo(form, "USUARIO", txtUsuario, 1, 1.0);
        agregarCampo(form, "CORREO", txtCorreo, 2, 2.0);
        agregarCampo(form, "ROL", cmbRol, 3, 1.0);
        agregarCampo(form, "N.º DE CAJA", spnCaja, 4, 0.6);
        agregarCampo(form, "CONTRASEÑA", txtClave, 5, 1.0);
        agregarCampo(form, "CONFIRMAR", txtConfirmar, 6, 1.0);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        abajo.setOpaque(false);
        abajo.add(btnCrear);
        lblMensaje.setFont(Estilo.NEGRITA);
        lblMensaje.setPreferredSize(new Dimension(520, 24));
        abajo.add(lblMensaje);
        contenedor.add(form, BorderLayout.NORTH);
        contenedor.add(abajo, BorderLayout.CENTER);
        return contenedor;
    }

    private void agregarCampo(JPanel panel, String etiqueta, JComponent campo, int columna, double peso) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.weightx = peso;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 10);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        panel.add(Estilo.etiqueta(etiqueta), c);
        c.gridy = 1;
        panel.add(campo, c);
    }

    private void mostrarUsuarios() {
        modeloTabla.setRowCount(0);
        ArrayList<Usuario> lista = sistema.getUsuarios().listar();
        for (int i = 0; i < lista.size(); i++) {
            Usuario u = lista.get(i);
            String caja = "-";
            if (u.getNumeroCaja() > 0) {
                caja = String.valueOf(u.getNumeroCaja());
            }
            modeloTabla.addRow(new Object[]{u.getNombreUsuario(), u.getNombre(), u.getCorreo(), u.getRol(), caja});
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == cmbRol) {
            spnCaja.setEnabled(cmbRol.getSelectedItem().equals("Cajero"));
        } else if (origen == btnCrear) {
            crearUsuario();
        } else if (origen == btnEliminar) {
            eliminarUsuario();
        }
    }

    private void crearUsuario() {
        String clave = new String(txtClave.getPassword());
        if (!clave.equals(new String(txtConfirmar.getPassword()))) {
            Estilo.mensajeError(lblMensaje, "Las contraseñas no coinciden.");
            return;
        }
        int caja = 0;
        if (cmbRol.getSelectedItem().equals("Cajero")) {
            caja = (Integer) spnCaja.getValue();
        }
        String error = sistema.getUsuarios().registrar(usuario, (String) cmbRol.getSelectedItem(),
                txtNombre.getText(), txtUsuario.getText(), txtCorreo.getText(), clave, caja);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        txtNombre.setText("");
        txtUsuario.setText("");
        txtCorreo.setText("");
        txtClave.setText("");
        txtConfirmar.setText("");
        mostrarUsuarios();
        Estilo.mensajeExito(lblMensaje, "Usuario creado correctamente.");
    }

    private void eliminarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.mensajeError(lblMensaje, "Elija un usuario de la tabla.");
            return;
        }
        String nombreUsuario = (String) modeloTabla.getValueAt(fila, 0);
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar al usuario \"" + nombreUsuario + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        String error = sistema.getUsuarios().eliminar(usuario, nombreUsuario);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mostrarUsuarios();
        Estilo.mensajeExito(lblMensaje, "Usuario eliminado.");
    }
}
