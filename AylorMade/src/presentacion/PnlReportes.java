package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import logica.ServicioReportes;
import logica.SistemaSupermercado;
import modelo.EntradaInventario;
import modelo.RegistroVenta;
import modelo.Usuario;

/**
 * Reportes de un dia: dinero recaudado, ventas, unidades vendidas, entradas de mercaderia,
 * y el detalle por venta, por producto, por cajero y por entrada. Solo para quien tiene permiso.
 */
public class PnlReportes extends JPanel implements ActionListener {

    private SistemaSupermercado sistema;

    private JTextField txtFecha = new JTextField(9);
    private JButton btnAnterior = Estilo.boton("Día anterior", Estilo.GRIS);
    private JButton btnHoy = Estilo.boton("Hoy", Estilo.AZUL_CLARO);
    private JButton btnSiguiente = Estilo.boton("Día siguiente", Estilo.GRIS);
    private JButton btnConsultar = Estilo.boton("Consultar", Estilo.AZUL);
    private JLabel lblMensaje = new JLabel(" ");

    private JLabel lblRecaudado = new JLabel("-");
    private JLabel lblVentas = new JLabel("-");
    private JLabel lblUnidades = new JLabel("-");
    private JLabel lblEntradas = new JLabel("-");
    private JLabel lblEfectivo = new JLabel("-");
    private JLabel lblTarjeta = new JLabel("-");
    private JLabel lblSinpe = new JLabel("-");
    private JLabel lblHistorico = new JLabel("-");

    private DefaultTableModel modeloVentas = new DefaultTableModel(new String[]{"N.º", "Hora", "Cajero", "Método de pago", "Total", "Vuelto"}, 0);
    private DefaultTableModel modeloProductos = new DefaultTableModel(new String[]{"Código", "Producto", "Unidades", "Total vendido"}, 0);
    private DefaultTableModel modeloCajeros = new DefaultTableModel(new String[]{"Usuario", "Nombre", "Rol", "Ventas", "Total vendido"}, 0);
    private DefaultTableModel modeloEntradas = new DefaultTableModel(new String[]{"Hora", "Código", "Producto", "Cantidad", "Recibió", "Proveedor"}, 0);

    public PnlReportes(SistemaSupermercado sistema, Usuario usuario) {
        this.sistema = sistema;
        setLayout(new BorderLayout(0, 14));
        setBackground(Estilo.FONDO);
        setBorder(new EmptyBorder(16, 20, 18, 20));

        add(crearBarraFecha(), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(crearIndicadores(), BorderLayout.NORTH);
        centro.add(crearPestanas(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        btnAnterior.addActionListener(this);
        btnHoy.addActionListener(this);
        btnSiguiente.addActionListener(this);
        btnConsultar.addActionListener(this);
        txtFecha.addActionListener(this);

        txtFecha.setText(sistema.getReportes().hoy());
        consultar();
    }

    // ----- Construccion -----

    private JPanel crearBarraFecha() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);
        izquierda.add(Estilo.titulo("Reportes del día", 22));
        izquierda.add(new JLabel("   "));
        izquierda.add(Estilo.etiqueta("FECHA (AAAA-MM-DD)"));
        Estilo.estilizarCampo(txtFecha);
        izquierda.add(txtFecha);
        izquierda.add(btnConsultar);
        izquierda.add(btnAnterior);
        izquierda.add(btnHoy);
        izquierda.add(btnSiguiente);
        barra.add(izquierda, BorderLayout.WEST);
        lblMensaje.setFont(Estilo.NEGRITA);
        barra.add(lblMensaje, BorderLayout.SOUTH);
        return barra;
    }

    private JPanel crearIndicadores() {
        JPanel filas = new JPanel(new GridLayout(2, 1, 0, 12));
        filas.setOpaque(false);
        JPanel arriba = new JPanel(new GridLayout(1, 4, 14, 0));
        arriba.setOpaque(false);
        arriba.add(tarjeta("Dinero recaudado", lblRecaudado, Estilo.VERDE, 28));
        arriba.add(tarjeta("Ventas realizadas", lblVentas, Estilo.AZUL_CLARO, 28));
        arriba.add(tarjeta("Unidades vendidas", lblUnidades, Estilo.NARANJA, 28));
        arriba.add(tarjeta("Unidades que entraron", lblEntradas, Estilo.GRIS, 28));
        JPanel abajo = new JPanel(new GridLayout(1, 4, 14, 0));
        abajo.setOpaque(false);
        abajo.add(tarjeta("En efectivo", lblEfectivo, Estilo.AZUL, 20));
        abajo.add(tarjeta("Con tarjeta", lblTarjeta, Estilo.AZUL, 20));
        abajo.add(tarjeta("Con SINPE Móvil", lblSinpe, Estilo.AZUL, 20));
        abajo.add(tarjeta("Recaudado en total (histórico)", lblHistorico, Estilo.GRIS, 20));
        filas.add(arriba);
        filas.add(abajo);
        filas.setPreferredSize(new Dimension(10, 170));
        return filas;
    }

    private JPanel tarjeta(String titulo, JLabel valor, Color color, int tamano) {
        JPanel t = Estilo.tarjeta();
        t.setLayout(new BorderLayout(0, 2));
        valor.setFont(new Font("Segoe UI", Font.BOLD, tamano));
        valor.setForeground(color);
        t.add(Estilo.etiqueta(titulo.toUpperCase()), BorderLayout.NORTH);
        t.add(valor, BorderLayout.CENTER);
        return t;
    }

    private JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilo.NEGRITA);
        pestanas.addTab("Ventas del día", crearTabla(modeloVentas));
        pestanas.addTab("Productos vendidos", crearTabla(modeloProductos));
        pestanas.addTab("Por cajero", crearTabla(modeloCajeros));
        pestanas.addTab("Entradas de mercadería", crearTabla(modeloEntradas));
        return pestanas;
    }

    private JScrollPane crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        Estilo.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ----- Datos -----

    private void consultar() {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim());
        } catch (Exception e) {
            Estilo.mensajeError(lblMensaje, "La fecha debe tener el formato AAAA-MM-DD (por ejemplo 2026-12-01).");
            return;
        }
        lblMensaje.setText(" ");
        String f = fecha.toString();
        ServicioReportes r = sistema.getReportes();

        lblRecaudado.setText(Estilo.dinero(r.totalRecaudado(f)));
        lblVentas.setText(String.valueOf(r.cantidadVentas(f)));
        lblUnidades.setText(String.valueOf(r.unidadesVendidas(f)));
        lblEntradas.setText(String.valueOf(r.unidadesIngresadas(f)));
        lblEfectivo.setText(Estilo.dinero(r.totalPorMetodo(f, "Efectivo")));
        lblTarjeta.setText(Estilo.dinero(r.totalPorMetodo(f, "Tarjeta")));
        lblSinpe.setText(Estilo.dinero(r.totalPorMetodo(f, "SINPE Movil")));
        lblHistorico.setText(Estilo.dinero(r.totalRecaudadoHistorico()));

        ArrayList<RegistroVenta> ventas = r.ventasDelDia(f);
        modeloVentas.setRowCount(0);
        for (int i = 0; i < ventas.size(); i++) {
            RegistroVenta v = ventas.get(i);
            String hora = v.getFecha();
            if (hora.length() >= 19) {
                hora = hora.substring(11, 19);
            }
            modeloVentas.addRow(new Object[]{v.getNumero(), hora, v.getUsuarioCajero(), v.getMetodoPago(),
                Estilo.dinero(v.getTotal()), Estilo.dinero(v.getVuelto())});
        }

        modeloProductos.setRowCount(0);
        ArrayList<String[]> productos = r.productosVendidos(f, sistema.getProductos());
        for (int i = 0; i < productos.size(); i++) {
            modeloProductos.addRow(new Object[]{productos.get(i)[0], productos.get(i)[1], productos.get(i)[2],
                Estilo.dinero(Double.parseDouble(productos.get(i)[3]))});
        }

        modeloCajeros.setRowCount(0);
        ArrayList<Usuario> usuarios = sistema.getUsuarios().listar();
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            int cantidad = 0;
            for (int j = 0; j < ventas.size(); j++) {
                if (ventas.get(j).getUsuarioCajero().equalsIgnoreCase(u.getNombreUsuario())) {
                    cantidad++;
                }
            }
            modeloCajeros.addRow(new Object[]{u.getNombreUsuario(), u.getNombre(), u.getRol(), cantidad,
                Estilo.dinero(r.totalPorCajero(f, u.getNombreUsuario()))});
        }

        modeloEntradas.setRowCount(0);
        ArrayList<EntradaInventario> entradas = r.entradasDelDia(f);
        for (int i = 0; i < entradas.size(); i++) {
            EntradaInventario en = entradas.get(i);
            String hora = en.getFecha();
            if (hora.length() >= 19) {
                hora = hora.substring(11, 19);
            }
            modeloEntradas.addRow(new Object[]{hora, en.getCodigoProducto(), en.getNombreProducto(),
                en.getCantidad(), en.getUsuario(), en.getProveedor()});
        }
    }

    // ----- Eventos -----

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origen = e.getSource();
        if (origen == btnHoy) {
            txtFecha.setText(sistema.getReportes().hoy());
            consultar();
        } else if (origen == btnAnterior || origen == btnSiguiente) {
            try {
                LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
                if (origen == btnAnterior) {
                    fecha = fecha.minusDays(1);
                } else {
                    fecha = fecha.plusDays(1);
                }
                txtFecha.setText(fecha.toString());
            } catch (Exception ex) {
                Estilo.mensajeError(lblMensaje, "La fecha debe tener el formato AAAA-MM-DD (por ejemplo 2026-12-01).");
                return;
            }
            consultar();
        } else {
            consultar();
        }
    }
}
