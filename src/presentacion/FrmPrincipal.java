package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import logica.ServicioReportes;
import logica.SistemaSupermercado;
import modelo.Producto;
import modelo.Usuario;

/**
 * Menu principal / Dashboard. Muestra los indicadores del dia, las alertas de inventario
 * y los botones para abrir las demas pantallas. Cada usuario ve solo lo que su rol permite.
 */
public class FrmPrincipal extends JFrame implements ActionListener {

    private SistemaSupermercado sistema;
    private Usuario usuario;

    private JButton btnCaja = botonMenu("Caja de cobro", "ic_caja.png");
    private JButton btnProductos = botonMenu("Productos", "ic_productos.png");
    private JButton btnMapa = botonMenu("Mapa y reportes", "ic_mapa.png");
    private JButton btnUsuarios = botonMenu("Usuarios", "ic_usuarios.png");
    private JButton btnClave = botonMenu("Mi contraseña", "ic_llave.png");
    private JButton btnSalir = botonMenu("Cerrar sesión", "ic_salir.png");
    private JButton btnActualizar = Estilo.boton("Actualizar", Estilo.AZUL_CLARO);

    private JLabel lblKpi1 = new JLabel("-");
    private JLabel lblKpi2 = new JLabel("-");
    private JLabel lblKpi3 = new JLabel("-");
    private JLabel lblKpi4 = new JLabel("-");

    private JLabel lblTituloBajo = new JLabel("Stock bajo");
    private JLabel lblTituloPorVencer = new JLabel("Por vencer");
    private JLabel lblTituloVencidos = new JLabel("Vencidos");
    private JTextArea txtBajo = new JTextArea();
    private JTextArea txtPorVencer = new JTextArea();
    private JTextArea txtVencidos = new JTextArea();

    public FrmPrincipal(SistemaSupermercado sistema, Usuario usuario) {
        this.sistema = sistema;
        this.usuario = usuario;
        setTitle("AylorMade - Panel de control");
        setIconImage(Imagenes.imagen("logo.png"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 700);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(crearMenuLateral(), BorderLayout.WEST);
        add(crearPanelCentral(), BorderLayout.CENTER);

        btnCaja.addActionListener(this);
        btnProductos.addActionListener(this);
        btnMapa.addActionListener(this);
        btnUsuarios.addActionListener(this);
        btnClave.addActionListener(this);
        btnSalir.addActionListener(this);
        btnActualizar.addActionListener(this);

        actualizar();
    }

    // ----- Construccion de la pantalla -----

    private static JButton botonMenu(String texto, String icono) {
        JButton b = new JButton(texto, Imagenes.icono(icono, 24));
        b.setFont(Estilo.NEGRITA);
        b.setForeground(Color.WHITE);
        b.setBackground(Estilo.AZUL);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new EmptyBorder(12, 26, 12, 20));
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIconTextGap(14);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private JPanel crearMenuLateral() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Estilo.AZUL);
        p.setPreferredSize(new Dimension(250, 700));

        JLabel logo = new JLabel(" AylorMade", Imagenes.icono("logo.png", 46), SwingConstants.LEFT);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setIconTextGap(10);
        logo.setBorder(new EmptyBorder(26, 20, 26, 10));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(logo);

        p.add(btnCaja);
        p.add(btnProductos);
        p.add(btnMapa);
        if (usuario.puedeGestionarUsuarios()) {
            p.add(btnUsuarios);
        }
        p.add(Box.createVerticalGlue());

        JLabel nombre = new JLabel(usuario.getNombre());
        nombre.setFont(Estilo.NEGRITA);
        nombre.setForeground(Color.WHITE);
        nombre.setBorder(new EmptyBorder(0, 26, 0, 10));
        nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        String detalle = usuario.getRol();
        if (usuario.getNumeroCaja() > 0) {
            detalle = detalle + " · Caja " + usuario.getNumeroCaja();
        }
        JLabel rol = new JLabel(detalle);
        rol.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rol.setForeground(new Color(0xA9BCD6));
        rol.setBorder(new EmptyBorder(2, 26, 12, 10));
        rol.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(nombre);
        p.add(rol);
        p.add(btnClave);
        p.add(btnSalir);
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        return p;
    }

    private JPanel crearPanelCentral() {
        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setBackground(Estilo.FONDO);
        centro.setBorder(new EmptyBorder(26, 30, 26, 30));

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Estilo.titulo("Hola, " + primerNombre(usuario.getNombre()), 28);
        JLabel fecha = new JLabel(fechaDeHoy());
        fecha.setFont(Estilo.FUENTE);
        fecha.setForeground(Estilo.GRIS);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        fecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(fecha);
        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(btnActualizar, BorderLayout.EAST);
        centro.add(encabezado, BorderLayout.NORTH);

        // Indicadores + alertas
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel indicadores;
        if (usuario.puedeVerReportes()) {
            indicadores = new JPanel(new GridLayout(1, 4, 16, 0));
            indicadores.add(tarjetaIndicador("Recaudado hoy", lblKpi1, Estilo.VERDE));
            indicadores.add(tarjetaIndicador("Ventas hoy", lblKpi2, Estilo.AZUL_CLARO));
            indicadores.add(tarjetaIndicador("Unidades vendidas hoy", lblKpi3, Estilo.NARANJA));
            indicadores.add(tarjetaIndicador("Entradas hoy (unidades)", lblKpi4, Estilo.GRIS));
        } else {
            indicadores = new JPanel(new GridLayout(1, 2, 16, 0));
            indicadores.add(tarjetaIndicador("Mi total vendido hoy", lblKpi1, Estilo.VERDE));
            indicadores.add(tarjetaIndicador("Productos en inventario", lblKpi2, Estilo.AZUL_CLARO));
        }
        indicadores.setOpaque(false);
        indicadores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        indicadores.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(indicadores);
        contenido.add(Box.createRigidArea(new Dimension(0, 22)));

        JLabel tituloAlertas = Estilo.titulo("Alertas de inventario", 18);
        tituloAlertas.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(tituloAlertas);
        contenido.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel alertas = new JPanel(new GridLayout(1, 3, 16, 0));
        alertas.setOpaque(false);
        alertas.add(tarjetaAlerta(lblTituloBajo, txtBajo, Estilo.AMARILLO));
        alertas.add(tarjetaAlerta(lblTituloPorVencer, txtPorVencer, new Color(0xD35400)));
        alertas.add(tarjetaAlerta(lblTituloVencidos, txtVencidos, Estilo.ROJO));
        alertas.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(alertas);

        centro.add(contenido, BorderLayout.CENTER);
        return centro;
    }

    private JPanel tarjetaIndicador(String titulo, JLabel valor, Color acento) {
        JPanel t = Estilo.tarjeta();
        t.setLayout(new BorderLayout(0, 6));
        JLabel lblTitulo = Estilo.etiqueta(titulo.toUpperCase());
        valor.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valor.setForeground(acento);
        t.add(lblTitulo, BorderLayout.NORTH);
        t.add(valor, BorderLayout.CENTER);
        return t;
    }

    private JPanel tarjetaAlerta(JLabel titulo, JTextArea area, Color color) {
        JPanel t = Estilo.tarjeta();
        t.setLayout(new BorderLayout(0, 8));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(color);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(Estilo.FUENTE);
        area.setForeground(new Color(0x33414F));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        t.add(titulo, BorderLayout.NORTH);
        t.add(scroll, BorderLayout.CENTER);
        return t;
    }

    private String primerNombre(String nombreCompleto) {
        int espacio = nombreCompleto.indexOf(' ');
        if (espacio > 0) {
            return nombreCompleto.substring(0, espacio);
        }
        return nombreCompleto;
    }

    private String fechaDeHoy() {
        String texto = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CR")));
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    // ----- Datos que muestra el panel -----

    public void actualizar() {
        ServicioReportes reportes = sistema.getReportes();
        String hoy = reportes.hoy();
        if (usuario.puedeVerReportes()) {
            lblKpi1.setText(Estilo.dinero(reportes.totalRecaudado(hoy)));
            lblKpi2.setText(String.valueOf(reportes.cantidadVentas(hoy)));
            lblKpi3.setText(String.valueOf(reportes.unidadesVendidas(hoy)));
            lblKpi4.setText(String.valueOf(reportes.unidadesIngresadas(hoy)));
        } else {
            lblKpi1.setText(Estilo.dinero(reportes.totalPorCajero(hoy, usuario.getNombreUsuario())));
            lblKpi2.setText(String.valueOf(sistema.getProductos().listar().size()));
        }

        ArrayList<Producto> bajos = sistema.getProductos().productosBajoStock();
        ArrayList<Producto> porVencer = sistema.getProductos().productosPorVencer();
        ArrayList<Producto> vencidos = sistema.getProductos().productosVencidos();
        lblTituloBajo.setText("Stock bajo (" + bajos.size() + ")");
        lblTituloPorVencer.setText("Por vencer (" + porVencer.size() + ")");
        lblTituloVencidos.setText("Vencidos (" + vencidos.size() + ")");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bajos.size(); i++) {
            sb.append("• ").append(bajos.get(i).getNombre()).append("\n   quedan ").append(bajos.get(i).getStock())
                    .append(" (mínimo ").append(bajos.get(i).getStockMinimo()).append(")\n");
        }
        txtBajo.setText(textoAlerta(sb));

        sb = new StringBuilder();
        for (int i = 0; i < porVencer.size(); i++) {
            sb.append("• ").append(porVencer.get(i).getNombre()).append("\n   vence ")
                    .append(porVencer.get(i).getFechaVencimientoTexto()).append("\n");
        }
        txtPorVencer.setText(textoAlerta(sb));

        sb = new StringBuilder();
        for (int i = 0; i < vencidos.size(); i++) {
            sb.append("• ").append(vencidos.get(i).getNombre()).append("\n   venció ")
                    .append(vencidos.get(i).getFechaVencimientoTexto()).append("\n");
        }
        txtVencidos.setText(textoAlerta(sb));
        txtBajo.setCaretPosition(0);
        txtPorVencer.setCaretPosition(0);
        txtVencidos.setCaretPosition(0);
    }

    private String textoAlerta(StringBuilder sb) {
        if (sb.length() == 0) {
            return "Todo en orden.";
        }
        return sb.toString();
    }

    // ----- Eventos -----

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == btnCaja) {
            new DlgCaja(this, sistema, usuario).setVisible(true);
            actualizar();
        } else if (origen == btnProductos) {
            new DlgProductos(this, sistema, usuario).setVisible(true);
            actualizar();
        } else if (origen == btnMapa) {
            new DlgMapaReportes(this, sistema, usuario).setVisible(true);
            actualizar();
        } else if (origen == btnUsuarios) {
            new DlgUsuarios(this, sistema, usuario).setVisible(true);
        } else if (origen == btnClave) {
            cambiarMiClave();
        } else if (origen == btnActualizar) {
            actualizar();
        } else if (origen == btnSalir) {
            int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?",
                    "AylorMade", JOptionPane.YES_NO_OPTION);
            if (respuesta == JOptionPane.YES_OPTION) {
                dispose();
                new FrmLogin(sistema).setVisible(true);
            }
        }
    }

    private void cambiarMiClave() {
        JPasswordField txtActual = new JPasswordField(18);
        JPasswordField txtNueva = new JPasswordField(18);
        JPasswordField txtConfirmar = new JPasswordField(18);
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel("Contraseña actual:"));
        panel.add(txtActual);
        panel.add(new JLabel("Contraseña nueva (mínimo 8 caracteres, letras y números):"));
        panel.add(txtNueva);
        panel.add(new JLabel("Confirmar contraseña nueva:"));
        panel.add(txtConfirmar);
        int respuesta = JOptionPane.showConfirmDialog(this, panel, "Cambiar mi contraseña",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        String nueva = new String(txtNueva.getPassword());
        if (!nueva.equals(new String(txtConfirmar.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Las contraseñas nuevas no coinciden.", "AylorMade", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String error = sistema.getUsuarios().cambiarClave(usuario, new String(txtActual.getPassword()), nueva);
        if (error.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contraseña cambiada correctamente.", "AylorMade", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, error, "AylorMade", JOptionPane.WARNING_MESSAGE);
        }
    }
}
