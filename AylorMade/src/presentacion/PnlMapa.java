package presentacion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import logica.SistemaSupermercado;
import modelo.Sucursal;
import modelo.Usuario;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

/**
 * Mapa de sucursales (libreria JXMapViewer2 con mapas de OpenStreetMap; necesita internet).
 * Todos los usuarios pueden verlo. El administrador, ademas, puede agregar sucursales
 * haciendo clic en el mapa para elegir el lugar, y eliminarlas.
 */
public class PnlMapa extends JPanel implements ActionListener, ListSelectionListener {

    private SistemaSupermercado sistema;
    private Usuario usuario;
    private boolean puedeEditar;

    private JXMapViewer mapa = new JXMapViewer();
    private WaypointPainter<WaypointSucursal> pintor = new WaypointPainter<>();
    private WaypointSucursal puntoNuevo = null;

    private DefaultTableModel modeloTabla = new DefaultTableModel(new String[]{"Sucursal", "Tipo", "Cajas", "Dirección"}, 0);
    private JTable tabla = new JTable(modeloTabla);

    private JTextField txtNombre = new JTextField(14);
    private JTextField txtDireccion = new JTextField(14);
    private JTextField txtLatitud = new JTextField(8);
    private JTextField txtLongitud = new JTextField(8);
    private JSpinner spnCajas = new JSpinner(new SpinnerNumberModel(2, 1, 99, 1));
    private JButton btnAgregar = Estilo.boton("Agregar sucursal", Estilo.VERDE);
    private JButton btnEliminar = Estilo.boton("Eliminar", Estilo.ROJO);
    private JLabel lblMensaje = Estilo.mensaje();

    public PnlMapa(SistemaSupermercado sistema, Usuario usuario) {
        this.sistema = sistema;
        this.usuario = usuario;
        this.puedeEditar = usuario.puedeGestionarUsuarios();
        setLayout(new BorderLayout(14, 0));
        setBackground(Estilo.FONDO);
        setBorder(new EmptyBorder(16, 20, 18, 20));

        configurarMapa();
        add(crearPanelIzquierdo(), BorderLayout.WEST);
        add(crearPanelMapa(), BorderLayout.CENTER);

        btnAgregar.addActionListener(this);
        btnEliminar.addActionListener(this);
        tabla.getSelectionModel().addListSelectionListener(this);

        mostrarSucursales();
        verTodasLasSucursales();
    }

    // ----- Construccion -----

    private void configurarMapa() {
        // OpenStreetMap pide que cada programa se identifique al descargar los mapas.
        System.setProperty("http.agent", "AylorMade/1.0 (proyecto universitario UTN)");
        // Se usa HTTPS: OpenStreetMap ya no entrega los mapas por HTTP simple.
        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
        DefaultTileFactory fabrica = new DefaultTileFactory(info);
        fabrica.setThreadPoolSize(4);
        mapa.setTileFactory(fabrica);

        PanMouseInputListener arrastrar = new PanMouseInputListener(mapa); // mover el mapa arrastrando
        mapa.addMouseListener(arrastrar);
        mapa.addMouseMotionListener(arrastrar);
        mapa.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapa)); // acercar con la rueda
        mapa.addMouseListener(new EscuchaMapa(this, mapa)); // clic para elegir un punto

        pintor.setRenderer(new RenderizadorSucursal());
        mapa.setOverlayPainter(pintor);
    }

    private JPanel crearPanelIzquierdo() {
        JPanel izquierdo = new JPanel(new BorderLayout(0, 12));
        izquierdo.setOpaque(false);
        izquierdo.setPreferredSize(new Dimension(520, 100));

        JPanel lista = Estilo.tarjeta();
        lista.setLayout(new BorderLayout(0, 10));
        lista.add(Estilo.titulo("Sucursales", 18), BorderLayout.NORTH);
        Estilo.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(170);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        lista.add(scroll, BorderLayout.CENTER);
        JButton btnTodas = Estilo.boton("Ver todas", Estilo.AZUL_CLARO);
        btnTodas.addActionListener(this);
        btnTodas.setActionCommand("todas");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnTodas);
        if (puedeEditar) {
            botones.add(btnEliminar);
        }
        lista.add(botones, BorderLayout.SOUTH);
        izquierdo.add(lista, BorderLayout.CENTER);

        if (puedeEditar) {
            izquierdo.add(crearFormulario(), BorderLayout.SOUTH);
        }
        return izquierdo;
    }

    private JPanel crearFormulario() {
        JPanel form = Estilo.tarjeta();
        form.setLayout(new GridBagLayout());
        JTextField[] campos = {txtNombre, txtDireccion, txtLatitud, txtLongitud};
        for (int i = 0; i < campos.length; i++) {
            Estilo.estilizarCampo(campos[i]);
        }
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 4, 10);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(Estilo.titulo("Nueva sucursal", 16), c);
        c.gridy = 1;
        form.add(Estilo.etiqueta("Haga clic en el mapa para elegir el lugar."), c);
        c.gridwidth = 1;
        campo(form, c, 2, "NOMBRE", txtNombre, "CAJAS", spnCajas);
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 4;
        form.add(Estilo.etiqueta("DIRECCIÓN"), c);
        c.gridy = 5;
        form.add(txtDireccion, c);
        c.gridwidth = 1;
        campo(form, c, 6, "LATITUD", txtLatitud, "LONGITUD", txtLongitud);
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.insets = new Insets(8, 0, 0, 0);
        form.add(btnAgregar, c);
        c.gridy = 9;
        c.insets = new Insets(6, 0, 0, 0);
        lblMensaje.setPreferredSize(new Dimension(10, 44));
        form.add(lblMensaje, c);
        return form;
    }

    // Agrega dos campos con su etiqueta en una misma fila del formulario.
    private void campo(JPanel form, GridBagConstraints c, int fila, String etiqueta1, JComponent campo1,
            String etiqueta2, JComponent campo2) {
        c.gridy = fila;
        c.gridx = 0;
        c.weightx = 2;
        form.add(Estilo.etiqueta(etiqueta1), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(Estilo.etiqueta(etiqueta2), c);
        c.gridy = fila + 1;
        c.gridx = 0;
        c.weightx = 2;
        form.add(campo1, c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(campo2, c);
        c.weightx = 0;
    }

    private JPanel crearPanelMapa() {
        JPanel p = Estilo.tarjeta();
        p.setLayout(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), new EmptyBorder(8, 8, 8, 8)));
        p.add(mapa, BorderLayout.CENTER);
        JLabel ayuda = Estilo.etiqueta("Arrastre para mover · Rueda del mouse para acercar · Datos del mapa: © OpenStreetMap contributors");
        p.add(ayuda, BorderLayout.SOUTH);
        return p;
    }

    // ----- Datos -----

    private void mostrarSucursales() {
        modeloTabla.setRowCount(0);
        ArrayList<Sucursal> lista = sistema.getSucursales().listar();
        for (int i = 0; i < lista.size(); i++) {
            Sucursal s = lista.get(i);
            modeloTabla.addRow(new Object[]{s.getNombre(), s.getTipo(), s.getNumeroCajas(), s.getDireccion()});
        }
        actualizarMarcadores();
    }

    private void actualizarMarcadores() {
        Set<WaypointSucursal> marcadores = new HashSet<>();
        ArrayList<Sucursal> lista = sistema.getSucursales().listar();
        for (int i = 0; i < lista.size(); i++) {
            marcadores.add(new WaypointSucursal(lista.get(i).getNombre(), lista.get(i).getLatitud(),
                    lista.get(i).getLongitud(), false));
        }
        if (puntoNuevo != null) {
            marcadores.add(puntoNuevo);
        }
        pintor.setWaypoints(marcadores);
        mapa.repaint();
    }

    // Ajusta el mapa para que se vean todas las sucursales (o Costa Rica si todavia no hay ninguna).
    // El zoom se calcula a partir de lo separadas que estan las sucursales, sin depender del tamaño de la ventana.
    private void verTodasLasSucursales() {
        ArrayList<Sucursal> lista = sistema.getSucursales().listar();
        if (lista.isEmpty()) {
            mapa.setAddressLocation(new GeoPosition(9.9281, -84.0907));
            mapa.setZoom(11);
            return;
        }
        double latMin = lista.get(0).getLatitud();
        double latMax = latMin;
        double lonMin = lista.get(0).getLongitud();
        double lonMax = lonMin;
        for (int i = 1; i < lista.size(); i++) {
            latMin = Math.min(latMin, lista.get(i).getLatitud());
            latMax = Math.max(latMax, lista.get(i).getLatitud());
            lonMin = Math.min(lonMin, lista.get(i).getLongitud());
            lonMax = Math.max(lonMax, lista.get(i).getLongitud());
        }
        mapa.setAddressLocation(new GeoPosition((latMin + latMax) / 2, (lonMin + lonMax) / 2));
        double separacion = Math.max(latMax - latMin, lonMax - lonMin);
        if (separacion < 0.002) {
            mapa.setZoom(4); // una sola sucursal (o varias en el mismo lugar): se acerca bastante
            return;
        }
        // Nivel de zoom de OpenStreetMap (0 = mundo, 19 = calle) en el que cabe esa separacion en unos 600 px.
        int nivelOsm = (int) Math.floor(Math.log(360.0 * 600 / (256 * separacion * 1.5)) / Math.log(2));
        nivelOsm = Math.max(2, Math.min(17, nivelOsm));
        mapa.setZoom(19 - nivelOsm); // en JXMapViewer2 el zoom 0 es el mas cercano
    }

    // Lo llama EscuchaMapa cuando el administrador hace clic en el mapa.
    public void alElegirPunto(double latitud, double longitud) {
        if (!puedeEditar) {
            return;
        }
        txtLatitud.setText(String.format(Locale.US, "%.5f", latitud));
        txtLongitud.setText(String.format(Locale.US, "%.5f", longitud));
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            nombre = "Nueva sucursal";
        }
        puntoNuevo = new WaypointSucursal(nombre, latitud, longitud, true);
        actualizarMarcadores();
        Estilo.mensajeExito(lblMensaje, "Lugar elegido. Complete el nombre y la dirección.");
    }

    // ----- Eventos -----

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || tabla.getSelectedRow() < 0) {
            return;
        }
        String nombre = (String) modeloTabla.getValueAt(tabla.convertRowIndexToModel(tabla.getSelectedRow()), 0);
        Sucursal s = sistema.getSucursales().buscar(nombre);
        if (s != null) {
            mapa.setAddressLocation(new GeoPosition(s.getLatitud(), s.getLongitud()));
            mapa.setZoom(3);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if ("todas".equals(e.getActionCommand())) {
            tabla.clearSelection();
            verTodasLasSucursales();
        } else if (origen == btnAgregar) {
            agregarSucursal();
        } else if (origen == btnEliminar) {
            eliminarSucursal();
        }
    }

    private double leerNumero(JTextField campo) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private void agregarSucursal() {
        double latitud = leerNumero(txtLatitud);
        double longitud = leerNumero(txtLongitud);
        if (Double.isNaN(latitud) || Double.isNaN(longitud)) {
            Estilo.mensajeError(lblMensaje, "Elija el lugar en el mapa, o escriba la latitud y la longitud como números.");
            return;
        }
        String error = sistema.getSucursales().agregar(usuario, txtNombre.getText(), txtDireccion.getText(),
                latitud, longitud, (Integer) spnCajas.getValue());
        if (!error.isEmpty()) {
            Estilo.mensajeError(lblMensaje, error);
            return;
        }
        mapa.setAddressLocation(new GeoPosition(latitud, longitud));
        mapa.setZoom(4);
        txtNombre.setText("");
        txtDireccion.setText("");
        txtLatitud.setText("");
        txtLongitud.setText("");
        puntoNuevo = null;
        mostrarSucursales();
        Estilo.mensajeExito(lblMensaje, "Sucursal agregada.");
    }

    private void eliminarSucursal() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Elija una sucursal de la tabla.", "AylorMade", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = (String) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar la sucursal \"" + nombre + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        String error = sistema.getSucursales().eliminar(usuario, nombre);
        if (!error.isEmpty()) {
            JOptionPane.showMessageDialog(this, error, "AylorMade", JOptionPane.WARNING_MESSAGE);
            return;
        }
        mostrarSucursales();
    }
}
