package presentacion;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import logica.SistemaSupermercado;

/**
 * Ventana "Olvide mi contraseña", en dos pasos:
 * 1) se escribe el usuario o correo y el sistema envia un codigo de 6 digitos al correo registrado;
 * 2) se escribe el codigo y la contraseña nueva.
 */
public class DlgRecuperarClave extends JDialog implements ActionListener {

    private SistemaSupermercado sistema;
    private CardLayout cartas = new CardLayout();
    private JPanel panelCartas = new JPanel(cartas);
    private String usuarioOCorreo = "";

    private JTextField txtUsuarioCorreo = new JTextField(22);
    private JButton btnEnviar = Estilo.boton("Enviar código", Estilo.AZUL);
    private JLabel lblMensaje1 = Estilo.mensaje();

    private JTextField txtCodigo = new JTextField(22);
    private JPasswordField txtClave1 = new JPasswordField(22);
    private JPasswordField txtClave2 = new JPasswordField(22);
    private JButton btnCambiar = Estilo.boton("Cambiar contraseña", Estilo.VERDE);
    private JButton btnVolver = Estilo.enlace("Volver a pedir el código");
    private JLabel lblInfo2 = Estilo.mensaje();
    private JLabel lblMensaje2 = Estilo.mensaje();

    public DlgRecuperarClave(Frame padre, SistemaSupermercado sistema) {
        super(padre, "Recuperar contraseña", true);
        this.sistema = sistema;
        setIconImage(Imagenes.imagen("logo.png"));
        setSize(480, 470);
        setLocationRelativeTo(padre);
        setResizable(false);
        panelCartas.add(crearPaso1(), "paso1");
        panelCartas.add(crearPaso2(), "paso2");
        add(panelCartas);
        cartas.show(panelCartas, "paso1");
    }

    private JPanel crearPaso1() {
        JPanel col = Estilo.columna(36);
        Estilo.estilizarCampo(txtUsuarioCorreo);
        Estilo.agregar(col, Estilo.titulo("¿Olvidó su contraseña?", 22), 0);
        Estilo.agregar(col, Estilo.etiqueta("Le enviaremos un código de 6 dígitos al correo registrado."), 6);
        Estilo.agregar(col, Estilo.etiqueta("USUARIO O CORREO"), 26);
        Estilo.agregar(col, txtUsuarioCorreo, 6);
        Estilo.agregar(col, lblMensaje1, 10);
        Estilo.agregar(col, btnEnviar, 10);
        col.add(Box.createVerticalGlue());
        txtUsuarioCorreo.addActionListener(this);
        btnEnviar.addActionListener(this);
        return col;
    }

    private JPanel crearPaso2() {
        JPanel col = Estilo.columna(36);
        JTextField[] campos = {txtCodigo, txtClave1, txtClave2};
        for (int i = 0; i < campos.length; i++) {
            Estilo.estilizarCampo(campos[i]);
        }
        Estilo.agregar(col, Estilo.titulo("Escriba el código", 22), 0);
        Estilo.agregar(col, lblInfo2, 6);
        Estilo.agregar(col, Estilo.etiqueta("CÓDIGO DE 6 DÍGITOS"), 16);
        Estilo.agregar(col, txtCodigo, 4);
        Estilo.agregar(col, Estilo.etiqueta("CONTRASEÑA NUEVA"), 10);
        Estilo.agregar(col, txtClave1, 4);
        Estilo.agregar(col, Estilo.etiqueta("CONFIRMAR CONTRASEÑA"), 10);
        Estilo.agregar(col, txtClave2, 4);
        Estilo.agregar(col, lblMensaje2, 8);
        Estilo.agregar(col, btnCambiar, 8);
        Estilo.agregar(col, btnVolver, 4);
        col.add(Box.createVerticalGlue());
        txtClave2.addActionListener(this);
        btnCambiar.addActionListener(this);
        btnVolver.addActionListener(this);
        return col;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == btnEnviar || origen == txtUsuarioCorreo) {
            enviarCodigo();
        } else if (origen == btnCambiar || origen == txtClave2) {
            cambiarClave();
        } else if (origen == btnVolver) {
            lblMensaje1.setText(" ");
            cartas.show(panelCartas, "paso1");
        }
    }

    private void enviarCodigo() {
        // Enviar el correo puede tardar unos segundos: se avisa y se muestra el cursor de espera.
        lblMensaje1.setForeground(Estilo.GRIS);
        lblMensaje1.setText("Enviando código...");
        lblMensaje1.paintImmediately(lblMensaje1.getBounds());
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String error = sistema.getUsuarios().solicitarCodigoRecuperacion(txtUsuarioCorreo.getText());
        setCursor(Cursor.getDefaultCursor());
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje1, error);
            return;
        }
        usuarioOCorreo = txtUsuarioCorreo.getText();
        lblInfo2.setForeground(Estilo.GRIS);
        lblInfo2.setText("<html>Si el usuario existe, enviamos un código a su correo.<br>Revise también la carpeta de spam. Vence en 15 minutos.</html>");
        lblMensaje2.setText(" ");
        txtCodigo.setText("");
        txtClave1.setText("");
        txtClave2.setText("");
        cartas.show(panelCartas, "paso2");
        txtCodigo.requestFocusInWindow();
    }

    private void cambiarClave() {
        String clave1 = new String(txtClave1.getPassword());
        String clave2 = new String(txtClave2.getPassword());
        if (!clave1.equals(clave2)) {
            Estilo.mensajeError(lblMensaje2, "Las contraseñas no coinciden.");
            return;
        }
        String error = sistema.getUsuarios().restablecerClave(usuarioOCorreo, txtCodigo.getText(), clave1);
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje2, error);
            return;
        }
        JOptionPane.showMessageDialog(this, "Su contraseña se cambió correctamente.\nYa puede iniciar sesión.",
                "AylorMade", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
