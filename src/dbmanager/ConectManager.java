/**
 * Contiene las clases que hacen funcionar el acceso de la base de datos y las diferentes funciones para el uso de la misma
 *  
*/
package dbmanager;

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
 *
 *  Esta clase permite conectarse a la base de datos
 *
 *  Version 8.0.0.0
 *
 *  Por Alejandro Rodriguez
 *
 *  Twitter: @silverAlox
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import Funcionamiento.Utilidades;
import Funcionamiento.Ventanas;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Clase que gestiona la conexión a la base de datos y proporciona métodos para
 * cargar el controlador JDBC, verificar la conexión y obtener una conexión
 * activa.
 */
public class ConectManager {

	// Conexion a la base de datos
	/**
	 * Conexión a la base de datos.
	 */
	private static Connection conn = null;

	/**
	 * Ventanas de la aplicación.
	 */
	private static Ventanas nav = new Ventanas();

	/**
	 * Usuario de la base de datos.
	 */
	public static String DB_USER;

	/**
	 * Contraseña de la base de datos.
	 */
	public static String DB_PASS;

	/**
	 * Puerto de la base de datos.
	 */
	public static String DB_PORT;

	/**
	 * Nombre de la base de datos.
	 */
	public static String DB_NAME;

	/**
	 * Host de la base de datos.
	 */
	public static String DB_HOST;

	/**
	 * URL de la base de datos.
	 */
	public static String DB_URL;

	public static boolean estadoConexion = false;

	private static Timer checkerTimer;

	/**
	 * Carga el controlador JDBC para el proyecto.
	 * 
	 * @return true si se carga el controlador correctamente, false en caso
	 *         contrario.
	 */
	public static boolean loadDriver() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return true;
		} catch (ClassNotFoundException ex) {
			nav.alertaException(ex.toString());
		}
		return false;

	}

	/**
	 * Comprueba la conexion y muestra su estado por pantalla
	 *
	 * @return true si la conexión existe y es válida, false en caso contrario
	 */
	public static boolean isConnected() {
		try {
			if (conn != null && !conn.isClosed()) {
				return true;
			}
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		}
		return false;
	}

	/**
	 * Devuelve un objeto Connection en caso de que la conexion sea correcta, datos
	 * introducidos por parametro
	 *
	 * @param numeroPuerto
	 * @param nombreBBDD
	 * @param nombreUsuario
	 * @param contraBBDD
	 */
	public static void datosBBDD(String[] datos) {
		DB_PORT = datos[0];
		DB_NAME = datos[1];
		DB_USER = datos[2];
		DB_PASS = datos[3];
		DB_HOST = datos[4];

		conexion();
	}

	/**
	 * Comprueba si los datos de conexión a la base de datos son correctos.
	 *
	 * @return true si todos los datos de conexión son válidos y no están vacíos, de
	 *         lo contrario, false.
	 */
	public static boolean datosConexionCorrectos() {

		return !(DB_HOST == null || DB_HOST.isEmpty() || DB_PORT == null || DB_PORT.isEmpty() || DB_NAME == null
				|| DB_NAME.isEmpty() || DB_USER == null || DB_USER.isEmpty() || DB_PASS == null || DB_PASS.isEmpty());
	}

	/**
	 * Devuelve un objeto Connection en caso de que la conexion sea correcta.
	 *
	 * @param numeroPuerto
	 * @param nombreBBDD
	 * @param nombreUsuario
	 * @param contraBBDD
	 * @return objeto Connection
	 */
	public static Connection conexion() {
		if (!datosConexionCorrectos()) {
			return null;
		}

		if (!Utilidades.validarDatosConexion()) {
			nav.alertaException("La URL de conexión no es válida");
			System.out.println("Error no conectado a mysql");
			return null;
		}

		DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?serverTimezone=UTC";

		// Validar la URL de conexión
		if (!DB_URL.startsWith("jdbc:mysql://") || DB_URL.indexOf(':', 12) == -1 || DB_URL.indexOf('/', 12) == -1) {
			nav.alertaException("La URL de conexión no es válida");
			return null;
		}

		try {
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

			if (conn == null) {
				nav.alertaException("No se pudo establecer la conexión a la base de datos");
			}

			estadoConexion = true;

			if (!Utilidades.validarDatosConexion()) {
				System.out.println("SQL cerrado");
				estadoConexion = false;
				nav.alertaException("Error. Servicio MySql apagado o desconectado de forma repentina.");
				return null;
			}

			return conn;
		} catch (SQLException ex) {
			nav.alertaException("ERROR. Revisa los datos del fichero de conexión.");
		}
		return conn;
	}

	/**
	 * Método estático que restablece la conexión a la base de datos.
	 */
	public static void resetConnection() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			conn = conexion();
		} catch (SQLException ex) {
			nav.alertaException(
					"No ha sido posible restablecer la conexion a la base de datos. Parece que esta desconectada");
		}
	}

	public static void iniciarThreadCheckerConexion(Scene miSceneVentana) {
		if (checkerTimer != null) {
			checkerTimer.cancel();
		}

//	    final int[] segundos = { 0 };
		checkerTimer = new Timer(true); // Hacer el temporizador de fondo

		checkerTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					if (!Utilidades.comprobarYManejarConexion(miSceneVentana) || conexion() == null) {
						Ventanas nav = new Ventanas();
						nav.verAccesoBBDD();

						// Cierra todas las ventanas
						Platform.runLater(() -> {
							for (Window window : Window.getWindows()) {
								if (window instanceof Stage) {
									((Stage) window).close();
								}
							}
						});

						checkerTimer.cancel();
					}
				});
//	            segundos[0]++;
//	            System.out.println("Temporizador: " + segundos[0]);
			}
		}, 0, 1000); // Comienza inmediatamente y repite cada segundo
	}

	/**
	 * Asigna valores predeterminados si las variables están vacías.
	 */
	public static void asignarValoresPorDefecto() {
		DB_USER = null;

		DB_PASS = null;

		DB_PORT = null;

		DB_NAME = null;

		DB_HOST = null;
	}

	/**
	 * Método estático que cierra la conexion con la base de datos
	 */
	public static void close() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
				conn = null;
				estadoConexion = false;
			}
		} catch (SQLException ex) {
			nav.alertaException("No ha sido posible cerrar su conexion con la base de datos");
		}
	}

}