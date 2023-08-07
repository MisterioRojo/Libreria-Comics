package JDBC;

import java.awt.Desktop;

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
 * Esta clase permite realizar diferentes operaciones pertinentes a la base de datos y tambien sirve para realizar las diferentes operaciones en la base de datos
 * que tenga que ver con la libreria de comics
 *
 *  Version Final
 *
 *  Por Alejandro Rodriguez
 *
 *  Twitter: @silverAlox
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import Funcionamiento.Comic;
import Funcionamiento.FuncionesExcel;
import Funcionamiento.Utilidades;
import Funcionamiento.Ventanas;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 * Esta clase sirve para realizar diferentes operaciones que tengan que ver con
 * la base de datos.
 *
 * @author Alejandro Rodriguez
 */
public class DBLibreriaManager extends Comic {

	public static List<Comic> listaComics = new ArrayList<>();
	public static List<Comic> listaComicsCheck = new ArrayList<>();

	public static List<String> listaNombre = new ArrayList<>();
	public static List<String> listaNumeroComic = new ArrayList<>();
	public static List<String> listaVariante = new ArrayList<>();
	public static List<String> listaFirma = new ArrayList<>();
	public static List<String> listaFormato = new ArrayList<>();
	public static List<String> listaEditorial = new ArrayList<>();
	public static List<String> listaGuionista = new ArrayList<>();
	public static List<String> listaDibujante = new ArrayList<>();
	public static List<String> listaFecha = new ArrayList<>();
	public static List<String> listaProcedencia = new ArrayList<>();
	public static List<String> listaCaja = new ArrayList<>();

	public static ArrayList<String> nombreComicList = new ArrayList<>();
	public static ArrayList<String> numeroComicList = new ArrayList<>();
	public static ArrayList<String> nombreFirmaList = new ArrayList<>();
	public static ArrayList<String> nombreGuionistaList = new ArrayList<>();
	public static ArrayList<String> nombreVarianteList = new ArrayList<>();
	public static ArrayList<String> numeroCajaList = new ArrayList<>();
	public static ArrayList<String> nombreProcedenciaList = new ArrayList<>();
	public static ArrayList<String> nombreFormatoList = new ArrayList<>();
	public static ArrayList<String> nombreEditorialList = new ArrayList<>();
	public static ArrayList<String> nombreDibujanteList = new ArrayList<>();

	private static Ventanas nav = new Ventanas();
	private static Connection conn = null;
	private static Utilidades utilidad = new Utilidades();
	private static FuncionesExcel carpeta = new FuncionesExcel();
	private static Ventanas ventanas = new Ventanas();

	/**
	 * @throws SQLException
	 *
	 */
	public void listasAutoCompletado() throws SQLException {
		listaNombre();
		listaVariante();
		listaFirma();
		listaFormato();
		listaEditorial();
		listaGuionista();
		listaDibujante();
		listaProcedencia();
		listaCajas();
		listaNumeroComic();
	}

	public static void limpiarListas() {
		nombreComicList.clear();
		numeroComicList.clear();
		nombreFirmaList.clear();
		nombreGuionistaList.clear();
		nombreVarianteList.clear();
		numeroCajaList.clear();
		nombreProcedenciaList.clear();
		nombreFormatoList.clear();
		nombreEditorialList.clear();
		nombreDibujanteList.clear();
	}

	/**
	 * Funcion que permite contar cuantas filas hay en la base de datos.
	 *
	 * @return
	 */
	public int countRows() {
		conn = DBManager.conexion();
		String sql = "SELECT COUNT(*) FROM comicsbbdd";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			int total = -1;

			total = rs.getRow();

			return total;
		} catch (SQLException e) {
			nav.alertaException(e.toString());
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				nav.alertaException(e.toString());
			}
		}
		return 0;
	}

	/**
	 * Borra el contenido de la base de datos.
	 *
	 * @return
	 * @throws SQLException
	 */
	public CompletableFuture<Boolean> deleteTable() {
		CompletableFuture<Boolean> futureResult = new CompletableFuture<>();

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					String sentencia[] = new String[2];
					sentencia[0] = "delete from comicsbbdd";
					sentencia[1] = "alter table comicsbbdd AUTO_INCREMENT = 1;";

					utilidad.copia_seguridad();
					utilidad.eliminarArchivosEnCarpeta();
					listaNombre.clear();
					listaNumeroComic.clear();
					listaVariante.clear();
					listaFirma.clear();
					listaEditorial.clear();
					listaGuionista.clear();
					listaDibujante.clear();
					listaFecha.clear();
					listaFormato.clear();
					listaProcedencia.clear();
					listaCaja.clear();

					// Ejecutar el PreparedStatement asíncronamente
					CompletableFuture<Boolean> ejecucionResult = ejecutarPreparedStatementAsync(sentencia);
					boolean ejecucionExitosa = ejecucionResult.join();

					futureResult.complete(ejecucionExitosa); // Completar la CompletableFuture con el resultado
				} catch (Exception e) {
					futureResult.completeExceptionally(e);
				}
				return null;
			}
		};

		task.setOnFailed(e -> futureResult.completeExceptionally(task.getException()));

		Thread thread = new Thread(task);
		thread.start();

		return futureResult;
	}

	public boolean contenidoTabla() {
		String sql = "SELECT COUNT(*) FROM comicsbbdd";
		boolean existeMasDeUnRegistro = false;

		try {
			// Obtener la conexión a la base de datos y crear la sentencia
			Connection conn = DBManager.conexion();
			Statement statement = conn.createStatement();

			// Ejecutar la consulta y obtener el resultado
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				int count = resultSet.getInt(1);
				existeMasDeUnRegistro = count >= 1;
			}

			// Cerrar recursos
			resultSet.close();
			statement.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// Manejar la excepción según tus necesidades
		}

		return existeMasDeUnRegistro;
	}

	/**
	 * Función que ejecuta un conjunto de sentencias PreparedStatement en la base de
	 * datos.
	 * 
	 * @param sentencia Un arreglo de cadenas que contiene las sentencias SQL a
	 *                  ejecutar.
	 * @return true si las sentencias se ejecutaron correctamente, false en caso
	 *         contrario.
	 */
	public CompletableFuture<Boolean> ejecutarPreparedStatementAsync(String[] sentencia) {
		CompletableFuture<Boolean> futureResult = new CompletableFuture<>();

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					conn = DBManager.conexion();
					PreparedStatement statement1 = conn.prepareStatement(sentencia[0]);
					PreparedStatement statement2 = conn.prepareStatement(sentencia[1]);
					statement1.executeUpdate();
					statement2.executeUpdate();
					statement1.close();
					statement2.close();
					futureResult.complete(true); // Si llega hasta aquí, se asume éxito
				} catch (Exception e) {
					futureResult.completeExceptionally(e);
				}
				return null;
			}
		};

		task.setOnFailed(e -> futureResult.completeExceptionally(task.getException()));

		Thread thread = new Thread(task);
		thread.start();

		return futureResult;
	}

	/////////////////////////////////
	//// FUNCIONES CREACION FICHEROS//
	/////////////////////////////////

	/**
	 * Funcion que crea una copia de seguridad de la base de datos siempre que el
	 * sistema operativo sea Linux
	 *
	 * @param fichero
	 */
	public void backupLinux(File fichero) {
		try {
			fichero.createNewFile();
			String command[] = new String[] { "mysqldump", "-u" + DBManager.DB_USER, "-p" + DBManager.DB_PASS, "-B",
					DBManager.DB_NAME, "--routines=true", "--result-file=" + fichero };
			ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.to(fichero));
			pb.start();

		} catch (IOException e) {
			nav.alertaException(e.toString());
		}
	}

	/**
	 * Funcion que crea una copia de seguridad de la base de datos siempre que el
	 * sistema operativo sea Windows
	 *
	 * @param fichero
	 */
	public void backupWindows(File fichero) {
		try {
			fichero.createNewFile();

			String pathMySql = "C:\\Program Files\\MySQL";

			File path = new File(pathMySql);

			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(path);
			fileChooser.getExtensionFilters()
					.addAll(new FileChooser.ExtensionFilter("MySqlDump only", "mysqldump.exe"));
			File directorio = fileChooser.showOpenDialog(null);

			String mysqlDump = directorio.getAbsolutePath();

			String command[] = new String[] { mysqlDump, "-u" + DBManager.DB_USER, "-p" + DBManager.DB_PASS, "-B",
					DBManager.DB_NAME, "--hex-blob", "--routines=true", "--result-file=" + fichero };
			ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.to(fichero));
			pb.start();

		} catch (Exception e) {
			nav.alertaException(e.toString());
		}
	}

	/**
	 * Permite devolver datos de la base de datos segun la query en parametros
	 *
	 * @param procedimiento
	 * @return
	 * @throws SQLException
	 */
	public ResultSet ejecucionSQL(String procedimiento) throws SQLException {
		ResultSet rs = null;
		Statement st = null;
		Connection conn = DBManager.conexion();

		try {
			st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery(procedimiento);
			return rs;
		} catch (SQLException e) {
			nav.alertaException(e.toString());
		}
		return null;
	}

	/**
	 * Función que guarda los datos para autocompletado en una lista.
	 * 
	 * @param sentenciaSQL La sentencia SQL para obtener los datos.
	 * @param columna      El nombre de la columna que contiene los datos para
	 *                     autocompletado.
	 * @return Una lista de cadenas con los datos para autocompletado.
	 * @throws SQLException Si ocurre algún error al ejecutar la consulta SQL.
	 */
	private List<String> guardarDatosAutoCompletado(String sentenciaSQL, String columna) throws SQLException {
		List<String> listaAutoCompletado = new ArrayList<>();
		listaAutoCompletado.clear();
		ResultSet rs = obtenLibreria(sentenciaSQL);
		try {
			if (verLibreria("SELECT * FROM comicsbbdd").size() != 0) {
				do {
					String datosAutocompletado = rs.getString(columna);
					if (columna.equals("nomComic")) {
						listaAutoCompletado.add(datosAutocompletado.trim());
					} else {
						String[] nombres = datosAutocompletado.split("-"); // Dividir nombres separados por "-"
						for (String nombre : nombres) {
							nombre = nombre.trim(); // Eliminar espacios al inicio y final
							if (!nombre.isEmpty()) {
								listaAutoCompletado.add(nombre); // Agregar a la lista solo si no está vacío
							}
						}
					}
				} while (rs.next());
				listaAutoCompletado = Utilidades.listaArregladaAutoComplete(listaAutoCompletado);

				return listaAutoCompletado;
			}
		} catch (SQLException e) {
			nav.alertaException(e.toString());
		}
		return listaAutoCompletado;
	}

	// **************************************//
	// ****FUNCIONES DE LA LIBRERIA**********//
	// **************************************//

	/**
	 * Método estático que obtiene el último ID de la tabla "comicsbbdd".
	 * 
	 * @return El último ID como una cadena de texto.
	 * @throws SQLException Si ocurre algún error al ejecutar la consulta SQL.
	 */
	public static String obtener_ultimo_id() throws SQLException {
		String query = "SELECT ID FROM comicsbbdd ORDER BY ID DESC LIMIT 1";
		PreparedStatement ps = null;
		ResultSet resultado;
		String ultimoId = "1"; // Valor predeterminado si no se encuentra ningún resultado

		try {
			ps = conn.prepareStatement(query);
			resultado = ps.executeQuery();

			if (resultado.next()) {
				int id = Integer.parseInt(resultado.getString("ID"));
				ultimoId = String.valueOf(id + 1);
			}

			resultado.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ps.close();
		}

		return ultimoId;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaNombre() throws SQLException {

		String sentenciaSQL = "SELECT nomComic from comicsbbdd ORDER BY nomComic ASC";
		String columna = "nomComic";
		reiniciarBBDD();
		listaNombre = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaNombre;

	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaNumeroComic() throws SQLException {

		String sentenciaSQL = "SELECT numComic from comicsbbdd ORDER BY numComic ASC";
		String columna = "numComic";
		reiniciarBBDD();
		listaNumeroComic = guardarDatosAutoCompletado(sentenciaSQL, columna);

		return listaNumeroComic;

	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaCajas() throws SQLException {

		String sentenciaSQL = "SELECT caja_deposito FROM comicsbbdd ORDER BY caja_deposito ASC";
		String columna = "caja_deposito";
		reiniciarBBDD();
		listaCaja = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaCaja;

	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaProcedencia() throws SQLException {

		String sentenciaSQL = "SELECT procedencia from comicsbbdd ORDER BY procedencia ASC";
		String columna = "procedencia";
		reiniciarBBDD();
		listaProcedencia = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaProcedencia;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaVariante() throws SQLException {

		String sentenciaSQL = "SELECT nomVariante from comicsbbdd ORDER BY nomVariante ASC";
		String columna = "nomVariante";
		reiniciarBBDD();
		listaVariante = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaVariante;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaFirma() throws SQLException {

		String sentenciaSQL = "SELECT firma from comicsbbdd ORDER BY firma ASC";
		String columna = "firma";
		reiniciarBBDD();
		listaFirma = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaFirma;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaFormato() throws SQLException {

		String sentenciaSQL = "SELECT formato from comicsbbdd ORDER BY formato ASC";
		String columna = "formato";
		reiniciarBBDD();
		listaFormato = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaFormato;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaEditorial() throws SQLException {

		String sentenciaSQL = "SELECT nomEditorial from comicsbbdd ORDER BY nomEditorial ASC";
		String columna = "nomEditorial";
		reiniciarBBDD();
		listaEditorial = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaEditorial;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaGuionista() throws SQLException {

		String sentenciaSQL = "SELECT nomGuionista from comicsbbdd ORDER BY nomGuionista ASC";
		String columna = "nomGuionista";
		reiniciarBBDD();
		listaGuionista = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaGuionista;
	}

	/**
	 * Devuelve todos los datos de la base de datos, tanto vendidos como no vendidos
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<String> listaDibujante() throws SQLException {

		String sentenciaSQL = "SELECT nomDibujante from comicsbbdd ORDER BY nomDibujante ASC";
		String columna = "nomDibujante";
		reiniciarBBDD();
		listaDibujante = guardarDatosAutoCompletado(sentenciaSQL, columna);
		return listaDibujante;
	}

	/**
	 * Método que muestra los cómics de la librería según la sentencia SQL
	 * proporcionada.
	 * 
	 * @param sentenciaSQL La sentencia SQL para obtener los cómics de la librería.
	 * @return Una lista de objetos Comic que representan los cómics de la librería.
	 * @throws SQLException Si ocurre algún error al ejecutar la consulta SQL.
	 */
	public List<Comic> verLibreria(String sentenciaSQL) {
		listaComics.clear(); // Limpiar la lista existente de cómics

		ResultSet rs = obtenLibreria(sentenciaSQL); // Obtener el ResultSet

		if (rs != null) {
			listaDatos(rs); // Llenar la lista de cómics con los datos del ResultSet
		}

		return listaComics;
	}

	/**
	 * Devuelve datos de la base de datos segun el parametro.
	 *
	 * @param datos
	 * @return
	 */
	public List<Comic> filtadroBBDD(Comic datos) {

		reiniciarBBDD();

		String sql = datosConcatenados(datos);
		Connection conn = DBManager.conexion();

		listaComics.clear();

		if (sql.length() != 0) {
			try {
				PreparedStatement ps = conn.prepareStatement(sql);

				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					listaDatos(rs);
				}
				return listaComics;
			} catch (SQLException ex) {
				nav.alertaException(ex.toString());
			}
		}
		return null;
	}

	/**
	 * Funcion que segun los datos introducir mediante parametros, concatenara las
	 * siguientes cadenas de texto. Sirve para hacer busqueda en una base de datos
	 *
	 * @param datos
	 * @return
	 */
	public String datosConcatenados(Comic comic) {

		int datosRellenados = 0;

		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM comicsbbdd");

		if (comic.getID().length() != 0) {

			sql.append(connector).append("ID = " + comic.getID());
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getNombre().length() != 0) {

			sql.append(connector).append("nomComic like'%" + comic.getNombre() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getNumCaja().length() != 0) {
			sql.append(connector).append("caja_deposito like'%" + comic.getNumCaja() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getNumero().length() != 0) {
			sql.append(connector).append("numComic = " + comic.getNumero());
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getVariante().length() != 0) {
			sql.append(connector).append("nomVariante like'%" + comic.getVariante() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getFirma().length() != 0) {
			sql.append(connector).append("firma like'%" + comic.getFirma() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getEditorial().length() != 0) {
			sql.append(connector).append("nomEditorial like'%" + comic.getEditorial() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getFormato().length() != 0) {
			sql.append(connector).append("formato like'%" + comic.getFormato() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getProcedencia().length() != 0) {
			sql.append(connector).append("procedencia like'%" + comic.getProcedencia() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getFecha().length() != 0) {
			sql.append(connector).append("fecha_publicacion like'%" + comic.getFecha() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getGuionista().length() != 0) {
			sql.append(connector).append("nomGuionista like'%" + comic.getGuionista() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getDibujante().length() != 0) {
			sql.append(connector).append("nomDibujante like'%" + comic.getDibujante() + "%'");
			connector = " AND ";
			datosRellenados++;
		}

		if (datosRellenados != 0) {
			return sql.toString();
		}

		return "";
	}

	public boolean numeroResultados(Comic comic) {
		int datosRellenados = 0;

		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM comicsbbdd");

		if (comic.getID().length() != 0) {
			sql.append(connector).append("ID = " + comic.getID());
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getNombre().length() != 0) {
			sql.append(connector).append("nomComic like'%" + comic.getNombre() + "%'");
			connector = " AND ";
			datosRellenados++;
		}

		if (comic.getNumCaja().length() != 0) {
			sql.append(connector).append("caja_deposito like'%" + comic.getNumCaja() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getNumero().length() != 0) {
			sql.append(connector).append("numComic = " + comic.getNumero());
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getVariante().length() != 0) {
			sql.append(connector).append("nomVariante like'%" + comic.getVariante() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getFirma().length() != 0) {
			sql.append(connector).append("firma like'%" + comic.getFirma() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getEditorial().length() != 0) {
			sql.append(connector).append("nomEditorial like'%" + comic.getEditorial() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getFormato().length() != 0) {
			sql.append(connector).append("formato like'%" + comic.getFormato() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getProcedencia().length() != 0) {
			sql.append(connector).append("procedencia like'%" + comic.getProcedencia() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getFecha().length() != 0) {
			sql.append(connector).append("fecha_publicacion like'%" + comic.getFecha() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getGuionista().length() != 0) {
			sql.append(connector).append("nomGuionista like'%" + comic.getGuionista() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (comic.getDibujante().length() != 0) {
			sql.append(connector).append("nomDibujante like'%" + comic.getDibujante() + "%'");
			connector = " AND ";
			datosRellenados++;
		}
		if (datosRellenados != 0) {
			try {
				PreparedStatement preparedStatement = conn.prepareStatement(sql.toString());
				ResultSet resultSet = preparedStatement.executeQuery();

				if (resultSet.next()) {
					int count = resultSet.getInt(1);
					if (count > 0) {
						return true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public ArrayList<String> obtenerResultadosDeLaBaseDeDatos(String sql, String campo) {
		HashMap<String, Integer> mapa = new HashMap<>();
		ArrayList<String> resultados = new ArrayList<>();

		try (Connection conn = DBManager.conexion()) {
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String valor = resultSet.getString(campo);
				// Verificar si el valor es diferente de nulo y no está vacío
				if (valor != null && !valor.isEmpty()) {
					mapa.put(valor, mapa.getOrDefault(valor, 0) + 1);
				}
			}

			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			// En caso de excepción, simplemente imprime un mensaje en lugar de mostrar el
			// error completo
			System.err.println("Error al obtener resultados de la base de datos: " + e.getMessage());
		}

		// Si el mapa está vacío, retorna una lista vacía
		if (mapa.isEmpty()) {
			return resultados;
		}

		for (String clave : mapa.keySet()) {
			resultados.add(clave);
		}

		return resultados;
	}

	/**
	 * Funcion que permite hacer una busqueda general mediante 1 sola palabra, hace
	 * una busqueda en ciertos identificadores de la tabla.
	 *
	 * @param sentencia
	 * @return
	 * @throws SQLException
	 */
	public List<Comic> verBusquedaGeneral(String busquedaGeneral) throws SQLException {
		Connection conn = DBManager.conexion();
		String sql1 = datosGeneralesNombre(busquedaGeneral);
		String sql2 = datosGeneralesVariante(busquedaGeneral);
		String sql3 = datosGeneralesFirma(busquedaGeneral);
		String sql4 = datosGeneralesGuionista(busquedaGeneral);
		String sql5 = datosGeneralesDibujante(busquedaGeneral);

		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		PreparedStatement ps5 = null;

		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		ResultSet rs5 = null;

		reiniciarBBDD();

		listaComics.clear();

		try {
			ps1 = conn.prepareStatement(sql1);
			ps2 = conn.prepareStatement(sql2);
			ps3 = conn.prepareStatement(sql3);
			ps4 = conn.prepareStatement(sql4);
			ps5 = conn.prepareStatement(sql5);

			rs1 = ps1.executeQuery();
			rs2 = ps2.executeQuery();
			rs3 = ps3.executeQuery();
			rs4 = ps4.executeQuery();
			rs5 = ps5.executeQuery();

			if (rs1.next()) {
				listaDatos(rs1);
			}
			if (rs2.next()) {
				listaDatos(rs2);
			}
			if (rs3.next()) {
				listaDatos(rs3);
			}
			if (rs4.next()) {
				listaDatos(rs4);
			}
			if (rs5.next()) {
				listaDatos(rs5);
			}

			listaComics = Utilidades.listaArreglada(listaComics);
			return listaComics;

		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		} finally {
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
			ps5.close();

			rs1.close();
			rs2.close();
			rs3.close();
			rs4.close();
			rs5.close();
		}
		return null;
	}

	/**
	 * Funcion que hace una busqueda de un identificador en concreto de la tabla
	 *
	 * @param datos
	 * @return
	 */
	public String datosGeneralesNombre(String busquedaGeneral) {
		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM comicsbbdd ");

		sql.append(connector).append("nomComic like'%" + busquedaGeneral + "%'");

		return sql.toString();
	}

	/**
	 * Funcion que hace una busqueda de un identificador en concreto de la tabla
	 *
	 * @param datos
	 * @return
	 */
	public String datosGeneralesVariante(String busquedaGeneral) {
		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM comicsbbdd ");

		sql.append(connector).append("nomVariante like'%" + busquedaGeneral + "%'");

		return sql.toString();
	}

	/**
	 * Funcion que hace una busqueda de un identificador en concreto de la tabla
	 *
	 * @param datos
	 * @return
	 */
	public String datosGeneralesFirma(String busquedaGeneral) {
		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM comicsbbdd ");

		sql.append(connector).append("firma like'%" + busquedaGeneral + "%'");

		return sql.toString();
	}

	/**
	 * Funcion que hace una busqueda de un identificador en concreto de la tabla
	 *
	 * @param datos
	 * @return
	 */
	public String datosGeneralesGuionista(String busquedaGeneral) {
		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM comicsbbdd ");

		sql.append(connector).append("nomGuionista like'%" + busquedaGeneral + "%'");

		return sql.toString();
	}

	/**
	 * Funcion que hace una busqueda de un identificador en concreto de la tabla
	 *
	 * @param datos
	 * @return
	 */
	public String datosGeneralesDibujante(String busquedaGeneral) {
		String connector = " WHERE ";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM comicsbbdd ");

		sql.append(connector).append("nomDibujante like'%" + busquedaGeneral + "%'");

		return sql.toString();
	}

	/**
	 * Crea una lista de objetos Comic a partir del ResultSet proporcionado.
	 *
	 * @param rs el ResultSet con los datos de los cómics
	 * @return una lista de objetos Comic
	 */
	public Comic listaDatos(ResultSet rs) {
		Comic comic = new Comic();

		try {
			if (rs != null) {
				do {
					this.ID = rs.getString("ID");
					this.nombre = rs.getString("nomComic");
					this.numCaja = rs.getString("caja_deposito");
					this.numero = rs.getString("numComic");
					this.variante = rs.getString("nomVariante");
					this.firma = rs.getString("firma");
					this.editorial = rs.getString("nomEditorial");
					this.formato = rs.getString("formato");
					this.procedencia = rs.getString("procedencia");
					this.fecha = rs.getString("fecha_publicacion");
					this.guionista = rs.getString("nomGuionista");
					this.dibujante = rs.getString("nomDibujante");
					this.key_issue = rs.getString("key_issue");
					this.estado = rs.getString("estado");
					this.puntuacion = rs.getString("puntuacion");
					this.imagen = rs.getString("portada");
					this.url_referencia = rs.getString("url_referencia");
					this.precio_comic = rs.getString("precio_comic");

					comic = new Comic(this.ID, this.nombre, this.numCaja, this.numero, this.variante, this.firma,
							this.editorial, this.formato, this.procedencia, this.fecha, this.guionista, this.dibujante,
							this.estado, this.key_issue, this.puntuacion, this.imagen, this.url_referencia,
							this.precio_comic);

					listaComics.add(comic);
				} while (rs.next());
			}
		} catch (SQLException e) {
			nav.alertaException("Datos introducidos incorrectos.");
			e.printStackTrace();
		}
		return comic;
	}

	/**
	 * Comprueba si la lista de comics contiene o no algun dato
	 *
	 * @param listaComic
	 */
	public boolean checkList(List<Comic> listaComic) {
		if (listaComic.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Permite reiniciar la pantalla donde se muestran los datos
	 */
	public void reiniciarBBDD() {
		listaComics.clear();
	}

	/**
	 * Devuelve un objeto Comic cuya ID coincida con el parámetro de búsqueda.
	 *
	 * @param identificador el ID del cómic a buscar
	 * @return el objeto Comic encontrado, o null si no se encontró ningún cómic con
	 *         ese ID
	 * @throws SQLException si ocurre algún error al ejecutar la consulta SQL
	 */
	public Comic comicDatos(String identificador) throws SQLException {
		Comic comic = null;

		String sentenciaSQL = "SELECT * FROM comicsbbdd WHERE ID = ?";

		PreparedStatement statement = null;
		ResultSet rs = null;

		try {
			statement = conn.prepareStatement(sentenciaSQL);
			statement.setString(1, identificador);
			rs = statement.executeQuery();

			if (rs.next()) {
				String ID = rs.getString("ID");
				String nombre = rs.getString("nomComic");
				String numCaja = rs.getString("caja_deposito");
				String numero = rs.getString("numComic");
				String variante = rs.getString("nomVariante");
				String firma = rs.getString("firma");
				String editorial = rs.getString("nomEditorial");
				String formato = rs.getString("formato");
				String procedencia = rs.getString("procedencia");
				String fecha = rs.getString("fecha_publicacion");
				String guionista = rs.getString("nomGuionista");
				String dibujante = rs.getString("nomDibujante");
				String estado = rs.getString("estado");
				String key_issue = rs.getString("key_issue");
				String puntuacion = rs.getString("puntuacion");
				String imagen = rs.getString("portada");
				String url_referencia = rs.getString("url_referencia");
				String precio_comic = rs.getString("precio_comic");
				comic = new Comic(ID, nombre, numCaja, numero, variante, firma, editorial, formato, procedencia, fecha,
						guionista, dibujante, estado, key_issue, puntuacion, imagen, url_referencia, precio_comic);
			}
		} catch (SQLException e) {
			nav.alertaException(e.toString());
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}

		return comic;
	}

	/**
	 * Comprueba que el ID introducido existe
	 *
	 * @return
	 * @throws SQLException
	 */
	public boolean checkID(String identificador) throws SQLException {
		if (identificador.length() == 0) {
			return false;
		}

		String sentenciaSQL = "SELECT * FROM comicsbbdd WHERE ID = ?";
		conn = DBManager.conexion();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(sentenciaSQL);
			preparedStatement.setString(1, identificador);
			rs = preparedStatement.executeQuery();

			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			nav.alertaException("No existe el " + identificador + " en la base de datos.");
		} finally {
			preparedStatement.close();
			rs.close();
		}

		return false;
	}

	/**
	 * Funcion que permite generar imagenes de formato JPG a la hora de exportar la
	 * base de datos excel.
	 * 
	 * @throws SQLException
	 */
	public void saveImageFromDataBase() throws SQLException {
		String sentenciaSQL = "SELECT * FROM comicsbbdd";

		conn = DBManager.conexion();
		File directorio = carpeta.carpetaPortadas();
		InputStream input = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			preparedStatement = conn.prepareStatement(sentenciaSQL);
			rs = preparedStatement.executeQuery();

			if (directorio != null) {
				while (rs.next()) {
					String direccionImagen = rs.getString(15);

					String nombreImagen = utilidad.obtenerNombreArchivo(direccionImagen);

					File imagenArchivo = new File(direccionImagen);

					if (!imagenArchivo.exists()) {
						input = getClass().getResourceAsStream("sinPortada.jpg");
						if (input == null) {
							throw new FileNotFoundException("La imagen predeterminada no se encontró en el paquete");
						}
						imagenArchivo = File.createTempFile("tmp", ".jpg");
						imagenArchivo.deleteOnExit();
						try (OutputStream output = new FileOutputStream(imagenArchivo)) {
							byte[] buffer = new byte[4096];
							int bytesRead;
							while ((bytesRead = input.read(buffer)) != -1) {
								output.write(buffer, 0, bytesRead);
							}
						}
					} else {
						FileInputStream fileInputStream = new FileInputStream(imagenArchivo);
						FileOutputStream fileOutputStream = new FileOutputStream(
								directorio.getAbsolutePath() + "/" + nombreImagen);

						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = fileInputStream.read(buffer)) != -1) {
							fileOutputStream.write(buffer, 0, bytesRead);
						}
						fileInputStream.close();
						fileOutputStream.close();

					}
				}
			}
		} catch (SQLException | IOException e) {
			nav.alertaException(e.toString());
		} finally {
			preparedStatement.close();
			rs.close();
		}
	}

	/**
	 * Permite modificar un comic de la base de datos
	 *
	 * @param id
	 * @param sentenciaSQL
	 * @throws SQLException
	 */
	public void modificarDatos(String id, String sentenciaSQL) throws SQLException {
		PreparedStatement stmt = null;
		Connection conn = DBManager.conexion();
		String direccion_portada = obtenerDireccionPortada(id);
		Utilidades.eliminarFichero(direccion_portada);
		try {
			if (id.length() != 0) {
				stmt = conn.prepareStatement(sentenciaSQL, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE); // Permite leer y ejecutar la sentencia de MySql

				stmt.setString(1, id);
				if (stmt.executeUpdate() == 1) {
				}
			}
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		} finally {
			stmt.close();
		}
	}

	/**
	 * Funcion que permite cambiar de estado el comic a "Vendido" y hace que no se
	 * muestre en la bbdd
	 * 
	 * @throws SQLException
	 */
	public void venderComicBBDD(String id) throws SQLException {
		String sentenciaSQL;

		sentenciaSQL = "UPDATE comicsbbdd set estado = 'Vendido' where ID = ?";

		modificarDatos(id, sentenciaSQL);
	}

	/**
	 * Funcion que permite cambiar de estado el comic a "Vendido" y hace que no se
	 * muestre en la bbdd
	 * 
	 * @throws SQLException
	 */
	public void enVentaComicBBDD(String id) throws SQLException {
		String sentenciaSQL;

		sentenciaSQL = "UPDATE comicsbbdd set estado = 'En venta' where ID = ?";

		modificarDatos(id, sentenciaSQL);
	}

	/**
	 * Funcion que manda una querry de eliminar comic de la base de datos.
	 * 
	 * @throws SQLException
	 */
	public void eliminarComicBBDD(String id) throws SQLException {
		String sentenciaSQL;

		sentenciaSQL = "DELETE from comicsbbdd where ID = ?";

		modificarDatos(id, sentenciaSQL);
	}

	/**
	 * Funcion que permite obtener datos de la libreria de comics almacenada en la
	 * base de datos
	 *
	 * @param sentenciaSQL
	 * @return
	 * @throws SQLException
	 */
	public ResultSet obtenLibreria(String sentenciaSQL) {

		conn = DBManager.conexion();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sentenciaSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			rs = stmt.executeQuery();
			if (!rs.first()) {
				return null;
			}

			return rs;

		} catch (NullPointerException ex) {
			ex.printStackTrace();
			nav.alertaException(ex.toString());
		} catch (SQLException ex) {
			ex.printStackTrace();
			nav.alertaException(ex.toString());
		}
		return null;
	}

	/**
	 * Inserta los datos de un cómic en la base de datos.
	 *
	 * @param comic_datos los datos del cómic a insertar
	 * @throws IOException  si ocurre un error al manejar el archivo de imagen
	 * @throws SQLException si ocurre un error al ejecutar la consulta SQL
	 */
	public void insertarDatos(Comic comic_datos) throws IOException, SQLException {
		String sentenciaSQL = "INSERT INTO comicsbbdd (nomComic, caja_deposito,precio_comic, numComic, nomVariante, firma, nomEditorial, formato, procedencia, fecha_publicacion, nomGuionista, nomDibujante, puntuacion, portada,key_issue,url_referencia, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		subirComic(sentenciaSQL, comic_datos);
	}

	/**
	 * Permite introducir un nuevo cómic en la base de datos.
	 *
	 * @param sentenciaSQL la sentencia SQL para insertar el cómic
	 * @param datos        los datos del cómic a insertar
	 * @throws IOException  si ocurre un error al manejar el archivo de imagen
	 * @throws SQLException si ocurre un error al ejecutar la consulta SQL
	 */
	public void subirComic(String sentenciaSQL, Comic datos) throws IOException, SQLException {
		conn = DBManager.conexion();
		PreparedStatement statement = null;

		try {
			statement = conn.prepareStatement(sentenciaSQL);
			statement.setString(1, datos.getNombre());
			if (datos.getNumCaja() == null) {
				statement.setString(2, "0");
			} else {
				statement.setString(2, datos.getNumCaja());
			}
			statement.setString(3, datos.getPrecio_comic());
			statement.setString(4, datos.getNumero());
			statement.setString(5, datos.getVariante());
			statement.setString(6, datos.getFirma());
			statement.setString(7, datos.getEditorial());
			statement.setString(8, datos.getFormato());
			statement.setString(9, datos.getProcedencia());
			statement.setString(10, datos.getFecha());
			statement.setString(11, datos.getGuionista());
			statement.setString(12, datos.getDibujante());
			statement.setString(13, "Sin puntuar");
			statement.setString(14, datos.getImagen());
			statement.setString(15, datos.getKey_issue());
			statement.setString(16, datos.getUrl_referencia());
			statement.setString(17, datos.getEstado());

			statement.executeUpdate();
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	/**
	 * Comprueba si el ID introducido existe en la base de datos.
	 *
	 * @param ID el ID a comprobar
	 * @return true si el ID existe, false en caso contrario
	 */
	public boolean comprobarID(String ID) {
		if (ID.isEmpty()) {
			return false;
		}

		try {
			if (checkID(ID)) {
				return true;
			} else {
				nav.alertaException("La ID no existe");
			}
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		}

		return false;
	}

	/**
	 * Funcion que comprueba si existe el ID introducido
	 *
	 * @param ps
	 * @return
	 */
	public void actualizar_comic(Comic datos) {
		utilidad = new Utilidades();
		String sentenciaSQL = "UPDATE comicsbbdd SET nomComic = ?, caja_deposito = ?, numComic = ?, nomVariante = ?, "
		        + "Firma = ?, nomEditorial = ?, formato = ?, Procedencia = ?, fecha_publicacion = ?, "
		        + "nomGuionista = ?, nomDibujante = ?, key_issue = ?, portada = ?, estado = ?, url_referencia = ?, precio_comic = ? "
		        + "WHERE ID = ?";


		try {
			if (comprobarID(datos.getID())) { // Comprueba si la ID introducida existe en la base de datos
				comicModificar(sentenciaSQL, datos); // Llama a funcion que permite cambiar los datos del comic
			}
		} catch (SQLException | IOException ex) {
			nav.alertaException(ex.toString());
		} finally {
			DBManager.resetConnection();
		}
	}

	/**
	 * Modifica la dirección de la portada de un cómic en la base de datos.
	 *
	 * @param nombre_completo el nombre completo de la imagen de la portada
	 * @param ID              el ID del cómic a modificar
	 * @throws SQLException si ocurre un error al ejecutar la consulta SQL
	 */
	public void modificar_direccion_portada(String nombre_completo, String ID) throws SQLException {
		utilidad = new Utilidades();
		String extension = ".jpg";
		String nuevoNombreArchivo = nombre_completo + extension;

		String userDir = System.getProperty("user.home");
		String documentsPath = userDir + File.separator + "Documents";
		String imagePath = documentsPath + File.separator + "libreria_comics" + File.separator + DBManager.DB_NAME
				+ File.separator + "portadas" + File.separator + nuevoNombreArchivo;

		String sql = "UPDATE comicsbbdd SET portada = ? WHERE ID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, imagePath);
			ps.setString(2, ID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Modifica los datos de un cómic en la base de datos.
	 *
	 * @param sentenciaSQL la sentencia SQL para modificar el cómic
	 * @param datos        los nuevos datos del cómic
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws IOException  si ocurre un error de lectura/escritura al manejar las
	 *                      imágenes
	 */
	public void comicModificar(String sentenciaSQL, Comic datos) throws SQLException, IOException {
		utilidad = new Utilidades();
		listaComics.clear();

		String ID = datos.getID();
		String nombre = datos.getNombre();
		String numCaja = datos.getNumCaja();
		String numero = datos.getNumero();
		String variante = datos.getVariante();
		String firma = datos.getFirma();
		String editorial = datos.getEditorial();
		String formato = datos.getFormato();
		String procedencia = datos.getProcedencia();
		String fecha = datos.getFecha();
		String guionista = datos.getGuionista();
		String dibujante = datos.getDibujante();
		String estado = datos.getEstado();
		String portada_final = datos.getImagen();
		String key_issue = datos.getKey_issue();
		String url_referencia = datos.getUrl_referencia();
		String precio_comic = datos.getPrecio_comic();
		String codigo_imagen = Utilidades.generarCodigoUnico(portada_final + File.separator);

		PreparedStatement ps = null;
		try {
		    ps = conn.prepareStatement(sentenciaSQL);

		    ps.setString(1, nombre);
		    ps.setString(2, numCaja);
		    ps.setString(3, numero);
		    ps.setString(4, variante);
		    ps.setString(5, firma);
		    ps.setString(6, editorial);
		    ps.setString(7, formato);
		    ps.setString(8, procedencia);
		    ps.setString(9, fecha);
		    ps.setString(10, guionista);
		    ps.setString(11, dibujante);
		    ps.setString(12, key_issue);
		    ps.setString(13, portada_final);
		    ps.setString(14, estado);
		    ps.setString(15, url_referencia);
		    ps.setString(16, precio_comic);
		    ps.setString(17, ID);

			if (ps.executeUpdate() == 1) {
				Comic nuevo_comic = new Comic("", nombre, numCaja, numero, variante, firma, editorial, formato,
						procedencia, fecha, guionista, dibujante, estado, key_issue, "", portada_final, url_referencia,
						precio_comic);

				String carpeta = Utilidades.eliminarDespuesUltimoPortadas(codigo_imagen);
				utilidad.nueva_imagen(portada_final, carpeta);
				modificar_direccion_portada(codigo_imagen, datos.getID());

				listaComics.add(nuevo_comic);
			}
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
			ex.printStackTrace();
		} finally {
			if (ps != null) {
				ps.close();
			}
			DBManager.resetConnection();
		}
	}

	/**
	 * Funcion que permite insertar una puntuacion a un comic segun la ID
	 * introducida.
	 * 
	 * @throws SQLException
	 */
	public void actualizarPuntuacion(String ID, String puntuacion) throws SQLException {

		String sentenciaSQL = "UPDATE comicsbbdd set puntuacion = ? where ID = ?";

		if (nav.alertaAgregarPuntuacion()) { // Llamada a alerta de modificacion

			comprobarOpinionInsertada(sentenciaSQL, ID, puntuacion);
		}
	}

	/**
	 * Funcion que permite insertar una puntuacion a un comic segun la ID
	 * introducida.
	 * 
	 * @throws SQLException
	 */
	public void borrarPuntuacion(String ID) throws SQLException {

		String sentenciaSQL = "UPDATE comicsbbdd set puntuacion = 'Sin puntuar' where ID = ?";

		if (nav.alertaBorrarPuntuacion()) { // Llamada a alerta de modificacion

			comprobarOpinionBorrada(sentenciaSQL, ID); // Llamada a funcion que permite comprobar el cambio realizado en
			// el
			// comic
		}
	}

	/**
	 * Funcion que comprueba si la opinion se ha introducida correctamente
	 *
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	public void comprobarOpinionInsertada(String sentenciaSQL, String ID, String puntuacion) throws SQLException {

		conn = DBManager.conexion();
		listaComics.clear();
		PreparedStatement ps = null;
		try {

			ps = conn.prepareStatement(sentenciaSQL);
			if (comprobarID(ID)) // Comprueba si la ID introducida existe en la base de datos
			{
				Comic comic = comicDatos(ID);
				ps.setString(1, puntuacion);
				ps.setString(2, ID);

				if (ps.executeUpdate() == 1) { // Si se ha modificado correctamente, saltara el siguiente mensaje
					listaComics.add(comic);
				}
			}
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		} finally {
			ps.close();
		}
	}

	/**
	 * Funcion que comprueba si la opinion se ha introducida correctamente
	 *
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	public void comprobarOpinionBorrada(String sentenciaSQL, String ID) throws SQLException {

		conn = DBManager.conexion();
		listaComics.clear();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sentenciaSQL);
			if (comprobarID(ID)) // Comprueba si la ID introducida existe en la base de datos
			{
				Comic comic = comicDatos(ID);
				ps.setString(1, ID);

				if (ps.executeUpdate() == 1) { // Si se ha modificado correctamente, saltara el siguiente mensaje
					listaComics.add(comic);
				}
			}
		} catch (SQLException ex) {
			nav.alertaException(ex.toString());
		} finally {
			ps.close();
		}
	}

	/**
	 * Función que permite mostrar la imagen de una portada cuando se hace clic con
	 * el ratón encima del cómic seleccionado.
	 *
	 * @param iD el ID del cómic
	 * @return la imagen de la portada del cómic
	 */
	public Image selectorImage(String iD) {
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {
			conn = DBManager.conexion(); // Se establece la conexión si no está conectado

			String sentenciaSQL = "SELECT portada FROM comicsbbdd WHERE ID = ?";
			statement = conn.prepareStatement(sentenciaSQL);
			statement.setString(1, iD);
			rs = statement.executeQuery();

			if (rs.next()) {
				String direccionImagen = rs.getString("portada");
				if (direccionImagen != null && !direccionImagen.isEmpty()) {
					try {
						Image imagen = new Image(new File(direccionImagen).toURI().toString());
						imagen = new Image(imagen.getUrl(), 250, 0, true, true);
						return imagen;
					} catch (Exception e) {
						nav.alertaException(e.toString());
					}
				}
			}
		} catch (SQLException e) {
			nav.alertaException(e.toString());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				nav.alertaException(e.toString());
			}
		}

		return null;
	}

	/**
	 * Función que muestra todos los cómics de la base de datos.
	 *
	 * @param sentenciaSQL la sentencia SQL para obtener los cómics
	 * @param excepcion    el mensaje de excepción a mostrar si no se encuentran
	 *                     cómics
	 * @return una lista de cómics obtenidos de la base de datos
	 * @throws SQLException
	 */
	public List<Comic> comprobarLibreria(String sentenciaSQL, String excepcion) {

		List<Comic> listComic = FXCollections.observableArrayList(verLibreria(sentenciaSQL));

		if (listComic.isEmpty()) {

			nav.alertaException(excepcion);
		}

		return listComic;
	}

	/**
	 * Función que busca en el ArrayList el o los cómics que tengan coincidencia con
	 * los datos introducidos en el TextField.
	 *
	 * @param comic           el cómic con los parámetros de búsqueda
	 * @param busquedaGeneral el texto de búsqueda general
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Comic> busquedaParametro(Comic comic, String busquedaGeneral) throws SQLException {
		String sentenciaSQL = "SELECT * from comicsbbdd";
		String excepcion = "No hay ningun comic guardado en la base de datos";
		List<Comic> listComic = comprobarLibreria(sentenciaSQL, excepcion);

		if (listComic.size() != 0) {
			if (busquedaGeneral.length() != 0) {
				listComic = FXCollections.observableArrayList(verBusquedaGeneral(busquedaGeneral));
			} else {
				try {
					listComic = FXCollections.observableArrayList(filtadroBBDD(comic));
				} catch (NullPointerException ex) {
					ex.getStackTrace();
				}
			}
		}
		return listComic;
	}

	/**
	 * Funcion que muestra todos los comics de la base de datos
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaPosesion() throws SQLException {

		String sentenciaSQL = "SELECT * from comicsbbdd where estado = 'En posesion' ORDER BY nomComic,fecha_publicacion,numComic";
		String excepcion = "No hay ningun comic guardado en la base de datos en posesion";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Funcion que muestra todos los comics de la base de datos
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaKeyIssue() throws SQLException {

		String sentenciaSQL = "SELECT * FROM comicsbbdd WHERE key_issue <> 'Vacio' ORDER BY nomComic, fecha_publicacion, numComic";
		String excepcion = "No hay ningun comic guardado en la base de datos con key issue";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Funcion que muestra todos los comics de la base de datos.
	 *
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public List<Comic> libreriaCompleta() throws IOException {
		String query = "SELECT * FROM comicsbbdd ORDER BY nomComic,fecha_publicacion,numComic";

		String excepcion = "No hay ningun comic guardado en la base de datos";

		return comprobarLibreria(query, excepcion);
	}

	/**
	 * Devuelve una lista con todos los comics de la base de datos que se encuentran
	 * "En posesion"
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaVendidos() throws SQLException {
		String sentenciaSQL = "SELECT * from comicsbbdd where estado = 'Vendido' ORDER BY nomComic,fecha_publicacion,numComic";
		String excepcion = "No hay comics en vendidos";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Devuelve una lista con todos los comics de la base de datos que se encuentran
	 * "En posesion"
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaComprados() throws SQLException {
		String sentenciaSQL = "SELECT * from comicsbbdd where estado = 'Comprado' ORDER BY nomComic,fecha_publicacion,numComic";
		String excepcion = "No hay comics comprados";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Devuelve una lista con todos los comics de la base de datos que se encuentran
	 * "En posesion"
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaPuntuacion() throws SQLException {
		String sentenciaSQL = "SELECT * from comicsbbdd where puntuacion <> 'Sin puntuar' ORDER BY nomComic,fecha_publicacion,numComic";
		String excepcion = "No hay comics puntuados";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Devuelve una lista con todos los comics de la base de datos que se encuentran
	 * "En posesion"
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaSeleccionado(String datoSeleccionado) throws SQLException {
		String sentenciaSQL = "SELECT * FROM comicsbbdd " + "WHERE nomVariante LIKE '%" + datoSeleccionado
				+ "%' OR nomComic LIKE '%" + datoSeleccionado + "%' OR nomGuionista LIKE '%" + datoSeleccionado
				+ "%' OR firma LIKE '%" + datoSeleccionado + "%' OR nomDibujante LIKE '%" + datoSeleccionado
				+ "%' OR nomComic LIKE '%" + datoSeleccionado + "%' OR nomEditorial LIKE '%" + datoSeleccionado
				+ "%' OR caja_deposito LIKE '%" + datoSeleccionado + "%' OR formato LIKE '%" + datoSeleccionado
				+ "%' OR fecha_publicacion LIKE '%" + datoSeleccionado + "%' OR procedencia LIKE '%" + datoSeleccionado
				+ "%' ORDER BY nomComic, fecha_publicacion, numComic ASC";

		String excepcion = "No hay comics relacionados con esa seleccion";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Devuelve el numero total del resultado de la busqueda de un campo individual
	 * 
	 * @param datoSeleccionado
	 * @return
	 * @throws SQLException
	 */
	public int numeroTotalSelecionado(Comic comic) throws SQLException {
		String sentenciaSQL = "SELECT COUNT(*) FROM comicsbbdd WHERE 1=1"; // Initialize with a true condition

		if (!comic.getVariante().isEmpty()) {
			sentenciaSQL += " AND nomVariante LIKE ?";
		}
		if (!comic.getNombre().isEmpty()) {
			sentenciaSQL += " AND nomComic LIKE ?";
		}
		if (!comic.getNumero().isEmpty()) {
			sentenciaSQL += " AND numComic = ?";
		}
		if (!comic.getGuionista().isEmpty()) {
			sentenciaSQL += " AND nomGuionista LIKE ?";
		}
		if (!comic.getDibujante().isEmpty()) {
			sentenciaSQL += " AND nomDibujante LIKE ?";
		}
		if (!comic.getFirma().isEmpty()) {
			sentenciaSQL += " AND firma LIKE ?";
		}
		if (!comic.getEditorial().isEmpty()) {
			sentenciaSQL += " AND nomEditorial LIKE ?";
		}
		if (!comic.getNumCaja().isEmpty()) {
			sentenciaSQL += " AND caja_deposito = ?";
		}
		if (!comic.getFormato().isEmpty()) {
			sentenciaSQL += " AND formato LIKE ?";
		}
		if (!comic.getFecha().isEmpty()) {
			sentenciaSQL += " AND fecha_publicacion LIKE ?";
		}
		if (!comic.getProcedencia().isEmpty()) {
			sentenciaSQL += " AND procedencia LIKE ?";
		}

		int count = 0;
		try (PreparedStatement ps = conn.prepareStatement(sentenciaSQL)) {
			int paramIndex = 1;

			if (!comic.getVariante().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getVariante() + "%");
			}
			if (!comic.getNombre().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getNombre() + "%");
			}
			if (!comic.getNumero().isEmpty()) {
				ps.setString(paramIndex++, comic.getNumero());
			}
			if (!comic.getGuionista().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getGuionista() + "%");
			}
			if (!comic.getDibujante().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getDibujante() + "%");
			}
			if (!comic.getFirma().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getFirma() + "%");
			}
			if (!comic.getEditorial().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getEditorial() + "%");
			}
			if (!comic.getNumCaja().isEmpty()) {
				ps.setString(paramIndex++, comic.getNumCaja());
			}
			if (!comic.getFormato().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getFormato() + "%");
			}
			if (!comic.getFecha().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getFecha() + "%");
			}
			if (!comic.getProcedencia().isEmpty()) {
				ps.setString(paramIndex++, "%" + comic.getProcedencia() + "%");
			}

			try (ResultSet resultado = ps.executeQuery()) {
				if (resultado.next()) {
					count = resultado.getInt(1);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return count;
	}

	public int numeroTotalSelecionado(String datoSeleccionado) throws SQLException {
		String sentenciaSQL = "SELECT COUNT(*) FROM comicsbbdd " + "WHERE nomVariante LIKE ? " + "OR nomComic LIKE ? "
				+ "OR nomGuionista LIKE ? " + "OR nomDibujante LIKE ? " + "OR firma LIKE ? " + "OR nomComic LIKE ? "
				+ "OR nomEditorial LIKE ? " + "OR caja_deposito LIKE ? " + "OR formato LIKE ? "
				+ "OR fecha_publicacion LIKE ?" + "OR procedencia LIKE ?";
		int count = 0;

		try (PreparedStatement ps = conn.prepareStatement(sentenciaSQL)) {
			ps.setString(1, "%" + datoSeleccionado + "%");
			ps.setString(2, "%" + datoSeleccionado + "%");
			ps.setString(3, "%" + datoSeleccionado + "%");
			ps.setString(4, "%" + datoSeleccionado + "%");
			ps.setString(5, "%" + datoSeleccionado + "%");
			ps.setString(6, "%" + datoSeleccionado + "%");
			ps.setString(7, "%" + datoSeleccionado + "%");
			ps.setString(8, "%" + datoSeleccionado + "%");
			ps.setString(9, "%" + datoSeleccionado + "%");
			ps.setString(10, "%" + datoSeleccionado + "%");
			ps.setString(11, "%" + datoSeleccionado + "%");
			try (ResultSet resultado = ps.executeQuery()) {
				if (resultado.next()) {
					count = resultado.getInt(1);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return count;
	}

	/**
	 * Devuelve una lista con todos los comics de la base de datos que se encuentran
	 * firmados
	 *
	 * @return una lista de cómics que coinciden con los criterios de búsqueda
	 * @throws SQLException
	 */
	public List<Comic> libreriaFirmados() throws SQLException {
		String sentenciaSQL = "SELECT * from comicsbbdd where Firma <> '' ORDER BY nomComic,fecha_publicacion,numComic";
		String excepcion = "No hay comics firmados";

		return comprobarLibreria(sentenciaSQL, excepcion);
	}

	/**
	 * Obtiene la dirección de la portada de un cómic.
	 *
	 * @param idComic ID del cómic
	 * @return Dirección de la portada del cómic
	 * @throws SQLException Si ocurre algún error de SQL
	 */
	public String obtenerDireccionPortada(String idComic) throws SQLException {
		String query = "SELECT portada FROM comicsbbdd WHERE ID = ?";
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, idComic);
			ResultSet resultado = ps.executeQuery();
			if (resultado.next()) {
				String portada = resultado.getString("portada");
				if (portada != null && !portada.isEmpty()) {
					return portada;
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			ps.close();
		}

		return null;
	}

	// Métodos para ordenar los HashMaps en orden ascendente (ASC) por valor
	public static List<Map.Entry<String, Integer>> sortByValue(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
		list.sort(Map.Entry.comparingByKey());
		return list;
	}

	public static List<Map.Entry<Integer, Integer>> sortByValueInt(Map<Integer, Integer> map) {
		List<Map.Entry<Integer, Integer>> list = new LinkedList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());
		return list;
	}

	public void generar_fichero_estadisticas() {
		// Crear HashMaps para almacenar los datos de cada campo sin repetición y sus
		// conteos
		Map<String, Integer> nomComicEstadistica = new HashMap<>();
		Map<Integer, Integer> cajaDepositoEstadistica = new HashMap<>();
		Map<String, Integer> nomVarianteEstadistica = new HashMap<>();
		Map<String, Integer> firmaEstadistica = new HashMap<>();
		Map<String, Integer> nomEditorialEstadistica = new HashMap<>();
		Map<String, Integer> formatoEstadistica = new HashMap<>();
		Map<String, Integer> procedenciaEstadistica = new HashMap<>();
		Map<String, Integer> fechaPublicacionEstadistica = new HashMap<>();
		Map<String, Integer> nomGuionistaEstadistica = new HashMap<>();
		Map<String, Integer> nomDibujanteEstadistica = new HashMap<>();
		Map<String, Integer> puntuacionEstadistica = new HashMap<>();
		Map<String, Integer> estadoEstadistica = new HashMap<>();
		List<String> keyIssueDataList = new ArrayList<>();

		String consultaSql = "SELECT * FROM comicsbbdd";
		int totalComics = 0;
		try {
			Connection conn = DBManager.conexion();
			// Realizar la consulta a la base de datos
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(consultaSql);

			// Procesar los datos y generar la estadística
			while (rs.next()) {
				// Obtener los datos de cada campo
				String nomComic = rs.getString("nomComic");
				int numComic = rs.getInt("numComic");
				int cajaDeposito = rs.getInt("caja_deposito");
				String nomVariante = rs.getString("nomVariante");
				String firma = rs.getString("firma");
				String nomEditorial = rs.getString("nomEditorial");
				String formato = rs.getString("formato");
				String procedencia = rs.getString("procedencia");
				String fechaPublicacion = rs.getString("fecha_publicacion");
				String nomGuionista = rs.getString("nomGuionista");
				String nomDibujante = rs.getString("nomDibujante");
				String puntuacion = rs.getString("puntuacion");
				String estado = rs.getString("estado");
				String clave_comic = rs.getString("key_issue");

				// Actualizar los HashMaps para cada campo
				nomComicEstadistica.put(nomComic, nomComicEstadistica.getOrDefault(nomComic, 0) + 1);
				cajaDepositoEstadistica.put(cajaDeposito, cajaDepositoEstadistica.getOrDefault(cajaDeposito, 0) + 1);

				firmaEstadistica.put(firma, firmaEstadistica.getOrDefault(firma, 0) + 1);
				nomEditorialEstadistica.put(nomEditorial, nomEditorialEstadistica.getOrDefault(nomEditorial, 0) + 1);
				formatoEstadistica.put(formato, formatoEstadistica.getOrDefault(formato, 0) + 1);
				procedenciaEstadistica.put(procedencia, procedenciaEstadistica.getOrDefault(procedencia, 0) + 1);
				fechaPublicacionEstadistica.put(fechaPublicacion,
						fechaPublicacionEstadistica.getOrDefault(fechaPublicacion, 0) + 1);

				// Dividir los valores separados por guiones ("-") en cada campo y contarlos
				// como entradas independientes en las estadísticas
				String[] claveList = clave_comic.split("-");
				for (String clave : claveList) {
					clave = clave.trim(); // Remove leading and trailing spaces

					// Aquí verificamos si clave_comic no es "Vacio" ni está vacío antes de agregar
					// a keyIssueDataList
					if (!clave.equalsIgnoreCase("Vacio") && !clave.isEmpty()) {
						String keyIssueData = "Nombre del comic: " + nomComic + " - " + "Numero: " + numComic
								+ " - Key issue:  " + clave;
						keyIssueDataList.add(keyIssueData);
					}
				}

				// Dividir los valores separados por guiones ("-") en cada campo y contarlos
				// como entradas independientes en las estadísticas
				String[] varianteList = nomVariante.split("-");
				for (String variante : varianteList) {
					variante = variante.trim(); // Remove leading and trailing spaces
					nomVarianteEstadistica.put(variante, nomVarianteEstadistica.getOrDefault(variante, 0) + 1);
				}

				// Dividir los valores separados por guiones ("-") en cada campo y contarlos
				// como entradas independientes en las estadísticas
				String[] guionistaList = nomGuionista.split("-");
				for (String guionista : guionistaList) {
					guionista = guionista.trim(); // Remove leading and trailing spaces
					nomGuionistaEstadistica.put(guionista, nomGuionistaEstadistica.getOrDefault(guionista, 0) + 1);
				}
				// Dividir los valores separados por guiones ("-") en cada campo y contarlos
				// como entradas independientes en las estadísticas

				String[] dibujanteList = nomDibujante.split("-");
				for (String dibujante : dibujanteList) {
					dibujante = dibujante.trim(); // Remove leading and trailing spaces
					nomDibujanteEstadistica.put(dibujante, nomDibujanteEstadistica.getOrDefault(dibujante, 0) + 1);
				}

				// Dividir los valores separados por guiones ("-") en cada campo y contarlos
				// como entradas independientes en las estadísticas
				String[] firmaList = firma.split("-");
				for (String firmaValor : firmaList) {
					firmaValor = firmaValor.trim(); // Remove leading and trailing spaces
					firmaEstadistica.put(firmaValor, firmaEstadistica.getOrDefault(firmaValor, 0) + 1);
				}

				// Dividir los valores separados por guiones ("-") en cada campo y contarlos
				// como entradas independientes en las estadísticas
				String[] procedenciaList = procedencia.split("-");
				for (String procedenciaValor : procedenciaList) {
					procedenciaValor = procedenciaValor.trim(); // Remove leading and trailing spaces
					procedenciaEstadistica.put(procedenciaValor,
							procedenciaEstadistica.getOrDefault(procedenciaValor, 0) + 1);
				}

				puntuacionEstadistica.put(puntuacion, puntuacionEstadistica.getOrDefault(puntuacion, 0) + 1);
				estadoEstadistica.put(estado, estadoEstadistica.getOrDefault(estado, 0) + 1);

				totalComics++; // Incrementar el contador de cómics

			}

			// Cerrar la conexión
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			System.out.println("Error al conectar a la base de datos: " + e.getMessage());
		}

		// Generar la cadena de estadística
		StringBuilder estadisticaStr = new StringBuilder();
		String lineaDecorativa1 = "\n--------------------------------------------------------";
		String lineaDecorativa2 = "--------------------------------------------------------\n";

		estadisticaStr.append("Estadisticas de comics de la base de datos: " + DBManager.DB_NAME + ", a fecha de: "
				+ obtenerFechaActual() + "\n");

		// Agregar los valores de nomComic a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de los nombres de comics:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> nomComicList = sortByValue(nomComicEstadistica);
		for (Map.Entry<String, Integer> entry : nomComicList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de cajaDeposito a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de las cajas:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<Integer, Integer>> cajaDepositoList = sortByValueInt(cajaDepositoEstadistica);
		for (Map.Entry<Integer, Integer> entry : cajaDepositoList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de nomVariante a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de los nombres de variantes:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> nomVarianteList = sortByValue(nomVarianteEstadistica);
		for (Map.Entry<String, Integer> entry : nomVarianteList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de firma a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de autores firma:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> firmaList = sortByValue(firmaEstadistica);
		for (Map.Entry<String, Integer> entry : firmaList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de nomGuionista a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de guionistas:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> nomGuionistaList = sortByValue(nomGuionistaEstadistica);
		for (Map.Entry<String, Integer> entry : nomGuionistaList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de nomDibujante a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de dibujantes:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> nomDibujantesList = sortByValue(nomDibujanteEstadistica);
		for (Map.Entry<String, Integer> entry : nomDibujantesList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de nomEditorial a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de Editoriales:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> nomEditorialList = sortByValue(nomEditorialEstadistica);
		for (Map.Entry<String, Integer> entry : nomEditorialList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de procedencia a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de procedencia:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> procedenciaList = sortByValue(procedenciaEstadistica);
		for (Map.Entry<String, Integer> entry : procedenciaList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de fechaPublicacion a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de fecha publicacion:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> fechaPublicacionList = sortByValue(fechaPublicacionEstadistica);
		for (Map.Entry<String, Integer> entry : fechaPublicacionList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de puntuacion a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de puntuacion:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> puntuacionList = sortByValue(puntuacionEstadistica);
		for (Map.Entry<String, Integer> entry : puntuacionList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de estado a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de estado:\n");
		estadisticaStr.append(lineaDecorativa2);
		List<Map.Entry<String, Integer>> estadoList = sortByValue(estadoEstadistica);
		for (Map.Entry<String, Integer> entry : estadoList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		// Agregar los valores de formato a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de key issue:\n");
		estadisticaStr.append(lineaDecorativa2);
		for (String keyIssueData : keyIssueDataList) {
			estadisticaStr.append(keyIssueData).append("\n");
		}

		// Agregar los valores de formato a la estadística
		estadisticaStr.append(lineaDecorativa1);
		estadisticaStr.append("\nEstadística de formato:\n");
		estadisticaStr.append(lineaDecorativa2);
		estadisticaStr.append("Comics en total: " + totalComics).append("\n");
		List<Map.Entry<String, Integer>> formatoList = sortByValue(formatoEstadistica);
		for (Map.Entry<String, Integer> entry : formatoList) {
			estadisticaStr.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}

		estadisticaStr.append(lineaDecorativa1);

		// Crear el archivo de estadística y escribir los datos en él
		String nombreArchivo = "estadistica_" + obtenerFechaActual() + ".txt";
		String userHome = System.getProperty("user.home");
		String ubicacion = userHome + File.separator + "AppData" + File.separator + "Roaming";
		String carpetaLibreria = ubicacion + File.separator + "libreria";
		String rutaCompleta = carpetaLibreria + File.separator + nombreArchivo;

		try (PrintWriter writer = new PrintWriter(new FileWriter(rutaCompleta))) {
			writer.print(estadisticaStr);

			// Abrir el archivo con el programa asociado en el sistema
			abrirArchivoConProgramaAsociado(rutaCompleta);

		} catch (IOException e) {

			ventanas.alertaException("Error al guardar la estadística en el archivo: " + e.getMessage());
		}
	}

	// Función para obtener la fecha y hora actual en el formato deseado
	private String obtenerFechaActual() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		Date date = new Date();
		return formatter.format(date);
	}

	// Función para abrir un archivo con el programa asociado en el sistema
	private void abrirArchivoConProgramaAsociado(String rutaArchivo) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(new File(rutaArchivo));
			} else {
				System.out.println("La apertura del archivo no es compatible con este sistema operativo.");
			}
		} catch (IOException e) {
			System.out.println("Error al abrir el archivo: " + e.getMessage());
		}
	}
}