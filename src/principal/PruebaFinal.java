package principal;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import logica.SistemaSupermercado;
import modelo.Efectivo;
import modelo.Producto;
import modelo.Sinpe;
import modelo.Usuario;
import modelo.Venta;

/**
 * VERIFICACION FINAL: recorre todo el sistema (seguridad, usuarios, inventario, ventas, reportes,
 * sucursales, factura PDF, correo y recuperacion de contrasena) y muestra [OK] o [FALLO] en cada paso.
 * Se ejecuta antes de entregar el proyecto. Usa la carpeta datos_prueba_final (no toca los datos reales)
 * y un correo falso (no envia nada). Debe correrse desde la carpeta raiz del proyecto.
 */
public class PruebaFinal {

    private static int correctas = 0;
    private static int fallidas = 0;

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            correctas++;
            System.out.println("[OK]    " + descripcion);
        } else {
            fallidas++;
            System.out.println("[FALLO] " + descripcion);
        }
    }

    public static void main(String[] args) throws Exception {
        String carpeta = "datos_prueba_final";
        String[] archivos = {"usuarios", "productos", "ventas", "entradas", "sucursales"};
        for (int i = 0; i < archivos.length; i++) {
            Files.deleteIfExists(Paths.get(carpeta + "/" + archivos[i] + ".txt"));
        }
        File[] viejas = new File(carpeta + "/facturas").listFiles();
        if (viejas != null) {
            for (int i = 0; i < viejas.length; i++) {
                viejas[i].delete();
            }
        }

        CorreoDePrueba correo = new CorreoDePrueba();
        SistemaSupermercado sistema = new SistemaSupermercado(carpeta, correo);

        System.out.println("--- SEGURIDAD Y USUARIOS ---");
        verificar("El sistema arranca sin usuarios", !sistema.getUsuarios().hayUsuarios());
        verificar("Rechaza una contraseña débil", !sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "1234").isEmpty());
        verificar("Crea el primer administrador", sistema.getUsuarios().registrarPrimerAdministrador("Ana Mora", "amora", "ana@correo.com", "Admin123*").isEmpty());
        verificar("No permite un segundo 'primer administrador'", !sistema.getUsuarios().registrarPrimerAdministrador("X", "xxx", "x@correo.com", "Clave12345").isEmpty());
        String lineaUsuario = Files.readAllLines(Paths.get(carpeta + "/usuarios.txt"), StandardCharsets.UTF_8).get(0);
        verificar("La contraseña se guarda encriptada (hash BCrypt), no en texto", !lineaUsuario.contains("Admin123*") && lineaUsuario.contains("$2a$"));
        Usuario admin = sistema.getUsuarios().iniciarSesion("amora", "Admin123*");
        verificar("Inicia sesión con la contraseña correcta", admin != null && admin.getRol().equals("Administrador"));
        verificar("Rechaza la contraseña incorrecta", sistema.getUsuarios().iniciarSesion("amora", "otraClave1") == null);
        verificar("El administrador crea un cajero", sistema.getUsuarios().registrar(admin, "Cajero", "Luis Rojas", "lrojas", "luis@correo.com", "Caja12345", 1).isEmpty());
        Usuario cajero = sistema.getUsuarios().iniciarSesion("lrojas", "Caja12345");
        verificar("El cajero no puede crear usuarios", !sistema.getUsuarios().registrar(cajero, "Cajero", "Otro", "otro", "o@correo.com", "Caja12345", 2).isEmpty());
        verificar("El cajero no puede modificar precios (polimorfismo en permisos)", !cajero.puedeModificarPrecios() && admin.puedeModificarPrecios());

        System.out.println("--- INVENTARIO ---");
        verificar("El cajero no puede agregar productos", !sistema.getProductos().agregar(cajero, "General", "X1", "Prueba", 100, 1, 1, null).isEmpty());
        verificar("Importa los 15 productos de ejemplo", sistema.getProductos().importar(admin, "ejemplo/productos_ejemplo.txt").isEmpty() && sistema.getProductos().getImportados() == 15);
        verificar("Detecta 1 producto con stock bajo", sistema.getProductos().productosBajoStock().size() == 1);
        verificar("Detecta 2 productos por vencer", sistema.getProductos().productosPorVencer().size() == 2);
        verificar("Detecta 1 producto vencido", sistema.getProductos().productosVencidos().size() == 1);
        verificar("Precio final distinto según el tipo (polimorfismo)",
                sistema.getProductos().buscarPorCodigo("100001").calcularPrecioFinal() < 1100 * 1.13
                && sistema.getProductos().buscarPorCodigo("100007").calcularPrecioFinal() < 650 * 1.13);
        verificar("Registra una entrada de mercadería", sistema.getProductos().registrarEntrada(admin, "100002", 20, "Distribuidora").isEmpty()
                && sistema.getProductos().buscarPorCodigo("100002").getStock() == 60);

        System.out.println("--- VENTAS ---");
        int stockArrozAntes = sistema.getProductos().buscarPorCodigo("100001").getStock();
        Venta venta = sistema.getVentas().iniciarVenta(cajero);
        verificar("Agrega productos al carrito", sistema.getVentas().agregarAlCarrito(venta, "100001", 3).isEmpty()
                && sistema.getVentas().agregarAlCarrito(venta, "100006", 2).isEmpty());
        verificar("No vende un producto vencido", !sistema.getVentas().agregarAlCarrito(venta, "100009", 1).isEmpty());
        verificar("No vende más de lo que hay en stock", !sistema.getVentas().agregarAlCarrito(venta, "100014", 99).isEmpty());
        verificar("Un pago insuficiente se rechaza y el stock no cambia",
                !sistema.getVentas().cobrar(venta, new Efectivo(100)).isEmpty()
                && sistema.getProductos().buscarPorCodigo("100001").getStock() == stockArrozAntes);
        double total = venta.calcularTotal();
        verificar("Cobra en efectivo y calcula el vuelto", sistema.getVentas().cobrar(venta, new Efectivo(20000)).isEmpty()
                && Math.abs(venta.getVuelto() - (20000 - total)) < 0.01);
        verificar("El stock baja al cobrar", sistema.getProductos().buscarPorCodigo("100001").getStock() == stockArrozAntes - 3);
        Venta venta2 = sistema.getVentas().iniciarVenta(admin);
        sistema.getVentas().agregarAlCarrito(venta2, "100011", 1);
        verificar("Cobra por SINPE", sistema.getVentas().cobrar(venta2, new Sinpe("88887777")).isEmpty());
        verificar("Rechaza un SINPE con teléfono inválido", !sistema.getVentas().cobrar(sistema.getVentas().iniciarVenta(admin), new Sinpe("12")).isEmpty());

        System.out.println("--- FACTURA PDF Y CORREO ---");
        File pdf = sistema.getVentas().generarPdf(venta);
        byte[] bytes = Files.readAllBytes(pdf.toPath());
        verificar("Crea la factura en PDF", pdf.exists() && bytes.length > 5000 && new String(bytes, 0, 5, StandardCharsets.ISO_8859_1).equals("%PDF-"));
        verificar("No crea PDF de una venta sin cobrar", sistema.getVentas().generarPdf(sistema.getVentas().iniciarVenta(cajero)) == null);
        verificar("Rechaza un correo de cliente mal escrito", !sistema.getVentas().enviarFactura(venta, "mal").isEmpty());
        verificar("Envía la factura con el PDF adjunto", sistema.getVentas().enviarFactura(venta, "cliente@gmail.com").isEmpty()
                && correo.getUltimoAdjunto().startsWith("factura_"));

        System.out.println("--- REPORTES Y SUCURSALES ---");
        String hoy = sistema.getReportes().hoy();
        verificar("El reporte suma lo recaudado", Math.abs(sistema.getReportes().totalRecaudado(hoy) - (total + venta2.calcularTotal())) < 0.01);
        verificar("El reporte cuenta 2 ventas", sistema.getReportes().cantidadVentas(hoy) == 2);
        verificar("El reporte cuenta las entradas de mercadería", sistema.getReportes().unidadesIngresadas(hoy) > 20);
        verificar("El cajero no puede agregar sucursales", !sistema.getSucursales().agregar(cajero, "Centro", "San José", 9.93, -84.08, 4).isEmpty());
        verificar("El administrador agrega una sucursal", sistema.getSucursales().agregar(admin, "AylorMade Centro", "San José centro", 9.9341, -84.0877, 8).isEmpty());
        verificar("Rechaza una latitud imposible", !sistema.getSucursales().agregar(admin, "Raro", "Ninguna", 123, -84, 2).isEmpty());

        System.out.println("--- RECUPERAR CONTRASEÑA ---");
        verificar("Envía el código al correo registrado", sistema.getUsuarios().solicitarCodigoRecuperacion("amora").isEmpty()
                && correo.getUltimoDestinatario().equals("ana@correo.com"));
        String mensaje = correo.getUltimoMensaje();
        String codigo = mensaje.substring(mensaje.indexOf("es: ") + 4, mensaje.indexOf("es: ") + 10);
        verificar("Rechaza un código incorrecto", !sistema.getUsuarios().restablecerClave("amora", "000000", "NuevaClave1").isEmpty());
        verificar("Cambia la contraseña con el código correcto", sistema.getUsuarios().restablecerClave("amora", codigo, "NuevaClave1").isEmpty()
                && sistema.getUsuarios().iniciarSesion("amora", "NuevaClave1") != null
                && sistema.getUsuarios().iniciarSesion("amora", "Admin123*") == null);

        System.out.println("--- PERSISTENCIA (se 'reinicia' el sistema) ---");
        SistemaSupermercado reiniciado = new SistemaSupermercado(carpeta, correo);
        verificar("Los usuarios siguen guardados", reiniciado.getUsuarios().listar().size() == 2);
        verificar("El inventario conserva el stock actualizado", reiniciado.getProductos().buscarPorCodigo("100001").getStock() == stockArrozAntes - 3);
        verificar("Las ventas siguen guardadas", reiniciado.getReportes().cantidadVentas(hoy) == 2);
        verificar("La sucursal sigue guardada", reiniciado.getSucursales().listar().size() == 1);
        verificar("El número de la próxima venta continúa la secuencia", reiniciado.getVentas().iniciarVenta(admin).getNumero() == 3);
        verificar("La nueva contraseña sigue funcionando", reiniciado.getUsuarios().iniciarSesion("amora", "NuevaClave1") != null);

        System.out.println("--- ARCHIVO DANADO ---");
        Files.write(Paths.get(carpeta + "/productos.txt"), "esto no es un producto\nGeneral|ZZ|Roto|abc|1|1|\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        ArrayList<Producto> leidos = new SistemaSupermercado(carpeta, correo).getProductos().listar();
        verificar("Con líneas dañadas en productos.txt el sistema sigue funcionando (las ignora)", leidos.size() == 15);

        System.out.println();
        System.out.println("RESULTADO: " + correctas + " verificaciones correctas, " + fallidas + " fallidas.");
        if (fallidas == 0) {
            System.out.println("TODO ESTA LISTO PARA ENTREGAR.");
        } else {
            System.out.println("REVISE LOS PASOS CON [FALLO].");
        }
    }
}
