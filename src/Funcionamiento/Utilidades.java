/**
 * Contiene las clases que hacen funcionar las diferentes funciones de uso de back end y front de todo el proyecto
 *  
*/
package Funcionamiento;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.json.JSONException;

import Apis.ApiISBNGeneral;
import Apis.ApiMarvel;
import Controladores.VentanaAccionController;
import JDBC.DBLibreriaManager;
import JDBC.DBLibreriaManager.TipoBusqueda;
import JDBC.DBManager;
import alarmas.AlarmaList;
import comicManagement.Comic;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import webScrap.WebScraperPreviewsWorld;

/**
 * Esta clase sirve para realizar diferentes funciones realizanas con la
 * comprobacion del sistema operativo que ejecuta este programa, al igual que
 * comprobar cual es tu navegador principal de internet
 *
 * @author Alejandro Rodriguez
 */
public class Utilidades {

	/**
	 * Lista de cómics limpios.
	 */
	public static List<Comic> listaLimpia = new ArrayList<>();

	/**
	 * Lista de sugerencias de autocompletado de entrada limpia.
	 */
	public static List<String> listaLimpiaAutoCompletado = new ArrayList<>();

	/**
	 * Sistema operativo actual.
	 */
	public static String os = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);

	/**
	 * Mapa que almacena tasas de cambio.
	 */
	private static final Map<String, Double> tasasDeCambio = new HashMap<>();

	/**
	 * Obtenemos el directorio de inicio del usuario
	 */
	private final static String USER_DIR = System.getProperty("user.home");

	/**
	 * Construimos la ruta al directorio "Documents"
	 */
	private final static String DOCUMENTS_PATH = USER_DIR + File.separator + "Documents";

	/**
	 * Verifica si el sistema operativo es Windows.
	 *
	 * @return true si el sistema operativo es Windows, false en caso contrario.
	 */
	public static boolean isWindows() {
		return os.contains("win");
	}

	/**
	 * Verifica si el sistema operativo es macOS (Mac).
	 *
	 * @return true si el sistema operativo es macOS, false en caso contrario.
	 */
	public static boolean isMac() {
		return os.contains("mac");
	}

	/**
	 * Verifica si el sistema operativo es Unix o Linux.
	 *
	 * @return true si el sistema operativo es Unix o Linux, false en caso
	 *         contrario.
	 */
	public static boolean isUnix() {
		return os.contains("nux");
	}

	/**
	 * Funcion que permite comprobar que navegadores tienes instalados en el sistema
	 * operativo linux y abre aquel que tengas en predeterminado.
	 *
	 * @param url
	 * @return
	 */
	public static StringBuffer navegador(String url) {
		String[] browsers = { "google-chrome", "firefox", "mozilla", "epiphany", "konqueror", "netscape", "opera",
				"links", "lynx" };

		StringBuffer cmd = new StringBuffer();
		for (int i = 0; i < browsers.length; i++) {
			if (i == 0) {
				cmd.append(String.format("%s \"%s\"", browsers[i], url));
			} else {
				cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
			}
		}
		return cmd;
	}

	/**
	 * Funcion que permite abrir navegador y pagina web de GitHub en Windows
	 *
	 * @param url
	 */
	public static void accesoWebWindows(String url) {
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Funcion que permite abrir navegador y pagina web de GitHub en Linux
	 *
	 * @param url
	 */
	public static void accesoWebLinux(String url) {
		Runtime rt = Runtime.getRuntime();

		StringBuffer cmd;
		try {
			cmd = Utilidades.navegador(url);
			rt.exec(new String[] { "sh", "-c", cmd.toString() }); // Ejecuta el bucle y permite abrir el navegador que
			// tengas principal
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Funcion que permite abrir navegador y pagina web de GitHub en Linux
	 *
	 * @param url
	 */
	public static void accesoWebMac(String url) {
		Runtime runtime = Runtime.getRuntime();
		String[] args = { "osascript", "-e", "open location \"" + url + "\"" };
		try {
			runtime.exec(args);
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Funcion que cambia una ',' por un guion '-'
	 *
	 * @param campos
	 * @return
	 */
	public String comaPorGuion(String dato) {

		String resultado = "";
		if (dato != null) {
			if (dato.contains(",")) {
				dato = dato.replace(",", " - ");
			}
			resultado = eliminarEspacios(dato);
		}

		return resultado;
	}

	/**
	 * Funcion que elimina los espacios del principios y finales
	 *
	 * @param campos
	 * @return
	 */
	public static String eliminarEspacios(String dato) {

		String resultado = "";
		if (dato != null) {
			// Elimina espacios adicionales al principio y al final.
			dato = dato.trim();

			// Reemplaza espacios múltiples entre palabras por un solo espacio.
			resultado = dato.replaceAll("\\s+", " ");
		}
		return resultado;
	}

	/**
	 * Funcion que permite la redimension de una imagen. Guarda la imagen y es
	 * cargada por otras funciones.
	 *
	 * @param file
	 * @return
	 */
	public void nueva_imagen(String imagen, String nuevoNombreArchivo) {
		try {
			File file = new File(imagen);
			InputStream input = null;

			if (!file.exists()) {
				input = getClass().getResourceAsStream("sinPortada.jpg");
				if (input == null) {
					throw new FileNotFoundException("La imagen predeterminada no se encontró en el paquete");
				}
				file = File.createTempFile("tmp", ".jpg");
				file.deleteOnExit();
				try (OutputStream output = new FileOutputStream(file)) {
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = input.read(buffer)) != -1) {
						output.write(buffer, 0, bytesRead);
					}
				}
			}

			String userDir = System.getProperty("user.home");
			String documentsPath = userDir + File.separator + "Documents";
			String defaultImagePath = documentsPath + File.separator + "libreria_comics" + File.separator
					+ DBManager.DB_NAME + File.separator + "portadas";

			// Esto se modificara para hacerlo dinamico
			String imagePath = defaultImagePath;

			File portadasFolder = new File(imagePath);

			if (!portadasFolder.exists()) {
				if (!portadasFolder.mkdirs()) {
					throw new IOException("No se pudo crear la carpeta 'portadas'");
				}
			}

			File newFile = new File(portadasFolder.getPath() + File.separator + nuevoNombreArchivo + ".jpg");
			Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Copia un directorio y cuenta los archivos que no existen durante la copia.
	 *
	 * @param sourceDirectoryPath      Ruta del directorio fuente.
	 * @param destinationDirectoryPath Ruta del directorio de destino.
	 */
	public static void copyDirectory(String sourceDirectoryPath, String destinationDirectoryPath) {
		Path sourceDirectory = Paths.get(sourceDirectoryPath);
		Path destinationDirectory = Paths.get(destinationDirectoryPath);

		final int[] contadorArchivosNoExistentes = { 0 }; // Array de un solo elemento para almacenar el contador

		try {
			if (!Files.exists(destinationDirectory)) {
				Files.createDirectories(destinationDirectory);
			}

			Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
					if (sourceFile.toString().endsWith(".jpg")) {
						Path destinationPath = destinationDirectory.resolve(sourceDirectory.relativize(sourceFile));
						try {
							Files.copy(sourceFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
						} catch (NoSuchFileException e) {
							contadorArchivosNoExistentes[0]++; // Incrementar el contador utilizando el array
						} catch (IOException e) {
							System.err.println("Se produjo un error al copiar el archivo: " + e.getMessage());
						}
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					System.err.println("No se pudo visitar el archivo: " + file + ". Motivo: " + exc.getMessage());
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Obtiene el nombre completo de un cómic con información específica.
	 *
	 * @param datos Los datos del cómic.
	 * @return El nombre completo del cómic con formato para archivo.
	 */
	public String obtenerNombreCompleto(Comic datos) {
		String userDir = System.getProperty("user.home");
		String documentsPath = userDir + File.separator + "Documents";
		String defaultImagePath = documentsPath + File.separator + "libreria_comics" + File.separator
				+ DBManager.DB_NAME + File.separator + "portadas" + File.separator;
		String nombre_comic = datos.getNombre().replace(" ", "_").replace(":", "_").replace("-", "_");
		String numero_comic = datos.getNumero();
		String variante_comic = datos.getVariante().replace(" ", "_").replace(",", "_").replace("-", "_").replace(":",
				"_");
		String fecha_comic = datos.getFecha();
		String nombre_completo = nombre_comic + "_" + numero_comic + "_" + variante_comic + "_" + fecha_comic;
		String extension = ".jpg";
		String nuevoNombreArchivo = defaultImagePath + nombre_completo + extension;
		return nuevoNombreArchivo;
	}

	/**
	 * Crea un nuevo nombre de archivo para un cómic con información específica.
	 *
	 * @param datos Los datos del cómic.
	 * @return El nuevo nombre de archivo del cómic con formato para archivo.
	 */
	public String crearNuevoNombre(Comic datos) {
		String nombre_comic = datos.getNombre().replace(" ", "_").replace(":", "_").replace("-", "_");
		String numero_comic = datos.getNumero();
		String variante_comic = datos.getVariante().replace(" ", "_").replace(",", "_").replace("-", "_").replace(":",
				"_");
		String fecha_comic = datos.getFecha();
		String nombre_completo = nombre_comic + "_" + numero_comic + "_" + variante_comic + "_" + fecha_comic;
		String extension = ".jpg";
		String nuevoNombreArchivo = nombre_completo + extension;
		return nuevoNombreArchivo;
	}

	/**
	 * Obtiene el nombre de archivo de una ruta completa.
	 *
	 * @param rutaCompleta La ruta completa del archivo.
	 * @return El nombre del archivo sin la ruta.
	 */
	public static String obtenerNombreArchivo(String rutaCompleta) {
		// Obtener el separador de ruta del archivo según el sistema operativo
		String separadorRuta = File.separator;

		// Obtener la última posición del separador de ruta del archivo en la ruta
		// completa
		int posicionSeparador = rutaCompleta.lastIndexOf(separadorRuta);

		// Extraer el nombre del archivo sin la ruta
		String nombreArchivo = rutaCompleta.substring(posicionSeparador + 1);

		return nombreArchivo;
	}

	/**
	 * Realiza una copia de seguridad de los archivos.
	 * 
	 * @throws SQLException
	 */
	public static void copia_seguridad() throws SQLException {
		// Realizar copia de seguridad
		FuncionesExcel excel = new FuncionesExcel();

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String nombre_carpeta = dateFormat.format(new Date());

			String userDir = System.getProperty("user.home");
			String documentsPath = userDir + File.separator + "Documents";
			String sourcePath = documentsPath + File.separator + "libreria_comics" + File.separator + DBManager.DB_NAME
					+ File.separator + "portadas";
			File sourceFolder = new File(sourcePath);

			String ubicacion = userDir + File.separator + "AppData" + File.separator + "Roaming";
			String carpetaLibreria = ubicacion + File.separator + "libreria" + File.separator + DBManager.DB_NAME
					+ File.separator + "backups" + File.separator + nombre_carpeta;

			if (sourceFolder.exists()) {
				// Create the backups folder if it doesn't exist
				File backupsFolder = new File(carpetaLibreria);
				if (!backupsFolder.exists()) {
					if (!backupsFolder.mkdirs()) {
						throw new IOException("Failed to create 'backups' folder.");
					}
				}

				// Crear archivo zip con fecha actual
				String backupFileName = "portadas_" + dateFormat.format(new Date()) + ".zip";
				String backupPath = carpetaLibreria + File.separator + backupFileName;
				File backupFile = new File(backupPath);

				// Comprimir carpeta en el archivo zip
				try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(backupFile))) {
					zipFile(sourceFolder, sourceFolder.getName(), zipOut);
				}
				excel.savedataExcel(nombre_carpeta);
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Comprime un archivo o carpeta en un archivo zip.
	 *
	 * @param fileToZip El archivo o carpeta que se va a comprimir.
	 * @param fileName  El nombre del archivo.
	 * @param zipOut    El flujo de salida del archivo zip.
	 * @throws IOException Si ocurre un error de E/S durante la compresión.
	 */
	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}

		if (fileToZip.isDirectory()) {
			fileName = fileName.endsWith("/") ? fileName : fileName + "/";
			zipOut.putNextEntry(new ZipEntry(fileName));

			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(fileToZip.toPath())) {
				for (Path childPath : dirStream) {
					zipFile(childPath.toFile(), fileName + childPath.getFileName(), zipOut);
				}
			}
			return;
		}

		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[4096];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
		}
	}

	/**
	 * Elimina todos los archivos en una carpeta específica.
	 */
	public static void eliminarArchivosEnCarpeta() {

		String userDir = System.getProperty("user.home");
		String documentsPath = userDir + File.separator + "Documents";
		String sourcePath = documentsPath + File.separator + "libreria_comics" + File.separator + DBManager.DB_NAME
				+ File.separator + "portadas";

		File carpeta = new File(sourcePath);
		if (carpeta.exists() && carpeta.isDirectory()) {
			File[] archivos = carpeta.listFiles();
			if (archivos != null) {
				for (File archivo : archivos) {
					if (archivo.isFile()) {
						archivo.delete();
					}
				}
			}
		}
	}

	/**
	 * Crea una copia de seguridad de la base de datos.
	 */
	public void crearCopiaBaseDatos() {
		try {

			String userHome = System.getProperty("user.home");
			String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
			String carpeta_backups = ubicacion + File.separator + "libreria" + File.separator + "backups"
					+ File.separator + DBManager.DB_NAME + File.separator;

			String pathMySql = "C:\\Program Files\\MySQL";
			String mysqlDump = pathMySql + "\\mysqldump.exe";

			String nombreCopia = "copia_base_datos.sql";
			File carpetaDestino = new File(carpeta_backups);
			File archivoCopia = new File(carpetaDestino, nombreCopia);

			String[] command = new String[] { mysqlDump, "-u" + DBManager.DB_USER, "-p" + DBManager.DB_PASS, "-B",
					DBManager.DB_NAME, "--hex-blob", "--routines=true",
					"--result-file=" + archivoCopia.getAbsolutePath() };

			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.to(archivoCopia));
			pb.start();

		} catch (Exception e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Funcion que devuelve una lista en la que solamente se guardan aquellos datos
	 * que no se repiten
	 *
	 * @param listaComics
	 * @return
	 */
	public static List<Comic> listaArreglada(List<Comic> listaComics) {

		// Forma número 1 (Uso de Maps).
		Map<String, Comic> mapComics = new HashMap<>(listaComics.size());

		// Aquí está la magia
		for (Comic c : listaComics) {
			mapComics.put(c.getID(), c);
		}

		// Agrego cada elemento del map a una nueva lista y muestro cada elemento.

		for (Entry<String, Comic> c : mapComics.entrySet()) {

			listaLimpia.add(c.getValue());

		}
		return listaLimpia;
	}

	/**
	 * Funcion que devuelve una lista en la que solamente se guardan aquellos datos
	 * que no se repiten
	 *
	 * @param listaComics
	 * @return
	 */
	public static List<String> listaArregladaAutoComplete(List<String> listaComics) {
		Set<String> uniqueSet = new HashSet<>();
		List<String> result = new ArrayList<>();

		for (String s : listaComics) {
			if (uniqueSet.add(s)) {
				result.add(s);
			}
		}

		// Ordenar la lista resultante de forma ascendente
		result.sort(String::compareTo);

		return result;
	}

	/**
	 * Elimina la imagen temporal de muestra de la base de datos.
	 */
	public void deleteImage() {
		try {
			Files.deleteIfExists(Paths.get("tmp.jpg"));
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/*
	 * Convierte los nombres de los archivos en una carpeta, reemplazando los
	 * guiones por guiones bajos.
	 * 
	 * @param rutaCarpeta La ruta de la carpeta que contiene los archivos.
	 * 
	 * @throws IOException Si hay un error al leer el contenido de la carpeta.
	 */
	public static void convertirNombresCarpetas(String rutaCarpeta) throws IOException {

		File carpeta = new File(rutaCarpeta);

		if (!carpeta.isDirectory()) {
			throw new IllegalArgumentException("La ruta proporcionada no es una carpeta.");
		}

		File[] archivos = carpeta.listFiles();
		if (archivos == null) {
			throw new IOException("Error al leer el contenido de la carpeta.");
		}

		for (File archivo : archivos) {
			if (archivo.isFile()) {
				String nombreArchivoAntiguo = archivo.getName();
				String nombreArchivoNuevo = convertirNombreArchivo(nombreArchivoAntiguo);
				if (!nombreArchivoAntiguo.equals(nombreArchivoNuevo)) {
					File nuevoArchivo = new File(carpeta, nombreArchivoNuevo);
					archivo.renameTo(nuevoArchivo);

				}
			}
		}
	}

	/**
	 * Convierte el nombre de un archivo, reemplazando los guiones por guiones
	 * bajos.
	 * 
	 * @param nombreArchivo El nombre del archivo a convertir.
	 * 
	 * @return El nuevo nombre de archivo convertido.
	 */
	public static String convertirNombreArchivo(String nombreArchivo) {

		StringBuilder nombreConvertido = new StringBuilder();

		for (int i = 0; i < nombreArchivo.length(); i++) {
			char caracterActual = nombreArchivo.charAt(i);

			if (caracterActual == '-' && esPosicionGuionValida(nombreArchivo, i)) {
				nombreConvertido.append('_');
			} else {
				nombreConvertido.append(caracterActual);
			}
		}

		return nombreConvertido.toString();
	}

	/**
	 * Verifica si la posición de un guion en el nombre de archivo es válida para
	 * convertirlo.
	 * 
	 * @param nombreArchivo El nombre del archivo.
	 * 
	 * @param indice        El índice del guion en el nombre del archivo.
	 * 
	 * @return true si la posición del guion es válida, false en caso contrario.
	 */
	public static boolean esPosicionGuionValida(String nombreArchivo, int indice) {

		// Verificar si el guion está precedido por un dígito
		if (indice > 0 && Character.isDigit(nombreArchivo.charAt(indice - 1))) {
			return false;
		}
		// Verificar si el guion está seguido por un dígito
		if (indice < nombreArchivo.length() - 1 && Character.isDigit(nombreArchivo.charAt(indice + 1))) {
			return false;
		}

		return true;
	}

	/**
	 * Funcion que devuelve la direccion de una url sin tener en cuenta el fichero y
	 * extension
	 * 
	 * @param rutaArchivo
	 * @return
	 */
	public static String eliminarDespuesUltimoPortadas(String rutaArchivo) {
		int indiceUltimoPortadas = rutaArchivo.lastIndexOf("portadas\\");
		if (indiceUltimoPortadas != -1) {
			return rutaArchivo.substring(0, indiceUltimoPortadas + 9);
		} else {
			return rutaArchivo;
		}
	}

	/**
	 * Funcion que solamente deuvelve el nombre del fichero y la extension dada una
	 * direccion
	 * 
	 * @param rutaArchivo
	 * @return
	 */
	public static String obtenerDespuesPortadas(String rutaArchivo) {
		int indicePortadas = rutaArchivo.indexOf("portadas\\");
		if (indicePortadas != -1) {
			return rutaArchivo.substring(indicePortadas + 9);
		} else {
			return "";
		}
	}

	/**
	 * Elimina un fichero en la ubicación especificada.
	 *
	 * @param direccion La dirección del fichero a eliminar.
	 */
	public static void eliminarFichero(String direccion) {

		Platform.runLater(() -> {
			File archivo = new File(direccion);

			archivo.delete();
		});
	}

	/**
	 * Renombra un archivo en una carpeta específica.
	 *
	 * @param carpeta              La carpeta donde se encuentra el archivo.
	 * @param nombreArchivoBuscado El nombre del archivo que se desea renombrar.
	 * @param nuevoNombreArchivo   El nuevo nombre para el archivo.
	 */
	public static void renombrarArchivo(String carpeta, String nombreArchivoBuscado, String nuevoNombreArchivo) {
		File directorio = new File(carpeta);
		File[] archivos = directorio.listFiles();

		if (archivos != null) {
			for (File archivo : archivos) {
				if (archivo.isFile() && !archivo.getName().equals(nombreArchivoBuscado)) {
					File nuevoArchivo = new File(directorio, nuevoNombreArchivo);
					archivo.renameTo(nuevoArchivo);

				}
			}
		}
	}

	/**
	 * Genera un codigo unico para renombrar una imagen solamente si el codigo no
	 * existe, si no, vuelve a crear otro hasta que aparezca uno que sea adecuado
	 * 
	 * @param carpeta
	 * @return
	 */
	public static String generarCodigoUnico(String carpeta) {
		String codigo;
		File directorio = new File(carpeta);
		File archivo = null;
		do {
			codigo = generarCodigo(); // Genera un nuevo código único
			String nombreArchivo = codigo + ".jpg";
			archivo = new File(directorio, nombreArchivo);
		} while (archivo.exists());

		return codigo;
	}

	/**
	 * Genera un codigo de forma aleatoria
	 * 
	 * @return
	 */
	private static String generarCodigo() {
		String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder codigo = new StringBuilder();

		for (int i = 0; i < 10; i++) {
			int indice = (int) (Math.random() * caracteres.length());
			codigo.append(caracteres.charAt(indice));
		}

		return codigo.toString();
	}

	/**
	 * Obtiene la carpeta de configuración en el sistema de usuario.
	 *
	 * @return La ruta de la carpeta de configuración.
	 */
	private static String carpetaConfiguracion() {
		String userDir = System.getProperty("user.home");
		String ubicacion = userDir + File.separator + "AppData" + File.separator + "Roaming";
		String direccion = ubicacion + File.separator + "libreria";

		File directorio = new File(direccion);
		if (!directorio.exists()) {
			directorio.mkdirs(); // Crear directorios si no existen
		}

		return direccion;
	}

	/**
	 * Guarda datos de claves de API de Marvel en un archivo de configuración.
	 */
	public static void guardarDatosClavesMarvel() {
		String nombreArchivo = carpetaConfiguracion() + File.separator + "claves_marvel_api.conf";

		File archivo = new File(nombreArchivo);

		if (!archivo.exists()) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
				writer.write("Public Key: ");
				writer.newLine();
				writer.write("Private Key: ");

				writer.close();
			} catch (IOException e) {
				manejarExcepcion(e);
			}
		}
	}

	/**
	 * Obtiene las claves de la API de Marvel desde un archivo de configuración.
	 *
	 * @return Las claves de la API en el formato "Private Key: Public Key".
	 */
	public static String obtenerClaveApiArchivo() {
		String nombreArchivo = carpetaConfiguracion() + File.separator + "claves_marvel_api.conf";

		File archivo = new File(nombreArchivo);
		if (!archivo.exists()) {
			guardarDatosClavesMarvel();
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
			String linea;
			String clavePublica = null;
			String clavePrivada = null;

			while ((linea = reader.readLine()) != null) {
				if (linea.startsWith("Public Key: ")) {
					clavePublica = linea.substring("Public Key: ".length()).trim();

				} else if (linea.startsWith("Private Key: ")) {
					clavePrivada = linea.substring("Private Key: ".length()).trim();
				}
			}

			// Verificar que ambas claves se hayan encontrado en el archivo
			if (clavePublica != null && clavePrivada != null) {
				return clavePrivada + ":" + clavePublica; // Retornar ambas claves en un formato deseado
			} else {
				return ""; // Manejo de error: devuelve una cadena vacía en caso de que falte alguna clave
			}
		} catch (IOException e) {
			manejarExcepcion(e);
			return ""; // Manejo de error: devuelve una cadena vacía en caso de error
		}
	}

	/**
	 * Carga tasas de cambio desde un archivo de configuración y las almacena en un
	 * mapa.
	 */
	public static void cargarTasasDeCambioDesdeArchivo() {
		String nombreArchivo = carpetaConfiguracion() + File.separator + "tasas_de_cambio.conf";

		// Verificar si el archivo existe y, si no, crearlo con los valores
		// predeterminados
		File archivo = new File(nombreArchivo);
		if (!archivo.exists()) {
			crearArchivoConValoresPredeterminados(nombreArchivo);
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(": ");
				if (parts.length == 2) {
					String pais = parts[0];
					double tasa = Double.parseDouble(parts[1]);
					tasasDeCambio.put(pais, tasa);
				}
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Crea un archivo de configuración con valores predeterminados.
	 *
	 * @param nombreArchivo El nombre del archivo a crear.
	 */
	private static void crearArchivoConValoresPredeterminados(String nombreArchivo) {
		try (PrintWriter writer = new PrintWriter(nombreArchivo)) {
			// Agregar los valores predeterminados al archivo
			String[] valoresPredeterminados = { "Japón (Japan): 141.75", "Estados Unidos (United States): 1.0",
					"Francia (France): 0.9085", "Italia (Italy): 0.9085", "España (Spain): 0.9085",
					"Reino Unido (United Kingdom): 0.78", "Alemania (Germany): 0.9085", "Brasil (Brazil): 4.87",
					"Corea del Sur (South Korea): 1304.04", "México (Mexico): 17.08", "Canadá (Canada): 1.34",
					"China (China): 7.17", "Australia (Australia): 1.67", "Argentina (Argentina): 276.01",
					"India (India): 82.68", "Bélgica (Belgium): 0.9085", "Países Bajos (Netherlands): 0.9085",
					"Portugal (Portugal): 0.9085", "Suecia (Sweden): 10.59", "Suiza (Switzerland): 0.87",
					"Finlandia (Finland): 0.9085", "Noruega (Norway): 10.14", "Dinamarca (Denmark): 6.77" };

			for (String valor : valoresPredeterminados) {
				writer.println(valor);
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Convierte una cantidad de moneda local a dólares.
	 *
	 * @param pais                El país del que se quiere convertir la moneda.
	 * @param cantidadMonedaLocal La cantidad de moneda local a convertir.
	 * @return La cantidad equivalente en dólares.
	 */
	public static double convertirMonedaADolar(String pais, String precio_comic) {
		if (tasasDeCambio.containsKey(pais)) {
			double tasaDeCambio = tasasDeCambio.get(pais);
			try {
				double cantidadMonedaLocal = Double.parseDouble(precio_comic);
				if (cantidadMonedaLocal > 0) {
					double resultado = cantidadMonedaLocal / tasaDeCambio;
					return Math.round(resultado * 100.0) / 100.0;
				}
			} catch (NumberFormatException e) {
				// Manejo de error si la conversión de la cadena a double falla
				manejarExcepcion(e);
			}
		}
		return 0.0; // Devolver 0 si el país no está en la lista, la cantidad no es un número o si
					// es negativa
	}

	/**
	 * Verifica si hay conexión a Internet disponible.
	 *
	 * @return true si hay conexión a Internet, false en caso contrario.
	 */
	public static boolean isInternetAvailable() {
		try {
			// Puedes cambiar la dirección a una página web confiable que esté disponible
			InetAddress.getByName("www.google.com").isReachable(1000); // 1000 ms = 1 segundo de tiempo de espera
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Guarda el nombre de usuario y contraseña en un archivo de configuración.
	 *
	 * @param usuario El campo de texto que contiene el nombre de usuario.
	 * @param pass    La contraseña del usuario.
	 * @throws IOException Si ocurre un error al escribir en el archivo.
	 */
	public static void guardarUsuario(TextField usuario, String pass) {

		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
		String carpetaLibreria = ubicacion + File.separator + "libreria";
		String archivoConfiguracion = carpetaLibreria + File.separator + "configuracion_usuario.conf";

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(archivoConfiguracion);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write("###############################");
			bufferedWriter.newLine();
			bufferedWriter.write("Usuario y contraseño del usuario");
			bufferedWriter.newLine();
			bufferedWriter.write("###############################");
			bufferedWriter.newLine();
			bufferedWriter.write("Usuario: " + usuario.getText());
			bufferedWriter.newLine();
			bufferedWriter.write("Password: " + pass);
			bufferedWriter.newLine();

			bufferedWriter.close();
		} catch (IOException e) {
			manejarExcepcion(e);
		}

	}

	/**
	 * Esta función guarda una clave de API para Comic Vine en un archivo de
	 * configuración. La clave de API se almacena en un archivo en la ubicación de
	 * la librería del usuario. Si el archivo ya existe, se sobrescribe.
	 *
	 */
	public static void guardarApiComicVine() {
		String nombreArchivo = carpetaConfiguracion() + File.separator + "clave_comicVine_api.conf";

		File archivo = new File(nombreArchivo);

		if (!archivo.exists()) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
				writer.write("###############################");
				writer.newLine();
				writer.write("Clave Api Comic Vine: ");
				writer.newLine();
				writer.write("###############################");

				writer.close();
			} catch (IOException e) {
				manejarExcepcion(e);
			}
		}
	}

	public static String cargarApiComicVine() {
		String nombreArchivo = carpetaConfiguracion() + File.separator + "clave_comicVine_api.conf";

		// Verificar si el archivo existe y, si no, crearlo con los valores
		// predeterminados
		File archivo = new File(nombreArchivo);
		if (!archivo.exists()) {
			guardarApiComicVine();
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(": ");
				if (parts.length == 2) {
					String clave_api = parts[1];
					return clave_api;
				}
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}
		return null;
	}

	/**
	 * Obtiene las claves de la API de Marvel de un archivo o fuente de datos.
	 *
	 * @return Un array de cadenas con las claves pública y privada de la API.
	 */
	public static String[] clavesApiMarvel() {
		String claves[] = new String[2]; // Crear un arreglo de dos elementos para almacenar las claves

		String clavesDesdeArchivo = Utilidades.obtenerClaveApiArchivo(); // Obtener las claves desde el archivo

		if (!clavesDesdeArchivo.isEmpty()) {
			String[] partes = clavesDesdeArchivo.split(":");
			if (partes.length == 2) {
				String clavePublica = partes[0].trim();
				String clavePrivada = partes[1].trim();

				claves[0] = clavePublica; // Almacenar la clave pública en el primer elemento del arreglo
				claves[1] = clavePrivada; // Almacenar la clave privada en el segundo elemento del arreglo
			}
		}

		return claves;
	}

	/**
	 * Verifica si hay contenido en las cadenas apiKey y claves.
	 *
	 * @param apiKey La cadena que contiene la clave API de Comic Vine.
	 * @param claves Un arreglo de cadenas que contiene las claves API de Marvel.
	 * @return true si ambas cadenas contienen contenido válido, false en caso
	 *         contrario.
	 */
	public static boolean existeContenido(String apiKey, String[] clavesMarvel) {
		// Comprueba si apiKey es nulo o está vacío
		if (apiKey == null || apiKey.isEmpty()) {
			return false;
		}

		// Comprueba si claves es nulo o está vacío
		if (clavesMarvel == null || clavesMarvel.length < 2 || clavesMarvel[0] == null || clavesMarvel[0].isEmpty()
				|| clavesMarvel[1] == null || clavesMarvel[1].isEmpty()) {
			return false;
		}

		// Si no se cumplen las condiciones anteriores, significa que hay contenido
		// válido
		return true;
	}

	/**
	 * Verifica si una cadena es una URL válida.
	 *
	 * @param cadena Cadena que se va a verificar como URL.
	 * @return true si la cadena es una URL válida, false en caso contrario.
	 */
	public static boolean isImageURL(String cadena) {

		if (isURL(cadena)) {
			// Obtener la extensión del archivo desde la URL
			String extension = getFileExtensionFromURL(cadena);

			// Lista de extensiones de imagen comunes
			String[] imageExtensions = { "jpg", "jpeg", "png", "gif", "bmp" };

			// Verificar si la extensión está en la lista de extensiones de imagen
			for (String imageExtension : imageExtensions) {
				if (extension.equalsIgnoreCase(imageExtension)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Verifica si una cadena es una URL válida.
	 *
	 * @param cadena Cadena que se va a verificar como URL.
	 * @return true si la cadena es una URL válida, false en caso contrario.
	 */
	public static boolean isURL(String cadena) {
		try {
			new URI(cadena);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	/**
	 * Verifica si la cadena proporcionada representa una ruta de archivo local.
	 * 
	 * @param cadena La cadena que se va a verificar.
	 * @return true si la cadena es una ruta de archivo local absoluta, false de lo
	 *         contrario.
	 */
	public static boolean isRutaDeArchivo(String cadena) {
		try {
			Path path = Paths.get(cadena);
			return path.isAbsolute(); // Verifica si es una ruta de archivo absoluta
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Obtiene la extensión del archivo desde una URL.
	 *
	 * @param url URL de la que se va a obtener la extensión.
	 * @return La extensión del archivo o una cadena vacía si no se encuentra.
	 */
	public static String getFileExtensionFromURL(String url) {
		int lastDotIndex = url.lastIndexOf('.');
		if (lastDotIndex > 0) {
			return url.substring(lastDotIndex + 1).toLowerCase();
		}
		return "";
	}

	/**
	 * Descarga una imagen desde una URL y la guarda en una carpeta de destino.
	 * 
	 * @param urlImagen      URL de la imagen a descargar.
	 * @param carpetaDestino Ruta de la carpeta de destino.
	 * @return Ruta de destino de la imagen descargada o null si hay un error.
	 * @throws IOException Si ocurre un error de entrada/salida.
	 */
	public static CompletableFuture<String> descargarImagenAsync(String urlImagen, String carpetaDestino) {
		Executor executor = Executors.newCachedThreadPool();
		CompletableFuture<String> downloadTask = new CompletableFuture<>();

		@SuppressWarnings("unused")
		CompletableFuture<Void> asyncTask = CompletableFuture.runAsync(() -> {
			try {

				String nombreImagen = "";
				nombreImagen = obtenerNombreImagen(urlImagen);

				crearCarpetaSiNoExiste(carpetaDestino);
				String rutaDestino = carpetaDestino + File.separator + nombreImagen;

				downloadTask.complete(rutaDestino);
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
				downloadTask.completeExceptionally(e);
			} catch (Exception e) {
				System.err.println("No se pudo acceder a la URL: " + urlImagen);
				e.printStackTrace();
				downloadTask.completeExceptionally(e);
			}
		}, executor);

		// Handle executor shutdown
		downloadTask.whenComplete((result, throwable) -> {
			((ExecutorService) executor).shutdown();
		});

		return downloadTask;
	}

	/**
	 * Valida una URL y la convierte en una URI.
	 * 
	 * @param urlImagen URL de la imagen a validar.
	 * @return URI válida de la URL.
	 * @throws IllegalArgumentException Si la URL no es válida.
	 */
	@SuppressWarnings("unused")
	private static URI validarURL(String urlImagen) throws IllegalArgumentException {
		try {
			return new URI(urlImagen);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("URL de imagen no válida: " + urlImagen, e);
		}
	}

	/**
	 * Obtiene el nombre de la imagen a partir de una URL.
	 * 
	 * @param urlImagen URL de la imagen.
	 * @return Nombre de la imagen.
	 */
	public static String obtenerNombreImagen(String urlImagen) {
		String[] partesURL = urlImagen.split("/");
		return partesURL[partesURL.length - 1];
	}

	/**
	 * Crea una carpeta de destino si no existe.
	 * 
	 * @param carpetaDestino Ruta de la carpeta de destino.
	 * @return Objeto File de la carpeta de destino.
	 */
	private static File crearCarpetaSiNoExiste(String carpetaDestino) {
		File carpeta = new File(carpetaDestino);
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}
		return carpeta;
	}

	/**
	 * Descarga una imagen desde una URL y la guarda en una carpeta de destino.
	 * 
	 * @param urlImagen      URI de la imagen a descargar.
	 * @param carpetaDestino Ruta de la carpeta de destino.
	 * @return true si la descarga y conversión son exitosas, false en caso
	 *         contrario.
	 */
	public static CompletableFuture<Boolean> descargarYConvertirImagenAsync(URI urlImagen, String carpetaDestino,
			String nuevoNombre) {

		return CompletableFuture.supplyAsync(() -> {
			try {
				URL url = urlImagen.toURL();
				URLConnection connection = url.openConnection();

				if (connection instanceof HttpURLConnection) {
					((HttpURLConnection) connection).setRequestMethod("HEAD");
					int responseCode = ((HttpURLConnection) connection).getResponseCode();

					if (responseCode != HttpURLConnection.HTTP_OK) {
						if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
							System.err.println("Error interno del servidor al acceder a la URL: " + url);
							// Realiza acciones específicas para manejar el error interno del servidor
							// Puede ser útil registrar o notificar sobre este tipo de errores
						} else {
							System.err.println("La URL no apunta a una imagen válida o no se pudo acceder: " + url);
						}
						return false;
					}
				}

				String finalNuevoNombre = nuevoNombre; // Create a new effectively final variable

				String extension = obtenerExtension(finalNuevoNombre);
				if (!extension.equals("jpg")) {
					BufferedImage image = ImageIO.read(url);

					if (image == null) {
						System.err.println("No se pudo cargar la imagen desde " + urlImagen);
						return false;
					}

					finalNuevoNombre += ".jpg";
					Path rutaDestino = Path.of(carpetaDestino, finalNuevoNombre);

					ImageIO.write(image, "jpg", rutaDestino.toFile());
				} else {
					Path rutaDestino = Path.of(carpetaDestino, finalNuevoNombre);
					try (InputStream in = url.openStream()) {
						java.nio.file.Files.copy(in, rutaDestino, StandardCopyOption.REPLACE_EXISTING);
					}
				}

				return true;
			} catch (IOException e) {
				manejarExcepcion(e);
				return false;
			}
		});
	}

	private static String obtenerExtension(String nombreArchivo) {
		int ultimoPunto = nombreArchivo.lastIndexOf(".");
		if (ultimoPunto == -1) {
			return "";
		}
		return nombreArchivo.substring(ultimoPunto + 1).toLowerCase();
	}

	/**
	 * Borra un archivo de imagen dada su ruta.
	 *
	 * @param rutaImagen Ruta del archivo de imagen que se va a borrar.
	 * @return true si se borra con éxito, false si no se puede borrar o el archivo
	 *         no existe.
	 */
	public static boolean borrarImagen(String rutaImagen) {

		File archivo = new File(rutaImagen);

		// Verificar si el archivo existe antes de intentar borrarlo
		if (archivo.exists()) {
			if (archivo.delete()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Reemplaza entidades HTML en una cadena con sus caracteres correspondientes.
	 *
	 * @param input Cadena que puede contener entidades HTML como &amp;, &lt;, &gt;,
	 *              etc.
	 * @return La cadena con las entidades HTML reemplazadas por los caracteres
	 *         correspondientes.
	 */
	public static String replaceHtmlEntities(String input) {
		return input.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"")
				.replaceAll("&#39;", "'").replaceAll("&rsquo;", "'").replaceAll("&ldquo;", "\"")
				.replaceAll("&rdquo;", "\"").replaceAll("&nbsp;", " ").replaceAll("&mdash;", "—")
				.replaceAll("&ndash;", "–").replaceAll("<ul>", "'").replaceAll("<li>", "'").replaceAll("</ul>", "'")
				.replaceAll("</li>", "'");
	}

	/**
	 * Método que crea la carpeta "portadas" para almacenar las imágenes de portada
	 * de los cómics.
	 * 
	 * @throws IOException Si ocurre un error al crear la carpeta.
	 */
	public static void crearCarpeta() throws IOException {
		String userDir = System.getProperty("user.home");
		String documentsPath = userDir + File.separator + "Documents";
		String defaultImagePath = documentsPath + File.separator + "libreria_comics" + File.separator
				+ obtenerDatoDespuesDeDosPuntos("Database") + File.separator + "portadas";
		File portadasFolder = new File(defaultImagePath);

		if (!portadasFolder.exists()) {
			if (!portadasFolder.mkdirs()) {
				throw new IOException("No se pudo crear la carpeta 'portadas'");
			}
		}
	}

	/**
	 * Obtiene el dato que sigue a dos puntos (:) en una línea específica del
	 * archivo de configuración.
	 *
	 * @param linea la línea específica para buscar el dato
	 * @return el dato encontrado o una cadena vacía si no se encuentra
	 */
	public static String obtenerDatoDespuesDeDosPuntos(String linea) {
		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + "\\AppData\\Roaming";
		String carpetaLibreria = ubicacion + "\\libreria";
		String archivoConfiguracion;

		archivoConfiguracion = carpetaLibreria + "\\configuracion_local.conf";

		try (BufferedReader reader = new BufferedReader(new FileReader(archivoConfiguracion))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(linea + ": ")) {
					return line.substring(linea.length() + 2).trim();
				}
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}
		return "";
	}

	/**
	 * Funcion que abre una ventana que aceptara los formatos de archivos que le
	 * demos como parametro.
	 *
	 * @param frase   Descripción del filtro de archivo (p. ej., "Archivos CSV")
	 * @param formato Extensiones de archivo permitidas (p. ej., "*.csv")
	 * @return FileChooser si el usuario selecciona un fichero; null si el usuario
	 *         cancela la selección o cierra la ventana
	 */
	public static FileChooser tratarFichero(String frase, String formato) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(frase, formato));

		return fileChooser; // Devuelve el FileChooser para que la interfaz gráfica lo utilice
	}

	/**
	 * Imprime un mensaje de error en un archivo, junto con la fecha y hora actual.
	 * Abre el archivo después de escribir en él.
	 * 
	 * @param mensajeError     Mensaje de error a imprimir.
	 * @param ubicacionArchivo Ruta del directorio donde se guardará el archivo.
	 */
	public static void imprimirEnArchivo(String mensajeError, String ubicacionArchivo) {

		// Obtener la fecha y hora actuales
		LocalDateTime now = LocalDateTime.now();

		// Formatear la fecha y hora
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm");

		// Mostrar la información
		String fechaFormateada = now.format(formatter);

		String direccionCompleta = ubicacionArchivo + File.separator + "CodigosFaltantes_" + fechaFormateada + ".txt";

		try {
			PrintWriter escritor = new PrintWriter(new FileWriter(direccionCompleta, true));
			escritor.println(mensajeError);
			escritor.close();
			// Abrir el archivo
			abrirArchivo(direccionCompleta);
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Abre un archivo en el sistema por su ubicación.
	 * 
	 * @param ubicacionArchivo Ruta del archivo que se desea abrir.
	 */
	private static void abrirArchivo(String ubicacionArchivo) {
		try {
			File archivo = new File(ubicacionArchivo);

			if (!Desktop.isDesktopSupported()) {
				return;
			}
			Desktop desktop = Desktop.getDesktop();

			if (archivo.exists()) {
				desktop.open(archivo);
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Elimina archivos en un directorio común que no están presentes en la lista
	 * proporcionada de URLs.
	 * 
	 * @param listaUrls Lista de URLs que representan los archivos a conservar.
	 */
	public static void borrarArchivosNoEnLista(List<String> listaUrls) {
		String directorioComun = DOCUMENTS_PATH + File.separator + "libreria_comics" + File.separator
				+ DBManager.DB_NAME + File.separator + "portadas" + File.separator;

		List<String> nombresArchivosEnDirectorio = obtenerNombresArchivosEnDirectorio(directorioComun);

		for (String nombreArchivo : nombresArchivosEnDirectorio) {
			Path archivoAEliminarPath = Paths.get(nombreArchivo).normalize();

			if (!listaUrls.stream().anyMatch(url -> url.equalsIgnoreCase(archivoAEliminarPath.toString()))) {
				try {
					if (archivoAEliminarPath.toFile().exists() && archivoAEliminarPath.toFile().isFile()) {
						archivoAEliminarPath.toFile().delete();
					}
				} catch (SecurityException e) {
					manejarExcepcion(e);
				}
			}
		}
	}

	/**
	 * Obtiene la lista de nombres de archivos en un directorio especificado.
	 * 
	 * @param directorio La ruta del directorio.
	 * @return Lista de nombres de archivos en el directorio.
	 */
	private static List<String> obtenerNombresArchivosEnDirectorio(String directorio) {
		List<String> nombresArchivos = new ArrayList<>();

		File directorioComun = new File(directorio);
		File[] archivosEnDirectorio = directorioComun.listFiles();

		if (archivosEnDirectorio != null) {
			for (File archivo : archivosEnDirectorio) {
				nombresArchivos.add(directorio + archivo.getName());
			}
		}

		return nombresArchivos;
	}

	/**
	 * Obtiene la lista de nombres de archivos con extensiones de una lista de URLs.
	 * 
	 * @param listaUrls Lista de URLs que representan archivos.
	 * @return Lista de nombres de archivos con extensiones.
	 */
	public static List<String> obtenerNombresYExtensiones(List<String> listaUrls) {
		List<String> nombresYExtensiones = new ArrayList<>();

		for (String url : listaUrls) {
			File archivo = new File(url);
			String nombreArchivo = archivo.getName();
			nombresYExtensiones.add(nombreArchivo);
		}

		return nombresYExtensiones;
	}

	/**
	 * Parsea la cadena de fecha y devuelve la fecha correspondiente. Si la cadena
	 * es nula o vacía, devuelve la fecha actual.
	 *
	 * @param fechaVenta Cadena de fecha a ser parseada.
	 * @return Objeto LocalDate que representa la fecha parseada.
	 */
	public static LocalDate parseFecha(String fechaVenta) {
		if (fechaVenta == null || fechaVenta.isEmpty()) {
			return LocalDate.of(2000, 1, 1); // Obtener la fecha actual si la cadena de fecha no está presente
		} else {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				return LocalDate.parse(fechaVenta, formatter);
			} catch (DateTimeParseException e) {
				// Manejar la excepción de formato de fecha, imprimir o registrar según sea
				// necesario
				manejarExcepcion(e);
				return LocalDate.of(2000, 1, 1); // Valor predeterminado en caso de error de formato
			}
		}
	}

	/**
	 * Obtiene la dirección de la portada de un cómic.
	 *
	 * @param direccionPortada La dirección de la portada del cómic.
	 * @return La dirección de la portada actualizada después de procesar la lógica.
	 */
	public static String obtenerPortada(String direccionPortada) {
		String portada = "";
		File file;

		if (!direccionPortada.isEmpty() || direccionPortada == null) {
			file = new File(direccionPortada);
			if (Utilidades.isImageURL(direccionPortada)) {
				// Es una URL en internet
				CompletableFuture<String> futurePortada = descargarImagenAsync(direccionPortada, DOCUMENTS_PATH);
				// Esperar a que el CompletableFuture se complete y obtener el resultado
				portada = futurePortada.join();
				file = new File(portada);
			} else if (!file.exists()) {
				portada = new File("Funcionamiento/sinPortada.jpg").toURI().toString();
			} else {
				portada = file.toURI().toString();
			}
		} else {
			portada = new File("Funcionamiento/sinPortada.jpg").toURI().toString();
		}

		// Realizar cualquier operación adicional si es necesario

		return portada;
	}

	/**
	 * Busca un cómic por su ID en una lista de cómics.
	 *
	 * @param comics  La lista de cómics en la que se realizará la búsqueda.
	 * @param idComic La ID del cómic que se está buscando.
	 * @return El cómic encontrado por la ID, o null si no se encuentra ninguno.
	 */
	public static Comic buscarComicPorID(List<Comic> comics, String idComic) {
		for (Comic c : comics) {
			if (c.getID().equals(idComic)) {
				return c; // Devuelve el cómic si encuentra la coincidencia por ID
			}
		}
		return null; // Retorna null si no se encuentra ningún cómic con la ID especificada
	}

	/**
	 * Agrega una etiqueta y un valor al constructor StringBuilder si el valor no
	 * está vacío o nulo.
	 *
	 * @param builder El constructor StringBuilder al que se va a agregar la
	 *                etiqueta y el valor.
	 * @param label   La etiqueta que se va a agregar.
	 * @param value   El valor que se va a agregar.
	 */
	public static void appendIfNotEmpty(StringBuilder builder, String label, String value) {
		if (value != null && !value.isEmpty()) {
			builder.append(label).append(": ").append(value).append("\n");
		}
	}

	/**
	 * Devuelve el valor predeterminado si la cadena dada es nula o vacía, de lo
	 * contrario, devuelve la cadena original.
	 *
	 * @param value        Cadena a ser verificada.
	 * @param defaultValue Valor predeterminado a ser devuelto si la cadena es nula
	 *                     o vacía.
	 * @return Cadena original o valor predeterminado.
	 */
	public static String defaultIfNullOrEmpty(String value, String defaultValue) {
		return (value == null || value.isEmpty()) ? defaultValue : value;
	}

	/**
	 * Convierte una ruta de archivo a una URL válida.
	 * 
	 * @param rutaArchivo La ruta del archivo a convertir.
	 * @return La URL generada a partir de la ruta del archivo.
	 */
	public static String convertirRutaAURL(String rutaArchivo) {
		String rutaConBarrasInclinadas = "";

		if (rutaArchivo == null || rutaArchivo.isEmpty()) {
			return null;
		} else {
			rutaConBarrasInclinadas = "file:///" + rutaArchivo.replace("\\", "/");
		}
		return rutaConBarrasInclinadas;
	}

	/**
	 * Devuelve un objeto Comic correspondiente al ID proporcionado.
	 * 
	 * @param id_comic El ID del cómic a buscar.
	 * @return El objeto Comic correspondiente al ID proporcionado. Si no se
	 *         encuentra, devuelve null.
	 */
	public static Comic devolverComic(String id_comic) {
		for (Comic comic : VentanaAccionController.comicsImportados) {
			if (comic.getID().equals(id_comic)) {
				return comic;
			}
		}
		// Si no se encuentra el cómic con el ID proporcionado, devolver null
		return null;
	}

	/**
	 * Realiza la comprobación previa para determinar si la lista de cómics está
	 * vacía. En caso afirmativo, se inicializa la librería.
	 */
	public static void comprobacionListaComics() {

		DBLibreriaManager libreria = new DBLibreriaManager();

		if (DBLibreriaManager.listaComics.isEmpty()) {
			return;
		}

		libreria = new DBLibreriaManager();
		libreria.buscarEnLibreria(TipoBusqueda.COMPLETA);
	}

	/**
	 * Obtiene el ID del cómic seleccionado desde la tabla.
	 * 
	 * @return El ID del cómic seleccionado o null si no hay selección.
	 */
	public static String obtenerIdComicSeleccionado(TableView<Comic> tablaBBDD) {
		Comic idRow = tablaBBDD.getSelectionModel().getSelectedItem();
		return (idRow != null) ? idRow.getID() : null;
	}

	/**
	 * Obtiene el objeto Comic seleccionado según el ID proporcionado.
	 * 
	 * @param id_comic El ID del cómic a obtener.
	 * @return El objeto Comic correspondiente al ID proporcionado.
	 * @throws SQLException Si hay un error al acceder a la base de datos.
	 */
	public static Comic obtenerComicSeleccionado(String id_comic) throws SQLException {
		Comic comic_temp;
		DBLibreriaManager libreria = new DBLibreriaManager();
		if (!VentanaAccionController.comicsImportados.isEmpty()) {
			VentanaAccionController.id_comic_selecionado = id_comic;
			comic_temp = Utilidades.devolverComic(id_comic);
		} else {
			comic_temp = libreria.comicDatos(id_comic);
		}

		return comic_temp;
	}

	public static Map<String, String> devolverDatosConfig() {
		Map<String, String> datosConfiguracion = new HashMap<>();

		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
		String carpetaLibreria = ubicacion + File.separator + "libreria";
		String archivoConfiguracion = carpetaLibreria + File.separator + "configuracion_local.conf";

		try (BufferedReader reader = new BufferedReader(new FileReader(archivoConfiguracion))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Usuario: ")) {
					datosConfiguracion.put("Usuario", defaultIfNullOrEmpty(line.substring("Usuario: ".length()), ""));
				} else if (line.startsWith("Password: ")) {
					datosConfiguracion.put("Password", defaultIfNullOrEmpty(line.substring("Password: ".length()), ""));
				} else if (line.startsWith("Puerto: ")) {
					datosConfiguracion.put("Puerto", defaultIfNullOrEmpty(line.substring("Puerto: ".length()), ""));
				} else if (line.startsWith("Database: ")) {
					datosConfiguracion.put("Database", defaultIfNullOrEmpty(line.substring("Database: ".length()), ""));
				} else if (line.startsWith("Hosting: ")) {
					datosConfiguracion.put("Hosting", defaultIfNullOrEmpty(line.substring("Hosting: ".length()), ""));
				}
			}
		} catch (IOException e) {
			manejarExcepcion(e);
		}

		return datosConfiguracion;
	}

	/**
	 * Guarda los datos de configuración de la base de datos local en un archivo de
	 * configuración.
	 *
	 * @throws SQLException Si ocurre un error de SQL.
	 */
	public static void guardarDatosBaseLocal(String[] datos, Label prontEstadoFichero, Label alarmaConexion)
			throws SQLException {

		String puertobbdd = datos[0];
		String nombreBBDD = datos[1];
		String usuario = datos[2];
		String pass = datos[3];
		String nombreHost = datos[4];

		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
		String carpetaLibreria = ubicacion + File.separator + "libreria";
		String carpetaBackup = carpetaLibreria + File.separator + nombreBBDD + File.separator + "backups";
		String archivoConfiguracion = carpetaLibreria + File.separator + "configuracion_local.conf";
		AlarmaList alarmaList = new AlarmaList();

		try {
			if (verificarDatos(datos, prontEstadoFichero)) {
				Utilidades.crearEstructura();

				// Utilizamos try-with-resources para asegurar el cierre de recursos
				try (FileWriter fileWriter = new FileWriter(archivoConfiguracion);
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

					bufferedWriter.write("###############################");
					bufferedWriter.newLine();
					bufferedWriter.write("Fichero de configuracion de la libreria");
					bufferedWriter.newLine();
					bufferedWriter.write("###############################");
					bufferedWriter.newLine();
					bufferedWriter.write("Usuario: " + usuario);
					bufferedWriter.newLine();
					bufferedWriter.write("Password: " + pass);
					bufferedWriter.newLine();
					bufferedWriter.write("Puerto: " + puertobbdd);
					bufferedWriter.newLine();
					bufferedWriter.write("Database: " + nombreBBDD);
					bufferedWriter.newLine();
					bufferedWriter.write("Hosting: " + nombreHost);
					bufferedWriter.newLine();
				}

				// Crear carpeta de backups si no existe
				File carpetaBackupsFile = new File(carpetaBackup);
				if (!carpetaBackupsFile.exists()) {
					carpetaBackupsFile.mkdirs(); // Usa mkdirs para crear directorios recursivamente
				}

				alarmaList.mensajeRespuestaGuardado(prontEstadoFichero, alarmaConexion);

			} else {
				alarmaList.mensajeRespuestaError(prontEstadoFichero, alarmaConexion);
			}

		} catch (IOException e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Comprueba si los datos ingresados coinciden con los datos en la base de
	 * datos.
	 *
	 * @return true si los datos coinciden, false si no coinciden o si hay un error
	 *         en la conexión
	 * @throws SQLException si hay un error al consultar la base de datos
	 */
	private static boolean verificarDatos(String[] datos, Label prontEstadoFichero) throws SQLException {

		DBManager.datosBBDD(datos);
		AlarmaList alarmaList = new AlarmaList();
		Connection connection = DBManager.conexion();
		if (connection == null) {
			prontEstadoFichero.setStyle("-fx-background-color: #DD370F");
			alarmaList.iniciarAnimacionBBDDError(prontEstadoFichero);
			return false;
		}

		if (JDBC.DBManager.isConnected()) {
			DBManager.close();
			return true;
		} else {
			prontEstadoFichero.setStyle("-fx-background-color: #DD370F");
			alarmaList.iniciarAnimacionBBDDError(prontEstadoFichero);
			return false;
		}
	}

	/**
	 * Verifica si el servicio MySQL está en ejecución en el host y puerto
	 * proporcionados.
	 *
	 * @param host       El host del servicio MySQL.
	 * @param portString El número de puerto del servicio MySQL.
	 * @return true si el servicio está en ejecución, false si no lo está.
	 */
	public static boolean isMySQLServiceRunning(String host, String portString) {
		try {
			int port = Integer.parseInt(portString); // Convertir la cadena a un entero
			InetAddress address = InetAddress.getByName(host);
			Socket socket = new Socket(address, port);
			socket.close();
			return true;
		} catch (Exception e) {
//			manejarExcepcion(e);
			return false;
		}
	}

	/**
	 * Crea las carpetas necesarias para realizar backups.
	 */
	public static void crearCarpetasBackup() {
		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
		String carpetaLibreria = ubicacion + File.separator + "libreria";
		String carpetaBackup = carpetaLibreria + File.separator + Utilidades.obtenerDatoDespuesDeDosPuntos("Database")
				+ File.separator + "backups";

		try {
			File carpeta_backupsFile = new File(carpetaBackup);
			Utilidades.crearCarpeta();
			if (!carpeta_backupsFile.exists()) {
				carpeta_backupsFile.mkdirs();
			}
		} catch (Exception e) {
			manejarExcepcion(e);
		}
	}

	/**
	 * Método que crea la estructura de carpetas y archivos necesarios para la
	 * librería.
	 */
	public static void crearEstructura() {
		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
		String carpetaLibreria = ubicacion + File.separator + "libreria";

		// Verificar y crear la carpeta "libreria" si no existe
		File carpetaLibreriaFile = new File(carpetaLibreria);
		if (!carpetaLibreriaFile.exists()) {
			carpetaLibreriaFile.mkdir();
			carpetaLibreriaFile.setWritable(true);
		}

		// Verificar y crear los archivos de configuración si no existen
		String archivoConfiguracionLocal = carpetaLibreria + File.separator + "configuracion_local.conf";
		String archivoConfiguracionOnline = carpetaLibreria + File.separator + "configuracion_usuario.conf";

		File archivoConfiguracionLocalFile = new File(archivoConfiguracionLocal);
		File archivoConfiguracionOnlineFile = new File(archivoConfiguracionOnline);

		if (!archivoConfiguracionLocalFile.exists()) {
			try {
				archivoConfiguracionLocalFile.createNewFile();

				// Escribir líneas en el archivo de configuración local
				FileWriter fileWriterLocal = new FileWriter(archivoConfiguracionLocalFile);
				BufferedWriter bufferedWriterLocal = new BufferedWriter(fileWriterLocal);
				bufferedWriterLocal.write("###############################");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("Fichero de configuracion local de la libreria");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("###############################");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("Usuario:");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("Password:");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("Puerto:");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("Database:");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.write("Hosting: Localhost");
				bufferedWriterLocal.newLine();
				bufferedWriterLocal.close();
			} catch (IOException e) {
				manejarExcepcion(e);
			}
		}

		if (!archivoConfiguracionOnlineFile.exists()) {
			try {
				archivoConfiguracionOnlineFile.createNewFile();

				// Escribir líneas en el archivo de configuración online
				FileWriter fileWriterOnline = new FileWriter(archivoConfiguracionOnlineFile);
				BufferedWriter bufferedWriterOnline = new BufferedWriter(fileWriterOnline);
				bufferedWriterOnline.write("###############################");
				bufferedWriterOnline.newLine();
				bufferedWriterOnline.write("Usuario y contraseño del usuario");
				bufferedWriterOnline.newLine();
				bufferedWriterOnline.write("###############################");
				bufferedWriterOnline.newLine();
				bufferedWriterOnline.write("Usuario: ");
				bufferedWriterOnline.newLine();
				bufferedWriterOnline.write("Password: ");
				bufferedWriterOnline.newLine();
				bufferedWriterOnline.close();
			} catch (IOException e) {
				manejarExcepcion(e);
			}
		}
	}

	/**
	 * Valida los datos de conexión a la base de datos MySQL.
	 *
	 * @param usuario  El nombre de usuario para la conexión.
	 * @param password La contraseña para la conexión.
	 * @param puerto   El puerto para la conexión.
	 * @param hosting  El host para la conexión.
	 * @return true si la validación fue exitosa, false si no lo fue.
	 */
	public static boolean validarDatosConexion() {

		String[] datosFichero = datosEnvioFichero();

		String puerto = datosFichero[0];
		String usuario = datosFichero[2];
		String password = datosFichero[3];
		String hosting = datosFichero[4];

		String url = construirURL(hosting, puerto);
		if (Utilidades.isMySQLServiceRunning(hosting, puerto)) {
			try (Connection connection = DriverManager.getConnection(url, usuario, password)) {

				return true; // La conexión se estableció correctamente
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			DBManager.asignarValoresPorDefecto();
			DBManager.close();
		}

		return false; // La conexión no se pudo establecer
	}

	private static String construirURL(String hosting, String puerto) {
		return "jdbc:mysql://" + hosting + ":" + puerto + "/";
	}

	/**
	 * Obtiene opciones para el ComboBox de nombreBBDD basado en la configuración.
	 *
	 * @param usuario  El nombre de usuario para la conexión.
	 * @param password La contraseña para la conexión.
	 * @param puerto   El puerto para la conexión.
	 * @param hosting  El host para la conexión.
	 * @return Lista de opciones para el ComboBox de nombreBBDD.
	 */
	public static List<String> obtenerOpcionesNombreBBDD(String usuario, String password, String puerto,
			String hosting) {
		List<String> opciones = new ArrayList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + hosting + ":" + puerto + "/",
				usuario, password);
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SHOW DATABASES");) {

			// Agregar los nombres de las bases de datos a la lista de opciones
			while (resultSet.next()) {
				String nombreBD = resultSet.getString(1);
				String urlBD = "jdbc:mysql://" + hosting + ":" + puerto + "/" + nombreBD;
				try (Connection dbConnection = DriverManager.getConnection(urlBD, usuario, password);
						Statement dbStatement = dbConnection.createStatement();
						ResultSet dbResultSet = dbStatement.executeQuery("SHOW TABLES LIKE 'comicsbbdd'");) {
					if (dbResultSet.next()) {
						opciones.add(nombreBD);
					}
				}
			}
		} catch (SQLException e) {
			manejarExcepcion(e);
		}
		return opciones;
	}

	public static boolean validarConexionMySQL(String puertoTexto, String hostingTexto) {
		return isMySQLServiceRunning(hostingTexto, puertoTexto) && validarDatosConexion();
	}

	/**
	 * Verifica si el código es válido, es decir, contiene solo números y letras y
	 * tiene una longitud mayor a 0.
	 *
	 * @param codigo El código a validar.
	 * @return true si el código es válido, false de lo contrario.
	 */
	public static boolean esCodigoValido(String codigo) {
		return codigo.length() >= 8 && codigo.matches("^[a-zA-Z0-9]+$");
	}

	public static void saveImageFromDataBase(String direccionImagen, File directorio) {

		try (Connection conn = DBManager.conexion()) {
			if (directorio != null) {
				String nombreImagen = obtenerNombreArchivo(direccionImagen);
				File imagenArchivo = new File(direccionImagen);

				if (!imagenArchivo.exists()) {
					copiarImagenPredeterminada(directorio, nombreImagen);
				} else {
					copiarImagenDesdeArchivo(imagenArchivo, directorio, nombreImagen);
				}
			}
		} catch (IOException | SQLException e) {
			manejarExcepcion(e);
		}
	}

	// OJO CON ESTA FUNCION, EL ERROR FUE PONER STATIC EN VEZ DE getClass() DONDE
	// UTILIDADES.CLASS

	private static void copiarImagenPredeterminada(File directorio, String nombreImagen) throws IOException {
		try (InputStream input = Utilidades.class.getResourceAsStream("sinPortada.jpg")) {
			if (input == null) {
				throw new FileNotFoundException("La imagen predeterminada no se encontró en el paquete");
			}

			File imagenArchivo = File.createTempFile("tmp", ".jpg");
			imagenArchivo.deleteOnExit();

			Path tempPath = imagenArchivo.toPath();
			Files.copy(input, tempPath, StandardCopyOption.REPLACE_EXISTING);

			Files.copy(tempPath, Paths.get(directorio.getAbsolutePath(), nombreImagen),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void copiarImagenDesdeArchivo(File imagenArchivo, File directorio, String nombreImagen)
			throws IOException {
		try (InputStream fileInputStream = new FileInputStream(imagenArchivo);
				OutputStream fileOutputStream = new FileOutputStream(
						Paths.get(directorio.getAbsolutePath(), nombreImagen).toFile())) {

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
		}
	}

	public static void manejarExcepcion(Exception e) {

		Ventanas nav = new Ventanas();

		nav.alertaException(e.toString());
		e.printStackTrace();
	}

	public static void procesarArchivo(File archivo) {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.submit(() -> {
			String linea;
			int contadorFaltas = 0;
			ApiISBNGeneral isbnGeneral = new ApiISBNGeneral();
			WebScraperPreviewsWorld previewsScraper = new WebScraperPreviewsWorld();
			String codigosFaltantes = "";
			Comic comicInfo = new Comic();
			String sourcePath = DOCUMENTS_PATH + File.separator + "libreria_comics" + File.separator
					+ DBManager.DB_NAME;

			try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
				while ((linea = br.readLine()) != null) {
					// Procesar cada línea según tus requisitos
					String finalValorCodigo = linea.trim();
					String valorCodigo = finalValorCodigo; // Asignar el valor adecuado

					if (!finalValorCodigo.isEmpty()) {
						if (finalValorCodigo.length() == 9) {
							comicInfo = previewsScraper.displayComicInfo(finalValorCodigo.trim(), null);

						} else {
							comicInfo = ApiMarvel.infoComicCode(finalValorCodigo.trim(), null);
							contadorFaltas++;

							if (comicInfo == null) {
								comicInfo = isbnGeneral.getBookInfo(finalValorCodigo.trim(), null);

								if (comicInfo == null) {
									contadorFaltas++;
								}

							}
						}

						if (contadorFaltas > 1) {
							codigosFaltantes += "Falta comic con codigo: " + valorCodigo;
						}
					}
				}
			} catch (IOException | URISyntaxException | JSONException e) {
				e.printStackTrace();
			}

			// Verificar el contador de faltas al final y actualizar el resultado
			if (contadorFaltas > 1) {
				Utilidades.imprimirEnArchivo(codigosFaltantes, sourcePath);
			}
		});

		executor.shutdown();
	}

	public static boolean codigoCorrectoImportado(String valorCodigo) {
		try {
			String finalValorCodigo = eliminarEspacios(valorCodigo).replace("-", "");
			ApiISBNGeneral isbnGeneral = new ApiISBNGeneral();
			WebScraperPreviewsWorld previewsScraper = new WebScraperPreviewsWorld();
			Comic comicInfo = null;

			if (finalValorCodigo.length() == 9) {
				comicInfo = previewsScraper.displayComicInfo(finalValorCodigo.trim(), null);
			} else {
				comicInfo = ApiMarvel.infoComicCode(finalValorCodigo.trim(), null);

				if (comicInfo == null) {
					comicInfo = isbnGeneral.getBookInfo(finalValorCodigo.trim(), null);
				}
			}

			// Realiza cualquier lógica adicional aquí si es necesario.

			return comicInfo != null; // Devuelve true si se encontró información del cómic.
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int contarLineas(File fichero) {
		int contador = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(fichero))) {
			// Lee el archivo línea por línea
			while (br.readLine() != null) {
				contador++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contador;
	}

	public static Image pasarImagenComic(String direccionComic) {
		if (direccionComic != null && !direccionComic.isEmpty()) {
			try {
				Image imagen = new Image(new File(direccionComic).toURI().toString());
				return new Image(imagen.getUrl(), 250, 0, true, true);
			} catch (Exception e) {
				Utilidades.manejarExcepcion(e);
			}
		}
		return null;
	}

	public static String[] datosEnvioFichero() {
		Map<String, String> datosConfiguracion = Utilidades.devolverDatosConfig();

		String puertoTexto = datosConfiguracion.get("Puerto");
		String databaseTexto = datosConfiguracion.get("Database");
		String usuarioTexto = datosConfiguracion.get("Usuario");
		String passwordTexto = datosConfiguracion.get("Password");
		String hostingTexto = datosConfiguracion.get("Hosting");

		String[] datosConfiguracionArray = { puertoTexto, databaseTexto, usuarioTexto, passwordTexto, hostingTexto };

		return datosConfiguracionArray;
	}

	public static boolean comprobarConexionContante(Stage myStage) {

		if (!validarDatosConexion()) {
			Platform.runLater(() -> {
				DBManager.close();
				Ventanas.cerrarVentanaActual(myStage);
			});
			return true;
		}
		return false;
	}

	public static boolean comprobarConexion(Scene ventana) {
		Ventanas nav = new Ventanas();
		if (ventana != null) {
			Stage stage = (Stage) ventana.getWindow();

			if (Utilidades.comprobarConexionContante(stage)) {
				Platform.runLater(() -> {
					nav.verAccesoBBDD();
					nav.alertaException("Error. Servicio MySql apagado o desconectado de forma repentina.");

				});

				return false; // No está conectado
			}
		}
		return true; // Está conectado
	}

}
