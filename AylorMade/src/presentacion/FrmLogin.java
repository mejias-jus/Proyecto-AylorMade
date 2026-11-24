package presentacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import logica.SistemaSupermercado;
import modelo.Usuario;

/**
 * Pantalla de inicio de sesion y registro.
 * - Si todavia no hay usuarios, muestra "Crear administrador" (el primer registro).
 * - Si ya hay usuarios, muestra el inicio de sesion.
 * La pantalla NO tiene reglas de negocio: solo toma lo que el usuario escribe y llama a los servicios.
 * Implementa ActionListener: el metodo actionPerformed recibe los clics de los botones.
 */
public class FrmLogin extends JFrame implements ActionListener {

    private SistemaSupermercado sistema;

    private CardLayout cartas = new CardLayout();
    private JPanel panelCartas = new JPanel(cartas);

    // Tarjeta de inicio de sesion
    private JTextField txtUsuario = new JTextField(20);
    private JPasswordField txtClave = new JPasswordField(20);
    private JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
    private JButton btnIngresar = Estilo.boton("Ingresar", Estilo.AZUL);
    private JButton btnOlvido = Estilo.enlace("¿Olvidó su contraseña?");
    private JLabel lblMensajeLogin = Estilo.mensaje();

    // Tarjeta de registro del primer administrador
    private JTextField txtNombre = new JTextField(20);
    private JTextField txtUsuarioNuevo = new JTextField(20);
    private JTextField txtCorreo = new JTextField(20);
    private JPasswordField txtClaveNueva = new JPasswordField(20);
    private JPasswordField txtClaveConfirmar = new JPasswordField(20);
    private JButton btnCrearAdmin = Estilo.boton("Crear administrador", Estilo.NARANJA);
    private JLabel lblMensajeRegistro = Estilo.mensaje();

    public FrmLogin(SistemaSupermercado sistema) {
        this.sistema = sistema;
        setTitle("AylorMade - Iniciar sesión");
        setIconImage(Imagenes.imagen("logo.png"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(crearPanelMarca(), BorderLayout.WEST);
        panelCartas.add(crearTarjetaLogin(), "login");
        panelCartas.add(crearTarjetaRegistro(), "registro");
        add(panelCartas, BorderLayout.CENTER);

        if (sistema.getUsuarios().hayUsuarios()) {
            cartas.show(panelCartas, "login");
        } else {
            cartas.show(panelCartas, "registro");
        }
    }

    // ----- Construccion de la pantalla -----

    private JPanel crearPanelMarca() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Estilo.AZUL);
        p.setPreferredSize(new Dimension(380, 580));

        JLabel logo = new JLabel(Imagenes.icono("logo.png", 150));
        JLabel nombre = new JLabel("AylorMade");
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 38));
        nombre.setForeground(Color.WHITE);
        JLabel lema = new JLabel("Cobro rápido, inventario al día");
        lema.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lema.setForeground(new Color(0xC9D6E8));
        JLabel pie = new JLabel("Programación Orientada a Objetos · UTN 2026");
        pie.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pie.setForeground(new Color(0x8FA5C2));

        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lema.setAlignmentX(Component.CENTER_ALIGNMENT);
        pie.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(logo);
        p.add(Box.createRigidArea(new Dimension(0, 18)));
        p.add(nombre);
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(lema);
        p.add(Box.createVerticalGlue());
        p.add(pie);
        p.add(Box.createRigidArea(new Dimension(0, 20)));
        return p;
    }

    private JPanel crearTarjetaLogin() {
        JPanel col = Estilo.columna(60);
        Estilo.estilizarCampo(txtUsuario);
        Estilo.estilizarCampo(txtClave);
        chkMostrar.setBackground(Color.WHITE);
        chkMostrar.setForeground(Estilo.GRIS);

        Estilo.agregar(col, Estilo.titulo("Iniciar sesión", 28), 40);
        Estilo.agregar(col, Estilo.etiqueta("Ingrese con su usuario y contraseña"), 4);
        Estilo.agregar(col, Estilo.etiqueta("USUARIO"), 28);
        Estilo.agregar(col, txtUsuario, 6);
        Estilo.agregar(col, Estilo.etiqueta("CONTRASEÑA"), 16);
        Estilo.agregar(col, txtClave, 6);
        Estilo.agregar(col, chkMostrar, 8);
        Estilo.agregar(col, lblMensajeLogin, 8);
        Estilo.agregar(col, btnIngresar, 10);
        Estilo.agregar(col, btnOlvido, 10);
        col.add(Box.createVerticalGlue());

        txtUsuario.addActionListener(this);
        txtClave.addActionListener(this);
        chkMostrar.addActionListener(this);
        btnIngresar.addActionListener(this);
        btnOlvido.addActionListener(this);
        return col;
    }

    private JPanel crearTarjetaRegistro() {
        JPanel col = Estilo.columna(50);
        JTextField[] campos = {txtNombre, txtUsuarioNuevo, txtCorreo, txtClaveNueva, txtClaveConfirmar};
        for (int i = 0; i < campos.length; i++) {
            Estilo.estilizarCampo(campos[i]);
        }

        Estilo.agregar(col, Estilo.titulo("Bienvenido a AylorMade", 26), 10);
        Estilo.agregar(col, Estilo.etiqueta("Es la primera vez que se abre el sistema. Cree la cuenta del administrador."), 4);
        Estilo.agregar(col, Estilo.etiqueta("NOMBRE COMPLETO"), 18);
        Estilo.agregar(col, txtNombre, 4);
        Estilo.agregar(col, Estilo.etiqueta("USUARIO"), 10);
        Estilo.agregar(col, txtUsuarioNuevo, 4);
        Estilo.agregar(col, Estilo.etiqueta("CORREO ELECTRÓNICO (para recuperar la contraseña)"), 10);
        Estilo.agregar(col, txtCorreo, 4);
        Estilo.agregar(col, Estilo.etiqueta("CONTRASEÑA (mínimo 8 caracteres, con letras y números)"), 10);
        Estilo.agregar(col, txtClaveNueva, 4);
        Estilo.agregar(col, Estilo.etiqueta("CONFIRMAR CONTRASEÑA"), 10);
        Estilo.agregar(col, txtClaveConfirmar, 4);
        Estilo.agregar(col, lblMensajeRegistro, 8);
        Estilo.agregar(col, btnCrearAdmin, 8);
        col.add(Box.createVerticalGlue());

        txtClaveConfirmar.addActionListener(this);
        btnCrearAdmin.addActionListener(this);
        return col;
    }

    // ----- Eventos -----

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == btnIngresar || origen == txtClave || origen == txtUsuario) {
            ingresar();
        } else if (origen == chkMostrar) {
            if (chkMostrar.isSelected()) {
                txtClave.setEchoChar((char) 0);
            } else {
                txtClave.setEchoChar('•');
            }
        } else if (origen == btnOlvido) {
            new DlgRecuperarClave(this, sistema).setVisible(true);
        } else if (origen == btnCrearAdmin || origen == txtClaveConfirmar) {
            crearAdministrador();
        }
    }

    private void ingresar() {
        String clave = new String(txtClave.getPassword());
        Usuario usuario = sistema.getUsuarios().iniciarSesion(txtUsuario.getText(), clave);
        if (usuario == null) {
            Estilo.mensajeError(lblMensajeLogin, "Usuario o contraseña incorrectos.");
            txtClave.setText("");
            return;
        }
        new FrmPrincipal(sistema, usuario).setVisible(true);
        dispose();
    }

    private void crearAdministrador() {
        String clave = new String(txtClaveNueva.getPassword());
        String confirmar = new String(txtClaveConfirmar.getPassword());
        if (!clave.equals(confirmar)) {
            Estilo.mensajeError(lblMensajeRegistro, "Las contraseñas no coinciden.");
            return;
        }
        String error = sistema.getUsuarios().registrarPrimerAdministrador(
                txtNombre.getText(), txtUsuarioNuevo.getText(), txtCorreo.getText(), clave);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensajeRegistro, error);
            return;
        }
        JOptionPane.showMessageDialog(this, "Administrador creado. Ahora inicie sesión.",
                "AylorMade", JOptionPane.INFORMATION_MESSAGE);
        txtUsuario.setText(txtUsuarioNuevo.getText());
        txtClave.setText("");
        txtNombre.setText("");
        txtUsuarioNuevo.setText("");
        txtCorreo.setText("");
        txtClaveNueva.setText("");
        txtClaveConfirmar.setText("");
        lblMensajeRegistro.setText(" ");
        lblMensajeLogin.setText(" ");
        cartas.show(panelCartas, "login");
        txtClave.requestFocusInWindow();
    }
}
