package funcionesInterfaz;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Controladores.ImagenAmpliadaController;
import Controladores.VentanaAccionController;
import alarmas.AlarmaList;
import comicManagement.Comic;
import dbmanager.ComicManagerDAO;
import dbmanager.ListasComicsDAO;
import funcionesAuxiliares.Utilidades;
import funcionesManagment.AccionAniadir;
import funcionesManagment.AccionEliminar;
import funcionesManagment.AccionFuncionesComunes;
import funcionesManagment.AccionModificar;
import funcionesManagment.AccionReferencias;
import funcionesManagment.AccionSeleccionar;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class AccionControlUI {

	private static AccionReferencias referenciaVentana = getReferenciaVentana();

	private static VentanaAccionController accionController = new VentanaAccionController();

	private static AccionAniadir accionAniadir = new AccionAniadir();

	private static AccionEliminar accionEliminar = new AccionEliminar();

	private static AccionModificar accionModificar = new AccionModificar();

	public static void autoRelleno() {

		referenciaVentana.getIdComicTratarTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.isEmpty()) {
				if (!rellenarCampos(newValue)) {
					limpiarAutorellenos(false);
					borrarDatosGraficos();
				}
			} else {
				limpiarAutorellenos(false);
				borrarDatosGraficos();
			}
		});
	}

	public static boolean rellenarCampos(String idComic) {
		Comic comicTempTemp = Comic.obtenerComic(idComic);
		if (comicTempTemp != null) {
			rellenarDatos(comicTempTemp);
			return true;
		}
		return false;
	}

	public static void mostrarOpcion(String opcion) {
		ocultarCampos();

		List<Node> elementosAMostrarYHabilitar = new ArrayList<>();

		switch (opcion.toLowerCase()) {
		case "eliminar":
			accionEliminar.mostrarElementosEliminar(elementosAMostrarYHabilitar);
			break;
		case "aniadir":
			accionAniadir.mostrarElementosAniadir(elementosAMostrarYHabilitar);
			break;
		case "modificar":
			accionModificar.mostrarElementosModificar(elementosAMostrarYHabilitar);
			break;
		default:
			accionController.closeWindow();
			return;
		}

		mostrarElementos(elementosAMostrarYHabilitar);
	}

	public static List<Node> modificarInterfazAccion(String opcion) {

		List<Node> elementosAMostrarYHabilitar = new ArrayList<>();

		switch (opcion.toLowerCase()) {
		case "modificar":
			elementosAMostrarYHabilitar.add(referenciaVentana.getBotonModificarComic());
			elementosAMostrarYHabilitar.add(referenciaVentana.getBotonEliminar());
			break;
		case "aniadir":
			elementosAMostrarYHabilitar.add(referenciaVentana.getBotonGuardarComic());
			elementosAMostrarYHabilitar.add(referenciaVentana.getBotonEliminarImportadoComic());
			elementosAMostrarYHabilitar.add(referenciaVentana.getBotonClonarComic());
			break;
		default:
			break;
		}

		return elementosAMostrarYHabilitar;
	}

	private static void mostrarElementos(List<Node> elementosAMostrarYHabilitar) {
		for (Node elemento : elementosAMostrarYHabilitar) {

			if (elemento != null) {
				elemento.setVisible(true);
				elemento.setDisable(false);
			}
		}

		if (!AccionFuncionesComunes.TIPO_ACCION.equals("modificar")) {
			autoRelleno();
		}

		if (!AccionFuncionesComunes.TIPO_ACCION.equals("aniadir")) {

			referenciaVentana.getNavegacionCerrar().setDisable(true);
			referenciaVentana.getNavegacionCerrar().setVisible(false);
		} else {
			referenciaVentana.getIdComicTratarTextField().setEditable(false);
			referenciaVentana.getIdComicTratarTextField().setOpacity(0.7);
		}

		if (AccionFuncionesComunes.TIPO_ACCION.equals("aniadir")) {
			referenciaVentana.getBotonEliminarImportadoListaComic().setVisible(false);
			referenciaVentana.getBotonGuardarListaComics().setVisible(false);

			referenciaVentana.getBotonEliminarImportadoListaComic().setDisable(true);
			referenciaVentana.getBotonGuardarListaComics().setDisable(true);
		}
		if (AccionFuncionesComunes.TIPO_ACCION.equals("modificar")) {

			referenciaVentana.getBotonModificarComic().setVisible(false);
			referenciaVentana.getBotonModificarComic().setDisable(true);

			referenciaVentana.getBotonEliminar().setVisible(false);
			referenciaVentana.getBotonEliminar().setDisable(true);
		}

	}

	/**
	 * Oculta y deshabilita varios campos y elementos en la interfaz gráfica.
	 */
	public static void ocultarCampos() {

//		List<Node> elementosTextfield = Arrays.asList(referenciaVentana.getGradeoComicTextField(),
//				referenciaVentana.getDireccionImagenTextField(), referenciaVentana.getUrlReferenciaTextField(),
//				referenciaVentana.getNombreEmpresaTextField(), referenciaVentana.getAnioComicTextField(),
//				referenciaVentana.getCodigoComicTextField());
//
//		List<Node> elementosLabel = Arrays.asList(referenciaVentana.getLabelGradeo(),
//				referenciaVentana.getLabelPortada(), referenciaVentana.getLabelReferencia(),
//				referenciaVentana.getLabelEmpresa(), referenciaVentana.getLabelCodigo(),
//				referenciaVentana.getLabelAnio());
//
//		List<Node> elementosBoton = Arrays.asList(referenciaVentana.getBotonSubidaPortada(),
//				referenciaVentana.getBotonEliminar(), referenciaVentana.getBotonModificarComic(),
//				referenciaVentana.getBotonBusquedaCodigo(), referenciaVentana.getBotonbbdd());
//
//		Utilidades.cambiarVisibilidad(elementosTextfield, true);
//		Utilidades.cambiarVisibilidad(elementosLabel, true);
//		Utilidades.cambiarVisibilidad(elementosBoton, true);
	}

	/**
	 * Establece los atributos del cómic basándose en el objeto Comic proporcionado.
	 * 
	 * @param comicTempTemp El objeto Comic que contiene los datos a establecer.
	 */
	public void setAtributosDesdeTabla(Comic comicTemp) {

		referenciaVentana.getIdComicTratarTextField().setText(comicTemp.getIdComic());
		referenciaVentana.getTituloComicTextField().setText(comicTemp.getTituloComic());
		referenciaVentana.getCodigoComicTextField().setText(comicTemp.getCodigoComic());
		referenciaVentana.getNumeroComicTextField().setText(comicTemp.getNumeroComic());
		referenciaVentana.getPrecioComicTextField().setText(comicTemp.getPrecioComic());
		Utilidades.setDatePickerValue(referenciaVentana.getDataPickFechaP(),
				referenciaVentana.getDataPickFechaP().toString());
		referenciaVentana.getNombreEditorTextField().setText(comicTemp.getEditorComic());
		referenciaVentana.getFirmaComicTextField().setText(comicTemp.getFirmaComic());
		referenciaVentana.getVarianteTextField().setText(comicTemp.getVarianteComic());
		referenciaVentana.getArtistaComicTextField().setText(comicTemp.getArtistaComic());
		referenciaVentana.getGuionistaComicTextField().setText(comicTemp.getGuionistaComic());
		referenciaVentana.getVarianteTextField().setText(comicTemp.getVarianteComic());
		referenciaVentana.getKeyComicData().setText(comicTemp.getKeyComentarios());
		referenciaVentana.getDireccionImagenTextField().setText(comicTemp.getDireccionImagenComic());
		referenciaVentana.getUrlReferenciaTextField().setText(comicTemp.getUrlReferenciaComic());

		Utilidades.cargarImagenAsync(comicTemp.getDireccionImagenComic(), referenciaVentana.getImagenComic());
	}

	private static void rellenarDatos(Comic comicTemp) {

		referenciaVentana.getIdComicTratarTextField().setText(comicTemp.getIdComic());
		referenciaVentana.getTituloComicTextField().setText(comicTemp.getTituloComic());
		referenciaVentana.getCodigoComicTextField().setText(comicTemp.getCodigoComic());
		referenciaVentana.getNumeroComicTextField().setText(comicTemp.getNumeroComic());
		Utilidades.setDatePickerValue(referenciaVentana.getDataPickFechaP(),
				referenciaVentana.getDataPickFechaP().toString());
		referenciaVentana.getNombreEditorTextField().setText(comicTemp.getEditorComic());
		referenciaVentana.getVarianteTextField().setText(comicTemp.getVarianteComic());
		referenciaVentana.getArtistaComicTextField().setText(comicTemp.getArtistaComic());
		referenciaVentana.getGuionistaComicTextField().setText(comicTemp.getGuionistaComic());
		referenciaVentana.getVarianteTextField().setText(comicTemp.getVarianteComic());
		referenciaVentana.getKeyComicData().setText(comicTemp.getKeyComentarios());
		referenciaVentana.getDireccionImagenTextField().setText(comicTemp.getDireccionImagenComic());
		referenciaVentana.getUrlReferenciaTextField().setText(comicTemp.getUrlReferenciaComic());

		referenciaVentana.getProntInfoTextArea().clear();
		referenciaVentana.getProntInfoTextArea().setOpacity(1);

		Image imagenComic = Utilidades.devolverImagenComic(comicTemp.getDireccionImagenComic());
		referenciaVentana.getImagenComic().setImage(imagenComic);
	}

	public static void validarCamposClave(boolean esBorrado) {
		List<TextField> camposUi = Arrays.asList(referenciaVentana.getTituloComicTextField(),
				referenciaVentana.getNombreEditorTextField(), referenciaVentana.getNombreEditorTextField(),
				referenciaVentana.getArtistaComicTextField(), referenciaVentana.getVarianteTextField(),
				referenciaVentana.getGuionistaComicTextField());

		for (TextField campoUi : camposUi) {

			if (campoUi != null) {
				String datoComic = campoUi.getText();

				if (esBorrado) {
					if (datoComic == null || datoComic.isEmpty() || datoComic.equalsIgnoreCase("vacio")) {
						campoUi.setStyle("");
					}
				} else {
					// Verificar si el campo está vacío, es nulo o tiene el valor "Vacio"
					if (datoComic == null || datoComic.isEmpty() || datoComic.equalsIgnoreCase("vacio")) {
						campoUi.setStyle("-fx-background-color: red;");
					} else {
						campoUi.setStyle("");
					}
				}
			}
		}
	}

	public boolean camposComicSonValidos() {
		List<TextField> camposUi = Arrays.asList(referenciaVentana.getTituloComicTextField(),
				referenciaVentana.getNombreEditorTextField(), referenciaVentana.getNombreEditorTextField(),
				referenciaVentana.getArtistaComicTextField(), referenciaVentana.getVarianteTextField(),
				referenciaVentana.getGuionistaComicTextField());

		for (Control campoUi : camposUi) {
			String datoComic = ((TextField) campoUi).getText();

			// Verificar si el campo está vacío, es nulo o tiene el valor "Vacio"
			if (datoComic == null || datoComic.isEmpty() || datoComic.equalsIgnoreCase("vacio")) {
				campoUi.setStyle("-fx-background-color: #FF0000;");
				return false; // Devolver false si al menos un campo no es válido
			} else {
				campoUi.setStyle("");
			}
		}

		return true; // Devolver true si todos los campos son válidos
	}

	public static boolean comprobarListaValidacion(Comic c) {
		// Validar campos requeridos y "vacio"
		if (c.getTituloComic() == null || c.getTituloComic().isEmpty() || c.getNumeroComic() == null
				|| c.getNumeroComic().isEmpty() || c.getEditorComic() == null || c.getEditorComic().isEmpty()
				|| c.getUrlReferenciaComic() == null || c.getUrlReferenciaComic().isEmpty()
				|| c.getFechaGradeo() == null || c.getFechaGradeo().isEmpty() || c.getDireccionImagenComic() == null
				|| c.getDireccionImagenComic().isEmpty() || c.getCodigoComic() == null || c.getCodigoComic().isEmpty()
				|| c.getKeyComentarios() == null || c.getKeyComentarios().isEmpty() || c.getArtistaComic() == null
				|| c.getArtistaComic().isEmpty() || c.getGuionistaComic() == null || c.getGuionistaComic().isEmpty()
				|| c.getVarianteComic() == null || c.getVarianteComic().isEmpty()) {

			String mensajePront = "Revisa la lista, algunos campos están mal rellenados.";
			AlarmaList.mostrarMensajePront(mensajePront, false, referenciaVentana.getProntInfoTextArea());
			return false;
		}

		return true;
	}

	/**
	 * Borra los datos del cómic
	 */
	public static void limpiarAutorellenos(boolean esPrincipal) {

		if (esPrincipal) {
			referenciaVentana.getProntInfoTextArea().clear();
			referenciaVentana.getProntInfoTextArea().setText(null);
			referenciaVentana.getProntInfoTextArea().setOpacity(0);
			referenciaVentana.getTablaBBDD().getItems().clear();
			referenciaVentana.getTablaBBDD().setOpacity(0.6);
			referenciaVentana.getTablaBBDD().refresh();
			referenciaVentana.getImagenComic().setImage(null);
			referenciaVentana.getImagenComic().setOpacity(0);
			return;
		}

		// Limpiar valores en TextField
		referenciaVentana.getIdComicTratarTextField().setText("");
		referenciaVentana.getTituloComicTextField().setText("");
		referenciaVentana.getCodigoComicTextField().setText("");
		referenciaVentana.getNumeroComicTextField().setText("");
		referenciaVentana.getNombreEditorTextField().setText("");
		referenciaVentana.getVarianteTextField().setText("");
		referenciaVentana.getArtistaComicTextField().setText("");
		referenciaVentana.getGuionistaComicTextField().setText("");
		referenciaVentana.getKeyComicData().setText("");
		referenciaVentana.getDireccionImagenTextField().setText("");
		referenciaVentana.getUrlReferenciaTextField().setText("");

		// Limpiar valor en DatePicker
		referenciaVentana.getDataPickFechaP().setValue(null);

		if ("aniadir".equals(AccionFuncionesComunes.TIPO_ACCION)) {
			referenciaVentana.getIdComicTratarTextField().setDisable(false);
			referenciaVentana.getIdComicTratarTextField().setText("");
			referenciaVentana.getIdComicTratarTextField().setDisable(true);
		} else {
			referenciaVentana.getTablaBBDD().getItems().clear();
			referenciaVentana.getTablaBBDD().refresh();
		}

		if ("modificar".equals(AccionFuncionesComunes.TIPO_ACCION)) {
			referenciaVentana.getBotonModificarComic().setVisible(false);
			referenciaVentana.getBotonEliminar().setVisible(false);
		}

		referenciaVentana.getProntInfoTextArea().setText(null);
		referenciaVentana.getProntInfoTextArea().setOpacity(0);
		referenciaVentana.getProntInfoTextArea().setStyle("");
		validarCamposClave(true);
	}

	public static void borrarDatosGraficos() {
		referenciaVentana.getProntInfoTextArea().setText(null);
		referenciaVentana.getProntInfoTextArea().setOpacity(0);
		referenciaVentana.getProntInfoTextArea().setStyle("");
	}

	/**
	 * Asigna tooltips a varios elementos en la interfaz gráfica. Estos tooltips
	 * proporcionan información adicional cuando el usuario pasa el ratón sobre los
	 * elementos.
	 */
	public static void establecerTooltips() {
		Platform.runLater(() -> {
			Map<Node, String> tooltipsMap = new HashMap<>();

			tooltipsMap.put(referenciaVentana.getTituloComicCombobox(), "Nombre de los cómics / libros / mangas");
			tooltipsMap.put(referenciaVentana.getNumeroComicCombobox(), "Número del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getNombreTiendaCombobox(),
					"Nombre de la tienda del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getNombreEditorCombobox(), "Nombre del editor del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getNombreArtistaCombobox(),
					"Nombre del artista del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getNombreGuionistaCombobox(),
					"Nombre del guionista del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getNombreVarianteCombobox(), "Variante del cómic / libro / manga");

			tooltipsMap.put(referenciaVentana.getBotonLimpiar(), "Limpia la pantalla y reinicia todos los valores");
			tooltipsMap.put(referenciaVentana.getBotonbbdd(), "Botón para acceder a la base de datos");
			tooltipsMap.put(referenciaVentana.getBotonSubidaPortada(), "Botón para subir una portada");
			tooltipsMap.put(referenciaVentana.getBotonEliminar(), "Botón para eliminar un cómic");
			tooltipsMap.put(referenciaVentana.getBotonParametroComic(),
					"Botón para buscar un cómic mediante una lista de parámetros");
			tooltipsMap.put(referenciaVentana.getBotonModificarComic(), "Botón para modificar un cómic");

			tooltipsMap.put(referenciaVentana.getBotonIntroducir(),
					"Realizar una acción de introducción del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getBotonModificar(),
					"Realizar una acción de modificación del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getBotonEliminar(),
					"Realizar una acción de eliminación del cómic / libro / manga");
			tooltipsMap.put(referenciaVentana.getBotonMostrarParametro(),
					"Buscar por parámetros según los datos rellenados");

			FuncionesTooltips.assignTooltips(tooltipsMap);
		});
	}

	public static void autocompletarListas() {
		FuncionesManejoFront.asignarAutocompletado(referenciaVentana.getTituloComicTextField(),
				ListasComicsDAO.listaNombre);
		FuncionesManejoFront.asignarAutocompletado(referenciaVentana.getNombreEditorTextField(),
				ListasComicsDAO.listaEditor);
		FuncionesManejoFront.asignarAutocompletado(referenciaVentana.getArtistaComicTextField(),
				ListasComicsDAO.listaArtista);
		FuncionesManejoFront.asignarAutocompletado(referenciaVentana.getGuionistaComicTextField(),
				ListasComicsDAO.listaGuionista);
		FuncionesManejoFront.asignarAutocompletado(referenciaVentana.getVarianteTextField(),
				ListasComicsDAO.listaVariante);
		FuncionesManejoFront.asignarAutocompletado(referenciaVentana.getNumeroComicTextField(),
				ListasComicsDAO.listaNumeroComic);
	}

	public static void controlarEventosInterfaz() {

		referenciaVentana.getProntInfoTextArea().textProperty().addListener((observable, oldValue, newValue) -> {
			FuncionesTableView.ajustarAnchoVBox();
		});

		// Desactivar el enfoque en el VBox para evitar que reciba eventos de teclado
		referenciaVentana.getRootVBox().setFocusTraversable(false);

		// Agregar un filtro de eventos para capturar el enfoque en el TableView y
		// desactivar el enfoque en el VBox
		referenciaVentana.getTablaBBDD().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			referenciaVentana.getRootVBox().setFocusTraversable(false);
			referenciaVentana.getTablaBBDD().requestFocus();
		});

		referenciaVentana.getImagenComic().imageProperty().addListener((observable, oldImage, newImage) -> {
			if (newImage != null) {
				// Cambiar la apariencia del cursor y la opacidad cuando la imagen se ha cargado
				referenciaVentana.getImagenComic().setOnMouseEntered(e -> {
					if (referenciaVentana.getImagenComic() != null) {
						referenciaVentana.getImagenComic().setOpacity(0.7);
						referenciaVentana.getImagenComic().setCursor(Cursor.HAND);
					}
				});

				// Restaurar el cursor y la opacidad al salir del ImageView
				referenciaVentana.getImagenComic().setOnMouseExited(e -> {

					if (referenciaVentana.getImagenComic() != null) {

						referenciaVentana.getImagenComic().setOpacity(1.0); // Restaurar la opacidad
						referenciaVentana.getImagenComic().setCursor(Cursor.DEFAULT);
					}

				});
			} else {
				// Restaurar el cursor y la opacidad al salir del ImageView
				referenciaVentana.getImagenComic()
						.setOnMouseEntered(e -> referenciaVentana.getImagenComic().setCursor(Cursor.DEFAULT));
			}
		});

	}

	public static void controlarEventosInterfazAccion() {
		controlarEventosInterfaz();

		// Establecemos un evento para detectar cambios en el segundo TextField
		referenciaVentana.getIdComicTratarTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			// Verificar que newValue no sea null antes de usarlo
			AccionSeleccionar.mostrarComic(newValue, false);

			if (newValue != null && !newValue.isEmpty()) {

				if ("modificar".equals(AccionFuncionesComunes.TIPO_ACCION)) {

					AccionSeleccionar.verBasedeDatos(true, true, null);
					Comic comic = ComicManagerDAO.comicDatos(newValue);

					referenciaVentana.getBotonEliminar().setVisible(true);
					referenciaVentana.getBotonModificarComic().setVisible(true);

					referenciaVentana.getBotonEliminar().setDisable(false);
					referenciaVentana.getBotonModificarComic().setDisable(false);

					referenciaVentana.getTablaBBDD().getSelectionModel().select(comic);
					referenciaVentana.getTablaBBDD().scrollTo(comic); // Esto hará scroll hasta el elemento seleccionado
				}

			}
		});

		List<Node> elementos = Arrays.asList(referenciaVentana.getBotonGuardarComic(),
				referenciaVentana.getBotonEliminarImportadoComic(),
				referenciaVentana.getBotonEliminarImportadoListaComic(), referenciaVentana.getBotonGuardarListaComics(),
				referenciaVentana.getBotonEliminarImportadoComic(), referenciaVentana.getBotonGuardarComic());

		ListasComicsDAO.comicsImportados.addListener((ListChangeListener<Comic>) change -> {
			while (change.next()) {

				if (!change.wasAdded() && ListasComicsDAO.comicsImportados.isEmpty()) {
					Utilidades.cambiarVisibilidad(elementos, true);
				}
			}
		});
	}

	public static void controlarEventosInterfazPrincipal(AccionReferencias referenciaVentana) {
		controlarEventosInterfaz();

		// Establecer un Listener para el tamaño del AnchorPane
		referenciaVentana.getRootAnchorPane().widthProperty().addListener((observable, oldValue, newValue) -> {

			FuncionesTableView.ajustarAnchoVBox();
			FuncionesTableView.seleccionarRaw();
			FuncionesTableView.modificarColumnas(true);
		});
	}

	public static Comic camposComic(List<String> camposComic, boolean esAccion) {
		Comic comicTemp = new Comic();

		String tituloComic = camposComic.get(0);
		String numeroComic = camposComic.get(1);
		String editorComic = camposComic.get(2);
		String firmaComic = camposComic.get(3);
		String guionistaComic = camposComic.get(4);
		String varianteComic = camposComic.get(5);
		String artistaComic = camposComic.get(6);

		String direccionImagenComic = "";
		String codigoComic = "";
		String urlReferenciaComic = "";
		String idComicTratar = "";
		String fechaGradeo = "";
		String keyComentarios = "";
		String precioComic = "";

		// Si es una acción, algunos campos se asignan en un orden diferente
		if (esAccion) {
			fechaGradeo = camposComic.get(2); // 2
			artistaComic = camposComic.get(3); // 3
			varianteComic = camposComic.get(4); // 4
			guionistaComic = camposComic.get(5); // 5
			urlReferenciaComic = camposComic.get(6); // 6
			direccionImagenComic = camposComic.get(7); // 7
			keyComentarios = camposComic.get(8); // 8
			editorComic = camposComic.get(9); // 9
			codigoComic = camposComic.get(10); // 9
			precioComic = camposComic.get(11); // 11
			firmaComic = camposComic.get(12); // 12
			idComicTratar = camposComic.get(13);
		}

		// Establecer los valores en el objeto ComicGradeo
		comicTemp.setTituloComic(Utilidades.defaultIfNullOrEmpty(tituloComic, ""));
		comicTemp.setNumeroComic(Utilidades.defaultIfNullOrEmpty(numeroComic, ""));
		comicTemp.setCodigoComic(Utilidades.defaultIfNullOrEmpty(codigoComic, ""));
		comicTemp.setPrecioComic(Utilidades.defaultIfNullOrEmpty(precioComic, ""));
		comicTemp.setFechaGradeo(Utilidades.defaultIfNullOrEmpty(fechaGradeo, ""));
		comicTemp.setEditorComic(Utilidades.defaultIfNullOrEmpty(editorComic, ""));
		comicTemp.setKeyComentarios(Utilidades.defaultIfNullOrEmpty(keyComentarios, ""));
		comicTemp.setFirmaComic(Utilidades.defaultIfNullOrEmpty(firmaComic, ""));
		comicTemp.setArtistaComic(Utilidades.defaultIfNullOrEmpty(artistaComic, ""));
		comicTemp.setGuionistaComic(Utilidades.defaultIfNullOrEmpty(guionistaComic, ""));
		comicTemp.setVarianteComic(Utilidades.defaultIfNullOrEmpty(varianteComic, ""));
		comicTemp.setDireccionImagenComic(Utilidades.defaultIfNullOrEmpty(direccionImagenComic, ""));
		comicTemp.setUrlReferenciaComic(Utilidades.defaultIfNullOrEmpty(urlReferenciaComic, ""));
		comicTemp.setIdComic(Utilidades.defaultIfNullOrEmpty(idComicTratar, ""));

		return comicTemp;
	}

	public static List<String> comprobarYDevolverLista(List<ComboBox<String>> comboBoxes,
			ObservableList<Control> observableList) {
		List<String> valores = new ArrayList<>();
		for (ComboBox<String> comboBox : comboBoxes) {
			valores.add(comboBox.getValue() != null ? comboBox.getValue() : "");
		}
		if (contieneNulo(comboBoxes)) {
			return Arrays.asList(observableList.stream()
					.map(control -> control instanceof TextInputControl ? ((TextInputControl) control).getText() : "")
					.toArray(String[]::new));
		} else {
			return valores;
		}
	}

	private static <T> boolean contieneNulo(List<T> lista) {

		if (lista == null) {
			return false;
		}

		for (T elemento : lista) {
			if (elemento == null) {
				return true;
			}
		}
		return false;
	}

	public static Comic comicModificado() {
		// Obtener el ID del cómic a modificar
		String idComicTemp = referenciaVentana.getIdComicTratarTextField().getText();

		// Obtener los datos del cómic existente
		Comic comicTemp = ComicManagerDAO.comicDatos(idComicTemp);

		List<Control> allControls = getReferenciaVentana().getControlAccion();
		List<String> valorControles = new ArrayList<>();
		for (Control control : allControls) {
			if (control instanceof TextField) {
				TextField textField = (TextField) control;
				String value = textField.getText();
				valorControles.add(value);
			} else if (control instanceof DatePicker) {
				DatePicker datePicker = (DatePicker) control;
				String value = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
				valorControles.add(value);
			} else if (control instanceof TextArea) {
				TextArea textArea = (TextArea) control;
				String value = textArea.getText();
				valorControles.add(value);
			}
		}

		// Crear un nuevo objeto ComicGradeo con los datos actualizados
		Comic datos = camposComic(valorControles, true);

		// Crear un objeto ComicGradeo modificado
		Comic comicTempModificado = new Comic();

		// Copiar el ID del cómic
		comicTempModificado.setIdComic(comicTemp.getIdComic());

		// Establecer los valores del cómic con los datos actualizados o los valores
		// existentes como respaldo
		comicTempModificado
				.setTituloComic(Utilidades.defaultIfNullOrEmpty(datos.getTituloComic(), comicTemp.getTituloComic()));
		comicTempModificado
				.setCodigoComic(Utilidades.defaultIfNullOrEmpty(datos.getCodigoComic(), comicTemp.getCodigoComic()));
		comicTempModificado
				.setNumeroComic(Utilidades.defaultIfNullOrEmpty(datos.getNumeroComic(), comicTemp.getNumeroComic()));
		comicTempModificado
				.setPrecioComic(Utilidades.defaultIfNullOrEmpty(datos.getPrecioComic(), comicTemp.getPrecioComic()));
		comicTempModificado
				.setFechaGradeo(Utilidades.defaultIfNullOrEmpty(datos.getFechaGradeo(), comicTemp.getFechaGradeo()));
		comicTempModificado
				.setEditorComic(Utilidades.defaultIfNullOrEmpty(datos.getEditorComic(), comicTemp.getEditorComic()));

		comicTempModificado
				.setFirmaComic(Utilidades.defaultIfNullOrEmpty(datos.getFirmaComic(), comicTemp.getFirmaComic()));

		comicTempModificado.setKeyComentarios(
				Utilidades.defaultIfNullOrEmpty(datos.getKeyComentarios(), comicTemp.getKeyComentarios()));
		comicTempModificado
				.setArtistaComic(Utilidades.defaultIfNullOrEmpty(datos.getArtistaComic(), comicTemp.getArtistaComic()));
		comicTempModificado.setGuionistaComic(
				Utilidades.defaultIfNullOrEmpty(datos.getGuionistaComic(), comicTemp.getGuionistaComic()));
		comicTempModificado.setVarianteComic(
				Utilidades.defaultIfNullOrEmpty(datos.getVarianteComic(), comicTemp.getVarianteComic()));
		comicTempModificado.setDireccionImagenComic(
				Utilidades.defaultIfNullOrEmpty(datos.getDireccionImagenComic(), comicTemp.getDireccionImagenComic()));
		comicTempModificado.setUrlReferenciaComic(
				Utilidades.defaultIfNullOrEmpty(datos.getUrlReferenciaComic(), comicTemp.getUrlReferenciaComic()));

		return comicTempModificado;
	}

	public static AccionReferencias getReferenciaVentana() {
		return referenciaVentana;
	}

	public static void setReferenciaVentana(AccionReferencias referenciaVentana) {
		AccionControlUI.referenciaVentana = referenciaVentana;
	}

}