package funcionesManagment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import Controladores.CargaComicsController;
import alarmas.AlarmaList;
import comicManagement.Comic;
import dbmanager.ComicManagerDAO;
import dbmanager.DatabaseManagerDAO;
import dbmanager.ListasComicsDAO;
import dbmanager.UpdateManager;
import funcionesAuxiliares.Utilidades;
import funcionesAuxiliares.Ventanas;
import funcionesInterfaz.AccionControlUI;
import funcionesInterfaz.FuncionesComboBox;
import funcionesInterfaz.FuncionesManejoFront;
import funcionesInterfaz.FuncionesTableView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import webScrap.WebScrapGoogleLeagueOfComics;
import webScrap.WebScraperMarvelComic;
import webScrap.WebScraperPaniniComics;
import webScrap.WebScraperPreviewsWorld;

public class AccionFuncionesComunes {

	private static File fichero;
	private static AtomicInteger contadorErrores;
	private static AtomicInteger comicsProcesados;
	private static AtomicInteger mensajeIdCounter;
	private static AtomicInteger numLineas;
	private static AtomicReference<CargaComicsController> cargaComicsControllerRef;
	private static StringBuilder codigoFaltante;
	private static HashSet<String> mensajesUnicos = new HashSet<>();

	/**
	 * Obtenemos el directorio de inicio del usuario
	 */
	private static final String USER_DIR = System.getProperty("user.home");

	/**
	 * Construimos la ruta al directorio "Documents"
	 */
	private static final String DOCUMENTS_PATH = USER_DIR + File.separator + "Documents";

	/**
	 * Instancia de la clase Ventanas para la navegación.
	 */
	private static Ventanas nav = new Ventanas();

	/**
	 * Instancia de la clase FuncionesComboBox para el manejo de ComboBox.
	 */
	private static FuncionesComboBox funcionesCombo = new FuncionesComboBox();

	public static String TIPO_ACCION = getTipoAccion();

	private static AccionReferencias referenciaVentana = getReferenciaVentana();

	private static AccionReferencias referenciaVentanaPrincipal = getReferenciaVentanaPrincipal();

	private static AccionControlUI accionRellenoDatos = new AccionControlUI();

	/**
	 * Procesa la información de un cómic, ya sea para realizar una modificación o
	 * una inserción en la base de datos.
	 *
	 * @param comic          El cómic con la información a procesar.
	 * @param esModificacion Indica si se está realizando una modificación (true) o
	 *                       una inserción (false).
	 * @throws Exception
	 */
	public void procesarComic(Comic comic, boolean esModificacion) {
		getReferenciaVentana().getProntInfoTextArea().setOpacity(1);

		if (!accionRellenoDatos.camposComicSonValidos()) {
			mostrarErrorDatosIncorrectos();
			return;
		}

		String codigoImagen = Utilidades.generarCodigoUnico(carpetaPortadas(Utilidades.nombreDB()) + File.separator);
		String mensaje = "";

		try {
			Utilidades.redimensionarYGuardarImagen(comic.getDireccionImagenComic(), codigoImagen);
			comic.setDireccionImagenComic(
					carpetaPortadas(Utilidades.nombreDB()) + File.separator + codigoImagen + ".jpg");

			if (esModificacion) {
				ComicManagerDAO.actualizarComicBBDD(comic, "modificar");
				mensaje = "Has modificado correctamente el cómic";
			} else {
				procesarNuevaComic(comic);
				mensaje = "Has introducido correctamente el cómic";
			}

			AlarmaList.mostrarMensajePront(mensaje, esModificacion, getReferenciaVentana().getProntInfoTextArea());
			procesarBloqueComun(comic);

		} catch (IOException | SQLException e) {
			Utilidades.manejarExcepcion(e);
		}
	}

	private void mostrarErrorDatosIncorrectos() {
		String mensaje = "Error. Debes de introducir los datos correctos";
		AlarmaList.mostrarMensajePront(mensaje, false, getReferenciaVentana().getProntInfoTextArea());
		List<Comic> comicsFinal = ListasComicsDAO.comicsImportados;
		Platform.runLater(() -> FuncionesTableView.tablaBBDD(comicsFinal));
	}

	private void procesarNuevaComic(Comic comic) {
		ComicManagerDAO.insertarDatos(comic, true);
		Comic newSelection = getReferenciaVentana().getTablaBBDD().getSelectionModel().getSelectedItem();
		List<Comic> comicsFinal;

		if (newSelection != null) {
			String idComicGradeo = newSelection.getIdComic();
			ListasComicsDAO.comicsImportados.removeIf(c -> c.getIdComic().equals(idComicGradeo));
			getReferenciaVentana().getTablaBBDD().getItems().clear();
			comicsFinal = ListasComicsDAO.comicsImportados;
		} else {
			comicsFinal = null; // Inicializar comicsFinal en caso de que no haya ningún cómic seleccionado
		}

		Platform.runLater(() -> FuncionesTableView.tablaBBDD(comicsFinal));
		getReferenciaVentana().getTablaBBDD().refresh();
	}

	/**
	 * Procesa el bloque común utilizado en la función procesarComicGradeo para
	 * actualizar la interfaz gráfica y realizar operaciones relacionadas con la
	 * manipulación de imágenes y la actualización de listas y combos.
	 *
	 * @param comic El objeto ComicGradeo que se está procesando.
	 * @throws SQLException Si ocurre un error al interactuar con la base de datos.
	 */
	private void procesarBloqueComun(Comic comic) {
		File file = new File(comic.getDireccionImagenComic());
		Image imagen = new Image(file.toURI().toString(), 250, 0, true, true);
		getReferenciaVentana().getImagenComic().setImage(imagen);

		ListasComicsDAO.listasAutoCompletado();
		FuncionesTableView.nombreColumnas();
		FuncionesTableView.actualizarBusquedaRaw();
	}

	public static boolean procesarComicPorCodigo(Comic comicInfo, boolean esClonar) {

		boolean alMenosUnoProcesado = false;

		if (comprobarCodigo(comicInfo)) {
			rellenarTablaImport(comicInfo, esClonar);
			alMenosUnoProcesado = true;
		}

		return alMenosUnoProcesado;
	}

	public static void actualizarPortadaComics(String codigoImagen, String correctedUrl) {

		// Asynchronously download and convert image
		Platform.runLater(() -> {
			URI uri = null;
			try {
				uri = new URI(correctedUrl);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			Utilidades.descargarYConvertirImagenAsync(uri, carpetaPortadas(Utilidades.nombreDB()),
					codigoImagen + ".jpg");
		});
	}

	public static void actualizarInformacionComics(Comic comicOriginal) {

		String numComicGradeo = comicOriginal.getNumeroComic();
		String nombreCorregido = Utilidades.eliminarParentesis(comicOriginal.getTituloComic());
		String nombreLimpio = Utilidades.extraerNombreLimpio(nombreCorregido);
		nombreLimpio = DatabaseManagerDAO.corregirPatrones(nombreLimpio);
		String editor = comicOriginal.getEditorComic();

		comicOriginal.setTituloComic(nombreLimpio);
		comicOriginal.setNumeroComic(numComicGradeo);
		comicOriginal.setEditorComic(editor);
		comicOriginal.setDireccionImagenComic(comicOriginal.getDireccionImagenComic());

	}

	public static void actualizarCompletoComics(Comic comicOriginal) {

		String numComicGradeo = comicOriginal.getNumeroComic();
		String nombreCorregido = Utilidades.eliminarParentesis(comicOriginal.getTituloComic());
		String nombreLimpio = Utilidades.extraerNombreLimpio(nombreCorregido);
		nombreLimpio = DatabaseManagerDAO.corregirPatrones(nombreLimpio);
		String editor = (comicOriginal.getEditorComic());

		comicOriginal.setTituloComic(nombreLimpio);
		comicOriginal.setNumeroComic(numComicGradeo);
		comicOriginal.setEditorComic(editor);

	}

	public static String codigoNuevaImagen() {
		return Utilidades.generarCodigoUnico(carpetaPortadas(Utilidades.nombreDB()) + File.separator);
	}

	public static String urlFinalImagen(String direccionImagen) {

		String urlImagen = direccionImagen;

		return urlImagen.replace("\\", "/").replaceFirst("^http:", "https:");
	}

	public static void actualizarComicsDatabase(Comic comicOriginal, String tipoUpdate) {

		String urlRefrencia = comicOriginal.getUrlReferenciaComic();
		String tipoTienda = determinarTipoTienda(urlRefrencia);

		if (urlRefrencia.isEmpty() || urlRefrencia.equals("Sin referencia")) {
			return;
		}

		if (comicOriginal.getCodigoComic().matches("[A-Z]{3}\\d{6}")
				&& !comicOriginal.getUrlReferenciaComic().contains("leagueofcomicgeeks.com")) {
			tipoTienda = "previews world";
			urlRefrencia = comicOriginal.getCodigoComic();
		}

		List<Comic> comicColeccion = obtenerComicInfo(urlRefrencia, true, tipoTienda);

		for (Comic comicInfo : comicColeccion) {
			Comic.cleanString(comicInfo.getTituloComic());
			Comic.cleanString(comicInfo.getArtistaComic());
			Comic.cleanString(comicInfo.getGuionistaComic());
			Comic.cleanString(comicInfo.getVarianteComic());
			Comic.cleanString(comicInfo.getKeyComentarios());

			comicInfo.setIdComic(comicOriginal.getIdComic());
			if (tipoUpdate.equalsIgnoreCase("actualizar datos")) {

				if (!comicOriginal.getFirmaComic().isEmpty()) {
					comicInfo.setFirmaComic(comicOriginal.getFirmaComic());
				} else {
					comicInfo.setUrlReferenciaComic(comicOriginal.getUrlReferenciaComic());
				}
				comicInfo.setDireccionImagenComic(comicOriginal.getDireccionImagenComic());
			} else if (tipoUpdate.equalsIgnoreCase("actualizar portadas")) {

				if (comicOriginal.getFirmaComic().isEmpty()) {
					String url = comicInfo.getDireccionImagenComic();
					comicInfo = comicOriginal;
					comicInfo.setDireccionImagenComic(url);
					String direccionDescarga = descargarImagenComic(comicInfo);
					comicInfo.setDireccionImagenComic(direccionDescarga);
				}

			} else {
				if (!comicOriginal.getFirmaComic().isEmpty()) {
					comicInfo.setFirmaComic(comicOriginal.getFirmaComic());
					comicInfo.setDireccionImagenComic(comicOriginal.getDireccionImagenComic());
				} else {
					String direccionDescarga = descargarImagenComic(comicInfo);
					comicInfo.setDireccionImagenComic(direccionDescarga);
				}

//				comicInfo.setUrlReferenciaComic(comicOriginal.getUrlReferenciaComic());
			}
			UpdateManager.actualizarComicBBDD(comicInfo, "modificar");
		}

	}

	public static String descargarImagenComic(Comic comicInfo) {
		String urlImagen = comicInfo.getDireccionImagenComic();

		File file = new File(urlImagen);
		// Manejo de la ruta de la imagen
		if (urlImagen == null || urlImagen.isEmpty()) {
			String rutaImagen = "/imagenes/sinPortada.jpg";
			URL url = Utilidades.class.getClass().getResource(rutaImagen);
			if (url != null) {
				urlImagen = url.toExternalForm();
			}
		} else {
			file = new File(urlImagen);
			urlImagen = file.toString();
		}

		// Corrección y generación de la URL final de la imagen
		String correctedUrl = urlImagen.replace("\\", "/").replace("http:", "https:").replace("https:", "https:/");
		String codigoImagen = Utilidades.generarCodigoUnico(carpetaPortadas(Utilidades.nombreDB()) + File.separator)
				+ ".jpg";
		String imagen = carpetaPortadas(Utilidades.nombreDB()) + File.separator;
		URI uri = null;
		try {
			uri = new URI(correctedUrl);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Utilidades.descargarYConvertirImagenAsync(uri, imagen, codigoImagen);
		return imagen + codigoImagen;
	}

	public static String determinarTipoTienda(String url) {
		if (url.contains("previewsworld")) {
			return "previews world";
		} else if (url.contains("marvel.com/comics/issue/")) {
			return "marvel";
		} else if (url.contains("leagueofcomicgeeks.com/comic/")) {
			return "league of comics";
		} else if (url.contains("panini.es")) {
			return "panini";
		}
		return "";
	}

	public static Comic copiarComicClon(Comic comicOriginal) {
		try {
			// Clonar la comic original
			Comic comicClon = (Comic) comicOriginal.clone();

			// Obtener la dirección actual de la imagen
			String direccionActual = comicOriginal.getDireccionImagenComic();

			// Verificar si la dirección actual es válida
			if (direccionActual != null && !direccionActual.isEmpty()) {
				File imagenActual = new File(direccionActual);

				// Verificar si el archivo existe antes de proceder
				if (imagenActual.exists()) {
					// Generar un nuevo nombre de archivo y ruta destino
					String codigoNuevoComicGradeo = codigoNuevaImagen();
					String nombreArchivoNuevo = codigoNuevoComicGradeo + ".jpg";
					String carpetaPortadas = carpetaPortadas(Utilidades.nombreDB());
					String urlFinal = carpetaPortadas + File.separator + nombreArchivoNuevo;

					// Crear la ruta destino para el archivo nuevo
					Path rutaDestino = new File(urlFinal).toPath();

					// Copiar y renombrar el archivo de imagen
					Files.copy(imagenActual.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

					// Actualizar la dirección de la imagen en la comic clonada
					comicClon.setDireccionImagenComic(urlFinal);

					// Devolver la comic clonada con la nueva dirección de imagen
					return comicClon;
				}
			}

			// Si la dirección actual no es válida o el archivo no existe, devolver la comic
			// original
			return comicOriginal;
		} catch (CloneNotSupportedException | IOException e) {
			// Manejar la excepción de clonación o de operaciones de archivos
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Funcion que escribe en el TextField de "Direccion de imagen" la dirrecion de
	 * la imagen
	 */
	public static void subirPortada() {

		String frase = "Fichero JPG";

		String formato = "*.jpg";

		File fichero = Utilidades.tratarFichero(frase, formato, false);

		// Verificar si se obtuvo un objeto FileChooser válido
		if (fichero != null) {
			String nuevoNombreArchivo = Utilidades.generarCodigoUnico(carpetaRaizPortadas(Utilidades.nombreDB()));

			try {
				Utilidades.redimensionarYGuardarImagen(fichero.getAbsolutePath(), nuevoNombreArchivo);
			} catch (IOException e) {
				e.printStackTrace();
			}

			String portada = carpetaRaizPortadas(Utilidades.nombreDB()) + "portadas" + File.separator
					+ nuevoNombreArchivo + ".jpg";

			getReferenciaVentana().getDireccionImagenTextField().setText(portada);

			String mensaje = "Portada subida correctamente.";

			AlarmaList.mostrarMensajePront(mensaje, true, getReferenciaVentana().getProntInfoTextArea());

			Utilidades.cargarImagenAsync(portada, getReferenciaVentana().getImagenComic());

		} else {
			String mensaje = "Has cancelado la subida de portada.";

			AlarmaList.mostrarMensajePront(mensaje, false, getReferenciaVentana().getProntInfoTextArea());
		}
	}

	/**
	 * Limpia y restablece todos los campos de datos en la sección de animaciones a
	 * sus valores predeterminados. Además, restablece la imagen de fondo y oculta
	 * cualquier mensaje de error o información.
	 */
	public static void limpiarDatosPantallaAccion() {
		// Restablecer los campos de datos

		if (!ListasComicsDAO.comicsImportados.isEmpty() && nav.alertaBorradoLista()) {
			getReferenciaVentana().getBotonGuardarComic().setVisible(false);
			getReferenciaVentana().getBotonEliminarImportadoComic().setVisible(false);
			ListasComicsDAO.comicsImportados.clear();
		}
		getReferenciaVentana().getTablaBBDD().getItems().clear();
		getReferenciaVentana().getTablaBBDD().refresh();
		getReferenciaVentana().getBotonClonarComic().setVisible(false);
		getReferenciaVentana().getProntInfoTextArea().setOpacity(0);
		getReferenciaVentana().getTablaBBDD().refresh();
		getReferenciaVentana().getBotonEliminarImportadoListaComic().setVisible(false);
		getReferenciaVentana().getBotonGuardarListaComics().setVisible(false);

		getReferenciaVentana().getBotonEliminarImportadoListaComic().setDisable(true);
		getReferenciaVentana().getBotonGuardarListaComics().setDisable(true);
		limpiarDatosComic();
	}

	public static void limpiarDatosComic() {
		// Limpiar campos de texto
		getReferenciaVentana().getTituloComicTextField().setText("");
		getReferenciaVentana().getCodigoComicTextField().setText("");
		getReferenciaVentana().getNombreEditorTextField().setText("");
		getReferenciaVentana().getArtistaComicTextField().setText("");
		getReferenciaVentana().getGuionistaComicTextField().setText("");
		getReferenciaVentana().getVarianteTextField().setText("");
		getReferenciaVentana().getIdComicTratarTextField().setText("");
		getReferenciaVentana().getDireccionImagenTextField().setText("");
		getReferenciaVentana().getUrlReferenciaTextField().setText("");
		getReferenciaVentana().getNumeroComicTextField().setText("");
		getReferenciaVentana().getFirmaComicTextField().setText("");
		getReferenciaVentana().getPrecioComicTextField().setText("");

		// Limpiar el TextArea
		getReferenciaVentana().getKeyComicData().setText(""); // Asegúrate de tener el método get para el TextArea

		// Limpiar el DatePicker
		getReferenciaVentana().getDataPickFechaP().setValue(null); // Asegúrate de tener el método get para el
																	// DatePicker

		// Limpiar la imagen (si es necesario)
		getReferenciaVentana().getImagenComic().setImage(null); // Asegúrate de tener el método get para la imagen

		getReferenciaVentana().getImagenComic().setImage(null);
		if ("modificar".equals(TIPO_ACCION)) {
			AccionControlUI.mostrarOpcion(TIPO_ACCION);
		}

		if ("aniadir".equals(TIPO_ACCION) && getReferenciaVentana().getNombreTiendaCombobox().isVisible()) {
			AccionFuncionesComunes.cambiarVisibilidadAvanzada();
		}

		// Borrar cualquier mensaje de error presente
		borrarErrores();
		AccionControlUI.validarCamposClave(true);
		AccionControlUI.borrarDatosGraficos();
	}

	/**
	 * Comprueba la existencia de un cómic en la base de datos y realiza acciones
	 * dependiendo del resultado.
	 *
	 * @param ID El identificador del cómic a verificar.
	 * @return true si el cómic existe en la base de datos y se realizan las
	 *         acciones correspondientes, false de lo contrario.
	 * @throws SQLException Si ocurre un error al interactuar con la base de datos.
	 */
	public boolean comprobarExistenciaComic(String idComic) {

		// Si el cómic existe en la base de datos
		if (ComicManagerDAO.comprobarIdentificadorComic(idComic)) {
			FuncionesTableView.actualizarBusquedaRaw();
			return true;
		} else { // Si el cómic no existe en la base de datos
			String mensaje = "ERROR. ID desconocido.";
			AlarmaList.mostrarMensajePront(mensaje, false, getReferenciaVentana().getProntInfoTextArea());
			return false;
		}
	}

	/**
	 * Verifica si se ha encontrado información válida para el cómic.
	 *
	 * @param comicInfo Un objeto ComicGradeo con la información del cómic.
	 * @return True si la información es válida y existe; de lo contrario, False.
	 */
	private static boolean comprobarCodigo(Comic comicInfo) {
		return comicInfo != null;
	}

	/**
	 * Rellena los campos de la interfaz gráfica con la información del cómic
	 * proporcionada.
	 *
	 * @param comicInfo Un arreglo de strings con información del cómic.
	 * @throws IOException
	 */
	private static void rellenarTablaImport(Comic comic, boolean esClonar) {
		Platform.runLater(() -> {
			String idComic = "A" + 0 + "" + (ListasComicsDAO.comicsImportados.size() + 1);
			String tituloComic = Utilidades.defaultIfNullOrEmpty(comic.getTituloComic(), "Vacio");
			String codigoComic = Utilidades.defaultIfNullOrEmpty(comic.getCodigoComic(), "0");
			String numeroComic = Utilidades.defaultIfNullOrEmpty(comic.getNumeroComic(), "0");
			String fechaGradeo = Utilidades.defaultIfNullOrEmpty(comic.getFechaGradeo(), "2000-01-01");
			String editorComic = Utilidades.defaultIfNullOrEmpty(comic.getEditorComic(), "Vacio");
			String keyComentarios = Utilidades.defaultIfNullOrEmpty(comic.getKeyComentarios(), "Vacio");
			String artistaComic = Utilidades.defaultIfNullOrEmpty(comic.getArtistaComic(), "Vacio");
			String guionistaComic = Utilidades.defaultIfNullOrEmpty(comic.getGuionistaComic(), "Vacio");
			String varianteComic = Utilidades.defaultIfNullOrEmpty(comic.getVarianteComic(), "Vacio");
			String direccionImagenComic = Utilidades.defaultIfNullOrEmpty(comic.getDireccionImagenComic(), "Vacio");
			String urlReferenciaComic = Utilidades.defaultIfNullOrEmpty(comic.getUrlReferenciaComic(), "Vacio");
			String valorComic = Utilidades.defaultIfNullOrEmpty(comic.getPrecioComic(), "Vacio");
			String firmaComic = Utilidades.defaultIfNullOrEmpty(comic.getFirmaComic(), "");

			// Variables relacionadas con la imagen del cómic
			String imagen = esClonar ? direccionImagenComic : descargarImagenComic(comic);

			// Construcción del objeto ComicGradeo usando el builder
			Comic comicImport = new Comic.ComicGradeoBuilder(idComic, tituloComic).codigoComic(codigoComic)
					.precioComic(valorComic).numeroComic(numeroComic).fechaGradeo(fechaGradeo).editorComic(editorComic)
					.keyComentarios(keyComentarios).firmaComic(firmaComic).artistaComic(artistaComic)
					.guionistaComic(guionistaComic).varianteComic(varianteComic).direccionImagenComic(imagen)
					.urlReferenciaComic(urlReferenciaComic).build();

			// Añadir el cómic a la lista e actualizar la tabla
			ListasComicsDAO.comicsImportados.add(comicImport);
			FuncionesTableView.nombreColumnas();
			FuncionesTableView.tablaBBDD(ListasComicsDAO.comicsImportados);
		});
	}

	public static List<Comic> obtenerComicInfo(String finalValorCodigo, boolean esImport, String tipoTienda) {
		List<Comic> comicInfo = new ArrayList<>();
		Comic comic = new Comic();
		if (esImport && tipoTienda.equalsIgnoreCase("marvel")) {
			comic = WebScraperMarvelComic.displayComicInfo(finalValorCodigo,
					getReferenciaVentana().getProntInfoTextArea());
		} else if (esImport && tipoTienda.equalsIgnoreCase("league of comics")) {
			comic = WebScrapGoogleLeagueOfComics.obtenerDatosDiv(finalValorCodigo);
		} else if (esImport && tipoTienda.equalsIgnoreCase("previews world")) {
			comic = WebScraperPreviewsWorld.displayComicInfo(finalValorCodigo,
					getReferenciaVentana().getProntInfoTextArea());
		} else if (esImport && tipoTienda.equalsIgnoreCase("panini") || tipoTienda.equalsIgnoreCase("panini comics")) {
			comic = WebScraperPaniniComics.displayComicInfo(finalValorCodigo,
					getReferenciaVentana().getProntInfoTextArea());
		}

		if (comic == null) {
			return new ArrayList<>();
		}

		String keyString = comic.getKeyComentarios().replace(";", "");
		comic.setKeyComentarios(keyString);
		comicInfo.add(comic);

		// Convertir la lista a un Set para eliminar duplicados
		Set<Comic> comicSet = new HashSet<>(comicInfo);
		comicInfo.clear(); // Limpiar la lista original
		comicInfo.addAll(comicSet); // Agregar los elementos únicos de vuelta a la lista
		return comicInfo;
	}

	public static String obtenerNombreTienda(String tipoTienda) {
		if (tipoTienda.toLowerCase().contains("marvel")) {
			return "marvel";
		} else if (tipoTienda.toLowerCase().contains("league of comics")) {
			return "league of comics";
		} else if (tipoTienda.toLowerCase().contains("previews world")) {
			return "previews world";
		} else {
			return "Tienda no reconocida";
		}
	}

	private static void actualizarInterfaz(AtomicInteger contadorErrores, String carpetaDatabase,
			AtomicInteger contadorTotal) {
		Platform.runLater(() -> {
			String mensaje = "";

			if (!carpetaDatabase.isEmpty()) {
				mensaje = "Se han procesado: " + (contadorTotal.get() - contadorErrores.get()) + " de "
						+ contadorTotal.get();
			} else {
				mensaje = "Se han procesado: " + (contadorErrores.get()) + " de " + contadorTotal.get();

			}

			AlarmaList.mostrarMensajePront(mensaje, true, getReferenciaVentana().getProntInfoTextArea());
		});
	}

	// ES OPCIONES
	public static void busquedaPorListaDatabase(List<Comic> listaComicsDatabase, String tipoUpdate) {

		controlCargaComics(listaComicsDatabase.size());

		Task<Void> tarea = createSearchTask(tipoUpdate, listaComicsDatabase, "");

		handleTaskEvents(tarea, tipoUpdate);

		Thread thread = new Thread(tarea);
		thread.setDaemon(true);
		thread.start();
	}

	// ES ACCION
	public static void busquedaPorCodigoImportacion(File file, String tipoTienda) {

		fichero = file;

		int numCargas = Utilidades.contarLineasFichero(fichero);

		controlCargaComics(numCargas);

		Task<Void> tarea = createSearchTask("", null, tipoTienda);

		handleTaskEvents(tarea, "");

		Thread thread = new Thread(tarea);
		thread.setDaemon(true);
		thread.start();
	}

	public static void controlCargaComics(int numCargas) {
		codigoFaltante = new StringBuilder();
		codigoFaltante.setLength(0);
		contadorErrores = new AtomicInteger(0);
		comicsProcesados = new AtomicInteger(0);
		mensajeIdCounter = new AtomicInteger(0);
		numLineas = new AtomicInteger(0);
		numLineas.set(numCargas);
		cargaComicsControllerRef = new AtomicReference<>();
		mensajesUnicos = new HashSet<>();
		mensajesUnicos.clear();
	}

	private static Task<Void> createSearchTask(String tipoUpdate, List<Comic> listaComicsDatabase,
			String tipoTiendaInicial) {
		return new Task<>() {
			@Override
			protected Void call() {
				nav.verCargaComics(cargaComicsControllerRef);

				final String[] tipoTiendaRef = { tipoTiendaInicial };

				if (tipoUpdate.isEmpty()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(fichero))) {

						reader.lines().forEach(linea -> {
							if (isCancelled() || !getReferenciaVentana().getStageVentana().isShowing()) {
								Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics("",
										"100%", 100.0));
								return;
							}

							if (linea.contains("https:") || linea.contains("www.") || linea.contains(".com")) {
								tipoTiendaRef[0] = determinarTipoTienda(linea);
							}
							List<Comic> listaOriginal = obtenerComicInfo(linea, true, tipoTiendaRef[0]);
							for (Comic comic : listaOriginal) {

								if (comic == null) {
									Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics("",
											"100%", 100.0));
									return;
								}

								mensajesCargaComics(comic, tipoUpdate);
								processComic(comic, "");
							}
//							getReferenciaVentana().getTablaBBDD().setOpacity(1);
//							getReferenciaVentana().getTablaBBDD().setDisable(false);
						});

					} catch (IOException e) {
						Utilidades.manejarExcepcion(e);
					}
				} else {
					listaComicsDatabase.forEach(comic -> {
						if (isCancelled() || !getReferenciaVentana().getStageVentana().isShowing()) {
							ListasComicsDAO.eliminarUltimaComicImportada(); // Eliminar la última comic importada
							return; // Salir del forEach si el Task está cancelado
						}
						mensajesCargaComics(comic, tipoUpdate);

						processComic(comic, tipoUpdate);
					});
				}
				return null;
			}
		};
	}

	private static void processComic(Comic comic, String tipoUpdate) {
		if (tipoUpdate.isEmpty() && !comic.getTituloComic().isEmpty()) {

			AccionFuncionesComunes.procesarComicPorCodigo(comic, false);

		} else {
			AccionFuncionesComunes.actualizarComicsDatabase(comic, tipoUpdate);
		}
	}

	private static void mensajesCargaComics(Comic comic, String tipoUpdate) {
		StringBuilder textoBuilder = new StringBuilder();

		if (comic.getTituloComic().isEmpty() || comic.getTituloComic().equalsIgnoreCase("vacio")) {

			if (tipoUpdate.isEmpty()) {
				codigoFaltante.append("URL no valida: ").append(comic.getUrlReferenciaComic()).append("\n");
				textoBuilder.append("URL no valida: ").append(comic.getUrlReferenciaComic()).append("\n");
			} else {
				codigoFaltante.append("ID no procesado: ").append(comic.getIdComic()).append("\n");
				textoBuilder.append("ID no procesado: ").append(comic.getIdComic()).append("\n");
			}
			contadorErrores.getAndIncrement();

		} else {

			if (tipoUpdate.isEmpty()) {
				textoBuilder.append("Codigo: ").append(comic.getTituloComic()).append(" procesado.").append("\n");
			} else if (tipoUpdate.equalsIgnoreCase("actualizar portadas")) {
				textoBuilder.append("Portada ComicGradeo ID: ").append(comic.getIdComic()).append(" actualizado.")
						.append("\n");
			} else {
				textoBuilder.append("ID: ").append(comic.getIdComic()).append(" actualizado.").append("\n");
			}
		}
		updateGUI(textoBuilder);
	}

	private static void updateGUI(StringBuilder textoBuilder) {
		String mensajeId = String.valueOf(mensajeIdCounter.getAndIncrement());

		String mensaje = mensajeId + ": " + textoBuilder.toString();
		mensajesUnicos.add(mensaje);
		comicsProcesados.getAndIncrement();

		long finalProcessedItems = comicsProcesados.get();
		double progress = (double) finalProcessedItems / (numLineas.get());
		String porcentaje = String.format("%.2f%%", progress * 100);

		if (nav.isVentanaCerrada()) {

			nav.verCargaComics(cargaComicsControllerRef);

			StringBuilder textoFiltrado = new StringBuilder();
			List<String> mensajesOrdenados = new ArrayList<>(mensajesUnicos);

			Collections.sort(mensajesOrdenados, Comparator.comparingInt(m -> Integer.parseInt(m.split(":")[0])));
			for (String mensajeUnico : mensajesOrdenados) {
				if (!mensajeUnico.equalsIgnoreCase(mensaje)) {
					textoFiltrado.append(mensajeUnico.substring(mensajeUnico.indexOf(":") + 2));
				}
			}

			Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics(textoFiltrado.toString(),
					porcentaje, progress));
		}
		Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics(textoBuilder.toString(),
				porcentaje, progress));

	}

	public static void cargarRuning() {
		AccionControlUI.limpiarAutorellenos(false);

		cambiarEstadoBotones(true);
		getReferenciaVentana().getImagenComic().setImage(null);
		getReferenciaVentana().getImagenComic().setVisible(true);

		AlarmaList.iniciarAnimacionCargaImagen(getReferenciaVentana().getCargaImagen());
		FuncionesManejoFront.cambiarEstadoMenuBar(true, referenciaVentana);
		FuncionesManejoFront.cambiarEstadoMenuBar(true, referenciaVentanaPrincipal);

		getReferenciaVentana().getMenuImportarFicheroCodigoBarras().setDisable(true);
		getReferenciaVentana().getBotonSubidaPortada().setDisable(true);
		getReferenciaVentana().getBotonEliminarImportadoListaComic().setVisible(false);
		getReferenciaVentana().getBotonGuardarListaComics().setVisible(false);

		getReferenciaVentana().getBotonClonarComic().setVisible(false);
		getReferenciaVentana().getBotonGuardarComic().setVisible(false);
		getReferenciaVentana().getBotonEliminarImportadoComic().setVisible(false);
		getReferenciaVentana().getTablaBBDD().setDisable(true);

		AlarmaList.mostrarMensajePront("Se estan cargando los datos", true,
				getReferenciaVentana().getProntInfoTextArea());
		AlarmaList.iniciarAnimacionCarga(getReferenciaVentana().getProgresoCarga());
	}

	public static void cargarCompletado() {
		AlarmaList.detenerAnimacionCargaImagen(getReferenciaVentana().getCargaImagen());
		cambiarEstadoBotones(false);

		getReferenciaVentana().getMenuImportarFicheroCodigoBarras().setDisable(false);
		AlarmaList.detenerAnimacionCarga(getReferenciaVentana().getProgresoCarga());

		if (cargaComicsControllerRef != null) {
			actualizarInterfaz(contadorErrores, carpetaRaizPortadas(Utilidades.nombreDB()), numLineas);
			Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics("", "100%", 100.0));
		}

	}

	public static void handleTaskEvents(Task<Void> tarea, String tipoUpdate) {

		tarea.setOnRunning(ev -> {
			if (tipoUpdate.isEmpty()) {
				cargarRuning();
			} else {
				String cadenaAfirmativo = getAffirmativeMessage(tipoUpdate);
				List<Stage> stageVentanas = FuncionesManejoFront.getStageVentanas();

				for (Stage stage : stageVentanas) {
					if (stage.getTitle().equalsIgnoreCase("Menu principal")
							|| stage.getTitle().equalsIgnoreCase("Opciones avanzadas")) {
						stage.setOnCloseRequest(closeEvent -> {

							tarea.cancel(true);
							Utilidades.cerrarCargaComics();
							FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentana);
							FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentanaPrincipal);
							FuncionesManejoFront.cambiarEstadoOpcionesAvanzadas(false, referenciaVentana);
							nav.cerrarMenuOpciones();
							nav.cerrarVentanaAccion();

							Utilidades.cerrarOpcionesAvanzadas();
						});
					}
				}

				getReferenciaVentanaPrincipal().getProntInfoTextArea().setDisable(false);
				getReferenciaVentanaPrincipal().getProntInfoTextArea().setOpacity(1);

				AlarmaList.mostrarMensajePront("Se estan cargando los datos", true,
						getReferenciaVentanaPrincipal().getProntInfoTextArea());

				AlarmaList.iniciarAnimacionAvanzado(getReferenciaVentana().getProntInfoEspecial(), cadenaAfirmativo);
				getReferenciaVentana().getBotonCancelarSubida().setVisible(true);
				actualizarCombobox();
				FuncionesManejoFront.cambiarEstadoMenuBar(true, referenciaVentana);
				FuncionesManejoFront.cambiarEstadoMenuBar(true, referenciaVentanaPrincipal);
				FuncionesManejoFront.cambiarEstadoOpcionesAvanzadas(true, getReferenciaVentana());
			}

		});

		tarea.setOnSucceeded(ev -> {

			if (ListasComicsDAO.comicsImportados.isEmpty() && tipoUpdate.isEmpty()) {

				List<Node> elementos = Arrays.asList(referenciaVentana.getBotonEliminarImportadoListaComic(),
						referenciaVentana.getBotonGuardarListaComics(), referenciaVentana.getBotonGuardarComic(),
						referenciaVentana.getBotonEliminarImportadoComic());

				Utilidades.cambiarVisibilidad(elementos, true);

				String cadenaAfirmativo = "No se han encontrado Comics en los datos proporcionados";
				AlarmaList.mostrarMensajePront(cadenaAfirmativo, false, getReferenciaVentana().getProntInfoTextArea());
				nav.cerrarCargaComics();
				getReferenciaVentana().getBotonCancelarSubida().setVisible(false);
				getReferenciaVentana().getTablaBBDD().setDisable(false);
			} else {
				if (tipoUpdate.isEmpty()) {
					getReferenciaVentana().getTablaBBDD().setDisable(false);
					referenciaVentana.getBotonEliminarImportadoListaComic().setVisible(true);
					referenciaVentana.getBotonGuardarListaComics().setVisible(true);

					cargarCompletado();
				} else {

					AlarmaList.mostrarMensajePront("Datos cargados correctamente", true,
							getReferenciaVentanaPrincipal().getProntInfoTextArea());

					Platform.runLater(() -> getReferenciaVentana().getBotonCancelarSubida().setVisible(false));
					String cadenaAfirmativo = getUpdateTypeString(tipoUpdate);
					AlarmaList.iniciarAnimacionAvanzado(getReferenciaVentana().getProntInfoEspecial(),
							cadenaAfirmativo);

					actualizarCombobox();

					FuncionesManejoFront.cambiarEstadoOpcionesAvanzadas(false, getReferenciaVentana());

				}

			}

			FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentana);
			FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentanaPrincipal);
			AlarmaList.detenerAnimacionCargaImagen(getReferenciaVentana().getCargaImagen());
			AlarmaList.detenerAnimacionCarga(getReferenciaVentana().getProgresoCarga());

			cambiarEstadoBotones(false);
		});

		tarea.setOnCancelled(ev -> {
			if (ListasComicsDAO.comicsImportados.isEmpty() && tipoUpdate.isEmpty()) {

				List<Node> elementos = Arrays.asList(referenciaVentana.getBotonEliminarImportadoListaComic(),
						referenciaVentana.getBotonGuardarListaComics(), referenciaVentana.getBotonGuardarComic(),
						referenciaVentana.getBotonEliminarImportadoComic());

				Utilidades.cambiarVisibilidad(elementos, true);

				String cadenaAfirmativo = "No se han encontrado Comics en los datos proporcionados";
				AlarmaList.mostrarMensajePront(cadenaAfirmativo, false, getReferenciaVentana().getProntInfoTextArea());
				nav.cerrarCargaComics();
			} else {
				if (tipoUpdate.isEmpty()) {

					getReferenciaVentana().getTablaBBDD().setDisable(false);
					cambiarEstadoBotones(false);

					Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics("", "100%", 100.0));

					AlarmaList.mostrarMensajePront("Se ha cancelado la importacion", false,
							getReferenciaVentana().getProntInfoTextArea());

					AlarmaList.detenerAnimacionCarga(getReferenciaVentana().getProgresoCarga());
					AlarmaList.detenerAnimacionCargaImagen(getReferenciaVentana().getCargaImagen());
					Thread.currentThread().interrupt();
				} else {
					String cadenaAfirmativo = "Cancelada la actualización de la base de datos.";
					AlarmaList.iniciarAnimacionAvanzado(getReferenciaVentana().getProntInfoEspecial(),
							cadenaAfirmativo);
//					actualizarCombobox();
					FuncionesManejoFront.cambiarEstadoOpcionesAvanzadas(false, getReferenciaVentana());
					Platform.runLater(() -> getReferenciaVentana().getBotonCancelarSubida().setVisible(false));
				}

			}

			if (!ListasComicsDAO.comicsImportados.isEmpty()) {
				referenciaVentana.getBotonEliminarImportadoListaComic().setVisible(true);
				referenciaVentana.getBotonGuardarListaComics().setVisible(true);
			}

			AlarmaList.detenerAnimacionCargaImagen(getReferenciaVentana().getCargaImagen());
			AlarmaList.detenerAnimacionCarga(getReferenciaVentana().getProgresoCarga());
			FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentana);
			FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentanaPrincipal);
		});

		// Configurar el evento para cancelar la tarea cuando se presiona el botón de
		// cancelar subida
		getReferenciaVentana().getBotonCancelarSubida().setOnAction(ev -> {
			if (tipoUpdate.isEmpty()) {
				actualizarInterfaz(comicsProcesados, "", numLineas);
				cambiarEstadoBotones(false);
				getReferenciaVentana().getMenuImportarFicheroCodigoBarras().setDisable(false);
			} else {
				referenciaVentanaPrincipal.getProntInfoTextArea().clear();
				referenciaVentanaPrincipal.getProntInfoTextArea().setText(null);
				referenciaVentanaPrincipal.getProntInfoTextArea().setOpacity(0);
				Platform.runLater(() -> cargaComicsControllerRef.get().cargarDatosEnCargaComics("", "100%", 100.0));
				String cadenaAfirmativo = "Cancelada la actualización de la base de datos.";
				AlarmaList.iniciarAnimacionAvanzado(getReferenciaVentana().getProntInfoEspecial(), cadenaAfirmativo);
				actualizarCombobox();
				FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentana);
				FuncionesManejoFront.cambiarEstadoMenuBar(false, referenciaVentanaPrincipal);
				FuncionesManejoFront.cambiarEstadoOpcionesAvanzadas(false, getReferenciaVentana());
				Platform.runLater(() -> getReferenciaVentana().getBotonCancelarSubida().setVisible(false));
				getReferenciaVentana().getBotonCancelarSubida().setVisible(false);

			}
			tarea.cancel(true);
		});
	}

	private static String getUpdateTypeString(String tipoUpdate) {
		if (tipoUpdate.equalsIgnoreCase("actualizar datos")) {
			return "Datos actualizados";
		} else if (tipoUpdate.equalsIgnoreCase("actualizar portadas")) {
			return "Portadas actualizadas";
		} else {
			return "Base de datos actualizada";
		}
	}

	private static String getAffirmativeMessage(String tipoUpdate) {
		if (tipoUpdate.equalsIgnoreCase("actualizar datos")) {
			return "Actualizando datos";
		} else if (tipoUpdate.equalsIgnoreCase("actualizar portadas")) {
			return "Actualizando portadas";
		} else {
			return "Actualizando base de datos";
		}
	}

	public static void actualizarCombobox() {
		ListasComicsDAO.reiniciarListaComics();
		ListasComicsDAO.listasAutoCompletado();

		getReferenciaVentana();
		List<ComboBox<String>> comboboxes = AccionReferencias.getListaComboboxes();

		if (comboboxes != null) {
			funcionesCombo.rellenarComboBox(comboboxes);
		}
	}

	/**
	 * Elimina cualquier resaltado de campos en rojo que indique errores.
	 */
	public static void borrarErrores() {

		if (getReferenciaVentana() != null) {
			setStyleIfNotNull(getReferenciaVentana().getTituloComicTextField());
			setStyleIfNotNull(getReferenciaVentana().getNumeroComicCombobox());
			setStyleIfNotNull(getReferenciaVentana().getNombreEditorTextField());
			setStyleIfNotNull(getReferenciaVentana().getArtistaComicTextField());
			setStyleIfNotNull(getReferenciaVentana().getGuionistaComicTextField());
			setStyleIfNotNull(getReferenciaVentana().getVarianteTextField());
		}
	}

	public static void setStyleIfNotNull(Node element) {
		if (element != null) {
			element.setStyle("");
		}
	}

	/**
	 * Modifica la visibilidad y el estado de los elementos de búsqueda en la
	 * interfaz de usuario.
	 *
	 * @param mostrar True para mostrar los elementos de búsqueda, False para
	 *                ocultarlos.
	 */
	public static void cambiarVisibilidadAvanzada() {

		List<Node> elementos = Arrays.asList(getReferenciaVentana().getBotonBusquedaCodigo(),
				getReferenciaVentana().getBusquedaCodigoTextField(), getReferenciaVentana().getNombreTiendaCombobox());

		if (getReferenciaVentana().getBotonBusquedaCodigo().isVisible()) {
			Utilidades.cambiarVisibilidad(elementos, true);
		} else {
			getReferenciaVentana().getBusquedaCodigoTextField().setText("");
			Utilidades.cambiarVisibilidad(elementos, false);

		}
	}

	public static void cambiarEstadoBotones(boolean esCancelado) {

		if (TIPO_ACCION != null) {
			List<Node> elementos = Arrays.asList(getReferenciaVentana().getBotonSubidaPortada());

			if (!TIPO_ACCION.equals("aniadir")) {
				elementos.add(getReferenciaVentana().getBotonBusquedaCodigo());
				elementos.add(getReferenciaVentana().getBusquedaCodigoTextField());
				elementos.add(getReferenciaVentana().getBotonCancelarSubida());
				elementos.add(getReferenciaVentana().getBotonBusquedaCodigo());
				elementos.add(getReferenciaVentana().getBotonSubidaPortada());
				elementos.add(getReferenciaVentana().getNombreTiendaCombobox());
			}

			getReferenciaVentana().getBotonCancelarSubida().setVisible(esCancelado);

			if (getReferenciaVentana().getBotonLimpiar() != null) {
				getReferenciaVentana().getBotonLimpiar().setDisable(esCancelado);
				getReferenciaVentana().getBotonBusquedaAvanzada().setDisable(esCancelado);
			}

			Utilidades.cambiarVisibilidad(elementos, esCancelado);
		}

	}

	public static String carpetaRaizPortadas(String nombreDatabase) {
		return DOCUMENTS_PATH + File.separator + "libreria_comics" + File.separator + nombreDatabase + File.separator;
	}

	public static String carpetaPortadas(String nombreDatabase) {
		return DOCUMENTS_PATH + File.separator + "libreria_comics" + File.separator + nombreDatabase + File.separator
				+ "portadas";
	}

	public static void setTipoAccion(String tipoAccion) {
		TIPO_ACCION = tipoAccion;
	}

	public static String getTipoAccion() {
		return TIPO_ACCION;
	}

	public static AccionReferencias getReferenciaVentana() {
		return referenciaVentana;
	}

	public static AccionReferencias getReferenciaVentanaPrincipal() {
		return referenciaVentanaPrincipal;
	}

	public static void setReferenciaVentana(AccionReferencias referenciaVentana) {
		AccionFuncionesComunes.referenciaVentana = referenciaVentana;
	}

	public static void setReferenciaVentanaPrincipal(AccionReferencias referenciaVentana) {
		AccionFuncionesComunes.referenciaVentanaPrincipal = referenciaVentana;
	}

}
