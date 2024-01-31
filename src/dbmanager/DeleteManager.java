package dbmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import Funcionamiento.Utilidades;
import comicManagement.Comic;

public class DeleteManager {
	
	private static final String DELETE_SENTENCIA = "DELETE FROM comicsbbdd WHERE ID = ?";

	/**
     * Borra y reinicia la base de datos de manera asíncrona.
     *
     * @return CompletableFuture que indica si la operación se realizó con éxito o no.
     */
    public static CompletableFuture<Boolean> deleteAndRestartDatabaseAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ListaComicsDAO.limpiarListasPrincipales();

                CompletableFuture<Boolean> result = CompletableFuture.supplyAsync(() -> {
                    try (Connection conn = ConectManager.conexion();
                         PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM comicsbbdd");
                         PreparedStatement resetAutoIncrementStatement = conn.prepareStatement("ALTER TABLE comicsbbdd AUTO_INCREMENT = 1")) {

                        Utilidades.copiaSeguridad();
                        Utilidades.eliminarArchivosEnCarpeta();
                    	
                        int deletedRows = deleteStatement.executeUpdate();
                        resetAutoIncrementStatement.executeUpdate();

                        System.out.println("Número de filas eliminadas: " + deletedRows);
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e); // Envuelve excepción en RuntimeException para ser manejada en el nivel superior
                    }
                });

                return result.join(); // Espera a que la operación asíncrona se complete y devuelve el resultado
            } catch (Exception e) {
                throw new RuntimeException(e); // Envuelve excepción en RuntimeException para ser manejada en el nivel superior
            }
        });
    }
    
	/**
	 * Función que comprueba si la opinión ha sido introducida correctamente.
	 *
	 * @param sentenciaSQL La sentencia SQL a ejecutar
	 * @param ID           La ID del cómic
	 * @param puntuacion   La puntuación a insertar
	 * @throws SQLException Si ocurre un error en la base de datos
	 */
	public static void borrarComic(String idComic) throws SQLException {
		ListaComicsDAO.listaComics.clear();

		try (Connection conn = ConectManager.conexion();
				PreparedStatement ps = conn.prepareStatement(DELETE_SENTENCIA);) {
			if (SelectManager.comprobarIdentificadorComic(idComic)) { // Comprueba si la ID introducida existe en la
																		// base de datos
				Comic comic = SelectManager.comicDatos(idComic);
				ps.setString(1, idComic);
				if (ps.executeUpdate() == 1) { // Si se ha modificado correctamente, se añade el cómic a la lista
					ListaComicsDAO.listaComics.add(comic);
				}
			}
		} catch (SQLException ex) {
			Utilidades.manejarExcepcion(ex);
		}
	}
}
