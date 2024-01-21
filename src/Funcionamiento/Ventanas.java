/**
 * Contiene las clases que hacen funcionar las diferentes funciones de uso de back end y front de todo el proyecto
 *  
*/
package Funcionamiento;

/**
 * Programa que permite el acceso a una base de datos de comics. Mediante JDBC con mySql
 * Las ventanas graficas se realizan con JavaFX.
 * El programa permite:
 *  - Conectarse a la base de datos.
 *  - Ver la base de datos completa o parcial segun parametros introducidos.
 *  - Guardar el contenido de la base de datos en un fichero .txt y .xlsx,CSV
 *  - Copia de seguridad de la base de datos en formato .sql
 *  - Introducir comics a la base de datos.
 *  - Modificar comics de la base de datos.
 *  - Eliminar comics de la base de datos(Solamente cambia el estado de "En posesion" a "Vendido". Los datos siguen en la bbdd pero estos no los muestran el programa
 *  - Ver frases de personajes de comics
 *  - Opcion de escoger algo para leer de forma aleatoria.
 *  - Puntuar comics que se encuentren dentro de la base de datos.
 *  Esta clase permite acceder al menu principal donde se puede viajar a diferentes ventanas, etc.
 *
 *  Version 8.0.0.0
 *
 *  @author Alejandro Rodriguez
 *
 */

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import Controladores.AccesoBBDDController;
import Controladores.CargaComicsController;
import Controladores.CrearBBDDController;
import Controladores.MenuLectorCodigoBarras;
import Controladores.MenuPrincipalController;
import Controladores.OpcionesDatosController;
import Controladores.RecomendacionesController;
import Controladores.SobreMiController;
import Controladores.VentanaAccionController;
import dbmanager.ConectManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Esta clase sirve para cambiar entre ventanas graficas
 *
 * @author Alejandro Rodriguez
 */
public class Ventanas {

	private Stage ventanaActual = null; // Variable para mantener la ventana actualmente abierta

	private static Alert dialog = null;

	private static Stage accesoBBDDStage = null;

	/**
	 * Abre una ventana para el acceso a la base de datos. Carga la vista y muestra
	 * una nueva ventana con el controlador correspondiente.
	 */
	/**
	 * Muestra la ventana de acceso a la base de datos.
	 */
	public void verAccesoBBDD() {
		Platform.runLater(() -> {
			if (accesoBBDDStage == null || !accesoBBDDStage.isShowing()) {
				try {
					// Cargo la vista
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/AccesoBBDD.fxml"));

					// Cargo el padre
					Parent root = loader.load();

					// Obtengo el controlador
					AccesoBBDDController controlador = loader.getController();

					// Creo la scene y el stage
					Scene scene = new Scene(root);
					scene.getStylesheets().add(getClass().getResource("/style/acces_style.css").toExternalForm());
					accesoBBDDStage = new Stage();
					accesoBBDDStage.setResizable(false);
					accesoBBDDStage.setTitle("Aplicacion bbdd comics");

					accesoBBDDStage.getIcons().add(new Image("/Icono/icon2.png"));

					// Asocio el stage con el scene
					accesoBBDDStage.setScene(scene);
					accesoBBDDStage.show();

					// Indico que debe hacer al cerrar
					accesoBBDDStage.setOnCloseRequest(e -> controlador.closeWindows());

				} catch (IOException ex) {
					alertaException(ex.toString());
				}
			}
		});
	}

	public void ventanaAbierta() {
		if (ventanaActual != null) {
			ventanaActual.close();
		}
	}

	/**
	 * Abre una ventana para realizar acciones en un cómic. Verifica si hay una
	 * ventana abierta y la cierra si es necesario. Carga la vista de la ventana de
	 * acciones del cómic y muestra la ventana correspondiente con su controlador.
	 * Define el comportamiento de cierre de la ventana y actualiza la referencia a
	 * la ventana actual.
	 */
	public void verAccionComic() {
		try {
			// Verifica si hay una ventana abierta y ciérrala si es necesario
			ventanaAbierta();

			// Cargo la vista
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/PantallaAccionComic.fxml"));

			// Cargo el padre
			Parent root = loader.load();

			// Obtengo el controlador
			VentanaAccionController controlador = loader.getController();

			// Creo la scene y el stage
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Acciones comic"); // Titulo de la aplicación.

			stage.getIcons().add(new Image("/Icono/icon2.png"));

			// Asocio el stage con el scene
			stage.setScene(scene);
			stage.show();

			// Indico que debe hacer al cerrar
			stage.setOnCloseRequest(e -> {
				controlador.closeWindow();
				ventanaActual = null; // Establece la ventana actual a null cuando se cierra
			});

			// Actualizar el estado de la ventana abierta
			ventanaActual = stage;
			ConectManager.resetConnection();
		} catch (IOException ex) {
			alertaException(ex.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * Abre la ventana de recomendaciones de cómics. Carga la vista de la ventana de
	 * recomendaciones y muestra la ventana correspondiente con su controlador.
	 * Define el tamaño de la ventana, la asocia con el comportamiento de cierre y
	 * resetea la conexión a la base de datos.
	 */
	public void verMenuCodigosBarra() {
		try {
			Platform.runLater(() -> {
				try {

					// Verifica si hay una ventana abierta y ciérrala si es necesario
					ventanaAbierta();

					// Load the view
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/ScannerComic.fxml"));

					// Load the parent
					Parent root = loader.load();

					// Get the controller
					MenuLectorCodigoBarras controlador = loader.getController();

					// Create the scene and the stage
					Scene scene = new Scene(root);

					Stage stage = new Stage();
					stage.setResizable(false);
					stage.setTitle("Lector de barras"); // Application title.
					stage.getIcons().add(new Image("/Icono/icon2.png"));

					// Associate the stage with the scene
					stage.setScene(scene);
					stage.show();

					// Indico que debe hacer al cerrar
					stage.setOnCloseRequest(e -> {
						controlador.closeWindow();
						ventanaActual = null; // Establece la ventana actual a null cuando se cierra
					});

					// Actualizar el estado de la ventana abierta
					ventanaActual = stage;

				} catch (IOException ex) {
					alertaException(ex.toString());
				}
				ConectManager.resetConnection();
			});
		} catch (Exception e) {
			alertaException(e.toString());
		}
	}

	/**
	 * Abre la ventana del menú principal de la aplicación. Carga la vista de la
	 * ventana del menú principal y muestra la ventana correspondiente con su
	 * controlador. Define el tamaño mínimo y máximo de la ventana según la
	 * resolución de la pantalla. Asocia el comportamiento de cierre de la ventana.
	 */
	public void verMenuPrincipal() {
		try {
			// Cargo la vista
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/MenuPrincipal.fxml"));

			// Cargo el padre
			Parent root = loader.load();

			// Obtengo el controlador
			MenuPrincipalController controlador = loader.getController();

			// Crea la escena
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/style/custom-combobox.css").toExternalForm());

			Stage stage = new Stage();

			// Determine the screen's dimensions
			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();

			double minWidth = 974;
			double minHeight = 607;

			// Set the minimum size
			stage.setMinWidth(minWidth);
			stage.setMinHeight(minHeight);

			// Verifica si la resolución es menor que las dimensiones mínimas
			if (bounds.getWidth() <= minWidth || bounds.getHeight() <= minHeight) {
				stage.setWidth(minWidth);
				stage.setHeight(minHeight);
			}

			// Set the maximum size to the screen's size
			stage.setMaxWidth(bounds.getWidth());
			stage.setMaxHeight(bounds.getHeight());

			stage.setTitle("Menu principal");
			stage.getIcons().add(new Image("/Icono/icon2.png"));

			// Asocio el stage con el scene
			stage.setScene(scene);
			stage.show();

			// Indico qué hacer al cerrar
			stage.setOnCloseRequest(e -> {
				controlador.closeWindows();
			});

		} catch (IOException ex) {
			alertaException(ex.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * Abre la ventana de recomendaciones de cómics. Carga la vista de la ventana de
	 * recomendaciones y muestra la ventana correspondiente con su controlador.
	 * Define el tamaño de la ventana, la asocia con el comportamiento de cierre y
	 * resetea la conexión a la base de datos.
	 */
	public void verRecomendacion() {

		try {
			// Cargo la vista
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/RecomendacionComic.fxml"));

			// Cargo el padre
			Parent root = loader.load();

			// Obtengo el controlador
			RecomendacionesController controlador = loader.getController();

			// Crea la escena y el escenario
			Scene scene = new Scene(root);

			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Recomendaciones"); // Titulo de la aplicacion.
			stage.getIcons().add(new Image("/Icono/icon2.png"));

			// Asocio el stage con el scene
			stage.setScene(scene);
			stage.show();
			// Indico que debe hacer al cerrar
			stage.setOnCloseRequest(e -> {
				controlador.closeWindows();
			});

			ConectManager.resetConnection();

		} catch (IOException ex) {
			alertaException(ex.toString());
		}
	}

	/**
	 * Abre la ventana de creación de la base de datos. Carga la vista de la ventana
	 * de creación de la base de datos y muestra la ventana correspondiente con su
	 * controlador. Define el tamaño de la ventana, la asocia con el comportamiento
	 * de cierre y aplica estilos de hojas de estilo.
	 */
	public void verCrearBBDD() {

		try {
			// Cargo la vista
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/CrearBBDD.fxml"));

			// Cargo el padre
			Parent root = loader.load();

			// Obtengo el controlador
			CrearBBDDController controlador = loader.getController();

			// Crea la escena y el escenario
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/style/opciones_style.css").toExternalForm());

			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Menu de creacion"); // Titulo de la aplicacion.
			stage.getIcons().add(new Image("/Icono/icon2.png"));

			// Asocio el stage con el scene
			stage.setScene(scene);
			stage.show();
			// Indico que debe hacer al cerrar
			stage.setOnCloseRequest(e -> {
				controlador.closeWindows();
			});

		} catch (IOException ex) {
			alertaException(ex.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * Abre la ventana de opciones de acceso. Carga la vista de la ventana de
	 * opciones de acceso y muestra la ventana correspondiente con su controlador.
	 * Define el tamaño de la ventana, la asocia con el comportamiento de cierre y
	 * aplica estilos de hojas de estilo.
	 */
	public void verOpciones() {

		try {
			// Cargo la vista
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/OpcionesAcceso.fxml"));

			// Cargo el padre
			Parent root = loader.load();

			// Obtengo el controlador
			OpcionesDatosController controlador = loader.getController();

			// Crea la escena y el escenario
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/style/acces_style.css").toExternalForm());
			scene.getStylesheets().add(getClass().getResource("/style/opciones_style.css").toExternalForm());

			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Opciones"); // Titulo de la aplicacion.
			stage.getIcons().add(new Image("/Icono/icon2.png"));

			// Asocio el stage con el scene
			stage.setScene(scene);
			stage.show();
			// Indico que debe hacer al cerrar
			stage.setOnCloseRequest(e -> {
				controlador.closeWindows();
			});
		} catch (IOException ex) {
			alertaException(ex.toString());
		}
	}

	/**
	 * Abre la ventana "Sobre Mi". Carga la vista de la ventana "Sobre Mi" y muestra
	 * la ventana correspondiente con su controlador. Define el tamaño de la
	 * ventana, la asocia con el comportamiento de cierre y aplica estilos de hojas
	 * de estilo. Además, resetea la conexión a la base de datos.
	 */
	public void verSobreMi() {

		try {
			// Cargo la vista
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/SobreMi.fxml"));

			// Cargo el padre
			Parent root = loader.load();

			// Obtengo el controlador
			SobreMiController controlador = loader.getController();

			// Crea la escena y el escenario
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/style/sobremi_style.css").toExternalForm());
			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Sobre mi"); // Titulo de la aplicacion.
			stage.getIcons().add(new Image("/Icono/icon2.png"));

			// Asocio el stage con el scene
			stage.setScene(scene);
			stage.show();
			// Indico que debe hacer al cerrar
			stage.setOnCloseRequest(e -> {
				controlador.closeWindows();
			});
			ConectManager.resetConnection();

		} catch (IOException ex) {
			alertaException(ex.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * Muestra una ventana de carga para la carga de cómics.
	 */
	public void verCargaComics(AtomicReference<CargaComicsController> cargaComicsControllerRef) {
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/PantallaCargaComics.fxml"));
//				loader.setController(this);  // Make sure this line is present
				Parent root = loader.load();

				CargaComicsController cargaComicsController = loader.getController();
				cargaComicsControllerRef.set(cargaComicsController);

				Scene scene = new Scene(root);
				Stage stage = new Stage();
				stage.setResizable(false);
				stage.setTitle("Carga de comics"); // Titulo de la aplicacion.
				stage.getIcons().add(new Image("/Icono/icon2.png"));

				// Indico que debe hacer al cerrar
				stage.setOnCloseRequest(e -> {
					cargaComicsController.closeWindow();
				});

				// Asocio el stage con el scene
				stage.setScene(scene);
				stage.show();

				// Now you can call methods on cargaComicsController
//				cargaComicsController.cargarDatosEnCargaComics("", "", 0.0); // Call the data passing function

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
	}

	public static void cerrarVentanaActual(Stage nodoEscena) {
		Stage stage = (Stage) nodoEscena.getScene().getWindow(); // Reemplaza tuNodoDeEscena con el nodo de tu
																	// escena actual
		stage.close();
	}

	/**
	 * Permite salir del programa completamente
	 *
	 * @param event
	 * @return
	 */
	public boolean salirPrograma(ActionEvent event) {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.setResizable(false);
		stage.getIcons().add(new Image("/Icono/exit.png")); // To add an icon
		alert.setTitle("Saliendo");
		alert.setHeaderText("Estas apunto de salir.");
		alert.setContentText(" Estas seguro que quieres salir?");

		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Llama a una ventana de alarma para eliminar datos
	 *
	 * @return
	 */
	public boolean alertaAccionGeneral() {
		Alert alert = new Alert(AlertType.CONFIRMATION);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Accion . . .");
		alert.setHeaderText("Vas a realizar una accion.");
		alert.setContentText("¿Estas seguro que quieres realizar la accion para el comic?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma para eliminar datos
	 *
	 * @return
	 */
	public boolean alertaEliminar() {
		Alert alert = new Alert(AlertType.CONFIRMATION);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Eliminando . . .");
		alert.setHeaderText("Estas apunto de eliminar datos.");
		alert.setContentText(" Estas seguro que quieres eliminar el comic?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma que avisa si se va a modificar un dato
	 *
	 * @return
	 */
	public boolean alertaModificar() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Modificando . . .");
		alert.setHeaderText("Estas apunto de modificar datos.");
		alert.setContentText(" Estas seguro que quieres modificar el comic?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma que avisa si se va a modificar un dato
	 *
	 * @return
	 */
	public boolean alertaBorrarPuntuacion() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Borrando puntuacion . . .");
		alert.setHeaderText("Estas apunto de borrar la puntuacion.");
		alert.setContentText(" Estas seguro que borrar la puntuacion del comic?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma que avisa si se va a modificar un dato
	 *
	 * @return
	 */
	public boolean alertaAgregarPuntuacion() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Agregando puntuacion . . .");
		alert.setHeaderText("Estas apunto de agregar la puntuacion.");
		alert.setContentText(" Estas seguro que agregar la puntuacion del comic?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma que avisa si se va a introducir un dato
	 *
	 * @return
	 */
	public boolean alertaInsertar() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Insertando . . .");
		alert.setHeaderText("Estas apunto de introducir datos.");
		alert.setContentText(" Estas seguro que quieres introducir el comic/comics?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma que avisa si se va a introducir un dato
	 *
	 * @return
	 */
	public boolean alertaPortadaVacia() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Insertando . . .");
		alert.setHeaderText("Parece que te has olvidado de la portada.");
		alert.setContentText(" Estas seguro que quieres introducir el comic sin portada??");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Llama a una ventana de alarma que avisa si se va reconstruir la base de datos
	 *
	 * @return
	 */
	public boolean alertaTablaError() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		stage.setResizable(false);
		alert.setTitle("Tablas no encontradas");
		alert.setHeaderText("ERROR. No tienes la base de datos bien construida.");
		alert.setContentText("¿Quieres recontruir la base de datos?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		}
		return false;
	}

	/**
	 * Solicita confirmación al usuario antes de borrar el contenido de la tabla.
	 *
	 * @return Un objeto CompletableFuture que se completará con true si el usuario
	 *         confirma la eliminación, o con false si el usuario cancela la
	 *         operación.
	 */
	public CompletableFuture<Boolean> borrarContenidoTabla() {
		CompletableFuture<Boolean> futureResult = new CompletableFuture<>();

		Platform.runLater(() -> {
			Alert alert1 = new Alert(AlertType.CONFIRMATION);
			Stage stage1 = (Stage) alert1.getDialogPane().getScene().getWindow();
			stage1.setResizable(false);
			stage1.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
			alert1.setTitle("Borrando . . .");
			alert1.setHeaderText("Estás a punto de borrar el contenido.");
			alert1.setContentText("¿Estás seguro que quieres borrarlo todo?");

			Optional<ButtonType> result1 = alert1.showAndWait();
			if (result1.isPresent() && result1.get() == ButtonType.OK) {
				Alert alert2 = new Alert(AlertType.CONFIRMATION);
				Stage stage2 = (Stage) alert2.getDialogPane().getScene().getWindow();
				stage2.setResizable(false);
				stage2.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
				alert2.setTitle("Borrando . . .");
				alert2.setHeaderText("¿Estás seguro?");
				alert2.setContentText("¿De verdad de verdad quieres borrarlo todo?");

				alert2.showAndWait().ifPresent(result2 -> {
					if (result2 == ButtonType.OK) {
						futureResult.complete(true);
					} else {
						futureResult.complete(false);
					}
				});
			} else {
				futureResult.complete(false);
			}
		});

		return futureResult;
	}

	/**
	 * Solicita confirmación al usuario antes de borrar el contenido de la
	 * configuración.
	 *
	 * @return true si el usuario confirma la eliminación, o false si el usuario
	 *         cancela la operación.
	 */
	public boolean borrarContenidoConfiguracion() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.setResizable(false);
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		alert.setTitle("Borrando . . .");
		alert.setHeaderText("Estas a punto de borrar el contenido.");
		alert.setContentText("¿Estas seguro que quieres restaurar la configuracion original?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Solicita confirmación al usuario antes de borrar el contenido de la lista de
	 * comics guardados.
	 *
	 * @return true si el usuario confirma la eliminación, o false si el usuario
	 *         cancela la operación.
	 */
	public boolean borrarListaGuardada() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.setResizable(false);
		stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
		alert.setTitle("Borrando . . .");
		alert.setHeaderText("Estas a punto de borrar el contenido.");
		alert.setContentText("¿Estas seguro que quieres borrar la lista guardada?");
		if (alert.showAndWait().get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Solicita confirmación al usuario antes de cancelar la subida de portadas.
	 *
	 * @return Un objeto CompletableFuture que se completará con true si el usuario
	 *         confirma la cancelación, o con false si el usuario decide continuar
	 *         la subida.
	 */
	public CompletableFuture<Boolean> cancelar_subida_portadas() {
		CompletableFuture<Boolean> futureResult = new CompletableFuture<>();

		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(new Image("/Icono/warning.jpg")); // To add an icon
			alert.setTitle("Subiendo datos . . .");
			alert.setHeaderText("Estas apunto de seleccionar la carpeta con las portadas");
			alert.setContentText("¿Quieres continuar? En caso de cancelar, se subirán portadas predeterminadas.");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				futureResult.complete(true);
			} else {
				futureResult.complete(false);
			}
		});

		return futureResult;
	}

	/**
	 * Llama a una ventana de alarma que avisa si hay una excepción.
	 *
	 * @param excepcion La excepción que se mostrará en la ventana de alerta.
	 */
	public void alertaException(String excepcion) {
		Platform.runLater(() -> {
			if (dialog == null || !dialog.isShowing()) {
				dialog = new Alert(AlertType.ERROR, excepcion, ButtonType.OK);
				Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
				stage.getIcons().add(new Image("/Icono/icon.png")); // Reemplaza con la ruta de tu icono

				dialog.showAndWait(); // Mostrar y esperar hasta que se cierre
			}
		});
	}

	/**
	 * Llama a una ventana de alarma que avisa si hay una excepcion.
	 *
	 * @param excepcion
	 */
	public void alertaNoApi(String excepcion) {
		Platform.runLater(() -> {
			Alert dialog = new Alert(AlertType.ERROR, excepcion, ButtonType.OK);
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			stage.getIcons().add(new Image("/Icono/icon.png")); // Reemplaza "path/to/your/icon.png" con la ruta de tu
																// icono

			dialog.show();
		});
	}
}
