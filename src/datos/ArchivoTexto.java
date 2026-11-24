package datos;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASE PADRE de la capa de datos: sabe leer y escribir lineas en un archivo .txt.
 * HERENCIA: ArchivoUsuarios, ArchivoProductos y ArchivoVentas la extienden,
 * asi no se repite el codigo de lectura y escritura.
 * ENCAPSULAMIENTO: la ruta es privada y los metodos de lectura/escritura son protected.
 */
public class ArchivoTexto {

    public static final String SEPARADOR = "|";

    private String ruta;

    public ArchivoTexto(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() {
        return ruta;
    }

    // Cantidad de lineas con datos que tiene el archivo (sin contar vacias ni comentarios).
    public int contarLineas() {
        return leerLineas().size();
    }

    // Quita el separador y los saltos de linea para que un dato no dane el formato del archivo.
    protected String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(SEPARADOR, " ").replace("\n", " ").replace("\r", " ").trim();
    }

    // Divide una linea en sus campos. El -1 conserva los campos vacios del final.
    protected String[] separar(String linea) {
        return linea.split("\\|", -1);
    }

    // Lee todas las lineas del archivo. Si el archivo no existe, devuelve una lista vacia.
    protected ArrayList<String> leerLineas() {
        ArrayList<String> lineas = new ArrayList<>();
        Path path = Paths.get(ruta);
        if (!Files.exists(path)) {
            return lineas;
        }
        try {
            List<String> leidas = leerConCodificacion(path);
            for (int i = 0; i < leidas.size(); i++) {
                String linea = leidas.get(i);
                // Algunos editores agregan una marca invisible (BOM) al inicio del archivo: se quita.
                if (i == 0 && linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
                }
                // se ignoran las lineas vacias y los comentarios (lineas que empiezan con #)
                if (!linea.trim().isEmpty() && !linea.trim().startsWith("#")) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo " + ruta + ": " + e.getMessage());
        }
        return lineas;
    }

    // Lee en UTF-8; si el archivo fue guardado en otra codificacion (por ejemplo ANSI en el Bloc de notas),
    // lo vuelve a leer como Windows-1252 para no perder los datos.
    private List<String> leerConCodificacion(Path path) throws IOException {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            return Files.readAllLines(path, Charset.forName("windows-1252"));
        }
    }

    public boolean existe() {
        return Files.exists(Paths.get(ruta));
    }

    // Ruta completa del archivo (sirve para mostrar en los mensajes de error).
    public String getRutaCompleta() {
        return Paths.get(ruta).toAbsolutePath().toString();
    }

    /**
     * Reescribe el archivo completo de forma segura: primero escribe todo en un archivo
     * temporal y solo si salio bien lo reemplaza por el original. Si el programa se cierra
     * a la mitad, el archivo original no queda danado.
     */
    protected boolean escribirLineas(ArrayList<String> lineas) {
        try {
            Path destino = Paths.get(ruta);
            Path temporal = Paths.get(ruta + ".tmp");
            if (destino.getParent() != null) {
                Files.createDirectories(destino.getParent());
            }
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("Error escribiendo " + ruta + ": " + e.getMessage());
            return false;
        }
    }

    // Agrega una linea al final del archivo (no toca lo que ya estaba).
    protected boolean agregarLinea(String linea) {
        try {
            Path destino = Paths.get(ruta);
            if (destino.getParent() != null) {
                Files.createDirectories(destino.getParent());
            }
            ArrayList<String> una = new ArrayList<>();
            una.add(linea);
            Files.write(destino, una, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.out.println("Error agregando a " + ruta + ": " + e.getMessage());
            return false;
        }
    }
}
