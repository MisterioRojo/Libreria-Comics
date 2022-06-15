package Controladores;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Funcionamiento.NavegacionVentanas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class VentanaModificarController {

    @FXML
    private TextField anioPublicacion;

    @FXML
    private Button botonLimpiarComic;

    @FXML
    private Button botonModificarDatos;

    @FXML
    private Button botonSalir;

    @FXML
    private Button botonVolver;

    @FXML
    private Label labelResultado;

    @FXML
    private TextField nombreComic;

    @FXML
    private TextField nombreDibujante;

    @FXML
    private TextField nombreEditorial;

    @FXML
    private TextField nombreFirma;

    @FXML
    private TextField nombreFormato;

    @FXML
    private TextField nombreGuionista;

    @FXML
    private TextField nombreProcedencia;

    @FXML
    private TextField nombreVariante;

    @FXML
    private TextField numeroComic;
    
	NavegacionVentanas nav = new NavegacionVentanas();


    @FXML
    void BotonLimpiarComic(ActionEvent event) {

    }

    @FXML
    void modificarDatos(ActionEvent event) {

    }

	 /**
	  * Permite salir completamente del programa.
	  * @param event
	  */
	 @FXML
	 public void salirPrograma(ActionEvent event) {

		 Alert alert = new Alert(AlertType.CONFIRMATION);
		 alert.setTitle("Saliendo");
		 alert.setHeaderText("Estas apunto de salir.");
		 alert.setContentText("¿Estas seguro que quieres salir?");

		 if (alert.showAndWait().get() == ButtonType.OK) {
			 Stage myStage = (Stage) this.botonSalir.getScene().getWindow();
			 myStage.close();
		 }
	 }

		/**
		 * Permite volver al menu de conexion a la base de datos.
		 * @param event
		 * @throws IOException
		 */
		 @FXML
		 void volverAlMenu(ActionEvent event) throws IOException {

			 // Ciero la ventana donde estoy
			 Stage myStage = (Stage) this.botonVolver.getScene().getWindow();
			 myStage.close();
		 }
    
	 /**
	  * Al cerrar la ventana, se cargara la ventana de verBBDD
	  */
	 public void closeWindows() {

		 try {
			 FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanas/ModificarComicBBDD.fxml"));

			 Parent root = loader.load();

			 Scene scene = new Scene(root);
			 Stage stage = new Stage();

			 stage.setScene(scene);
			 stage.show();

			 Stage myStage = (Stage) this.botonVolver.getScene().getWindow();
			 myStage.close();

		 } catch (IOException ex) {
			 Logger.getLogger(MenuPrincipalController.class.getName()).log(Level.SEVERE, null, ex);
		 }
	 }

}
