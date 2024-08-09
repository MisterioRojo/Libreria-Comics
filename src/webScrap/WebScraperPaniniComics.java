package webScrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import alarmas.AlarmaList;
import comicManagement.Comic;
import javafx.scene.control.TextArea;

/**
 * Clase principal para realizar el web scraping de información específica de la
 * página web "https://www.previewsworld.com". Extrae detalles de un cómic, como
 * el título, número, creadores, imagen principal, fecha de salida, valor (SRP),
 * editorial y URL de referencia.
 */
public class WebScraperPaniniComics {

	public static int verificarCodigoRespuesta(String urlString) throws IOException, URISyntaxException {
		try {
			// Desactivar la validación del certificado SSL/TLS
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			URI uri = new URI(urlString);
			HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
			connection.setRequestMethod("GET");

			// Obtener el código de respuesta HTTP
			int codigoRespuesta = connection.getResponseCode();

			// Cerrar la conexión
			connection.disconnect();

			return codigoRespuesta;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static String agregarMasAMayusculas(String cadena) {
		return cadena.toUpperCase();
	}

	public static String buscarEnGoogle(String searchTerm) {
		searchTerm = agregarMasAMayusculas(searchTerm);
		searchTerm = searchTerm.replace("(", "%28").replace(")", "%29").replace("#", "%23");

		try {
			String encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8");
			String urlString = "https://www.google.com/search?q=" + encodedSearchTerm + "+panini+comics";
			URI uri = new URI(urlString);
			URL url = uri.toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36");

			int responseCode = con.getResponseCode();
			if (responseCode == 429) {
				// Manejar el error de demasiadas solicitudes
				System.out.println("Demasiadas solicitudes. Esperando y reintentando...");
				Thread.sleep(5000); // Esperar 5 segundos antes de reintentar
				return buscarEnGoogle(searchTerm); // Reintentar la búsqueda
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder content = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();

			String html = content.toString();
			int startIndex = html.indexOf("https://www.panini.es/shp_esp_es/");
			if (startIndex != -1) {
				int endIndex = html.indexOf("\"", startIndex);
				String[] urls = html.substring(startIndex, endIndex).split("\"");
				String googleUrl = "https://www.panini.es/shp_esp_es/";
				return encontrarURLRelevante(urls, googleUrl);
			} else {
				return null;
			}
		} catch (IOException | InterruptedException | URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String encontrarURLRelevante(String[] urls, String googleUrl) {
		String urlElegida = null;
		int minDifference = Integer.MAX_VALUE;
		for (String url : urls) {
			int difference = Math.abs(url.indexOf(googleUrl) - url.indexOf(url));
			if (difference < minDifference) {
				minDifference = difference;
				urlElegida = url;
			}
		}
		return urlElegida;
	}

	public static String referenciaUrl(String codigoBusqueda) {
		String url = "";
		if (codigoBusqueda.contains("panini.es")) {
			url = codigoBusqueda;
		} else {
			url = buscarEnGoogle(codigoBusqueda);
		}
		return url;
	}

	/**
	 * Realiza scraping en una URL dada para extraer información sobre un cómic.
	 * 
	 * @param url La URL de la página web del cómic.
	 * @return Un array de cadenas que contiene la información del cómic, o un array
	 *         vacío si ocurre un error.
	 * @throws URISyntaxException
	 */
	public static Comic displayComicInfo(String diamondCode, TextArea prontInfo) {

		try {
			String urlReferencia = referenciaUrl(diamondCode);
			System.out.println(urlReferencia);
			int codigoRespuesta = verificarCodigoRespuesta(urlReferencia);

			if (codigoRespuesta == 404) {
				String mensaje = "No se encontró el cómic con Diamond Code: " + diamondCode;
				AlarmaList.mostrarMensajePront(mensaje, false, prontInfo);

				return null;
			}
			Document document = Jsoup.connect(urlReferencia).get();

			String titulo = scrapeTitle(document);
			String issueKey = scrapeDescription(document);
			String numero = extractNumeroFromTitle(document);
			// Scraping de la etiqueta div con class "SRP"
			String precio = scrapePrice(document).replace(",", ".");
			String artistasComic = scrapeArtist(document);
			String[] artistasFiltrados = ScrapeoTipoArtistaComicVine.displayArtistasInfo(artistasComic);
			// Variables para almacenar los creadores
			String artist = artistasFiltrados[0];
			String writer = artistasFiltrados[1];

			String codigoComic = scrapeUPC(document);

			// Scraping de la etiqueta div con class "ReleaseDate"
			String fecha = scrapeFechaPublicacion(document);

			// Scraping de la etiqueta img con id "MainContentImage"
			String portadaImagen = scrapeAndPrintMainImage(document);

			// Scraping de la etiqueta div con class "Publisher"
			String editorial = scrapeAndPrintPublisher();

			titulo = Comic.limpiarCampo(titulo);
			editorial = Comic.limpiarCampo(editorial);
			writer = Comic.limpiarCampo(writer);
			artist = Comic.limpiarCampo(artist);
			issueKey = Comic.limpiarCampo(issueKey);

			return new Comic.ComicGradeoBuilder("", titulo).codigoComic(codigoComic).precioComic(precio)
					.numeroComic(numero).fechaGradeo(fecha).editorComic(editorial).keyComentarios(issueKey)
					.firmaComic("").artistaComic(artist).guionistaComic(writer).varianteComic("")
					.direccionImagenComic(portadaImagen).urlReferenciaComic(urlReferencia).build();

		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Extrae el título del documento HTML proporcionado. Elimina el símbolo "#" y
	 * todo lo que le sigue, luego capitaliza la primera letra de cada palabra en el
	 * título.
	 *
	 * @param document El documento HTML a analizar.
	 * @return El título extraído y formateado, o null si no se encuentra.
	 */
	private static String scrapeTitle(Document document) {
		Element titleElement = document.selectFirst("span.base");
		if (titleElement != null) {
			String titleContent = titleElement.text().trim();
			titleContent = capitalizeTitle(titleContent);
			titleContent = removeHashtagAndFollowing(titleContent);

			return titleContent;
		}
		return null;
	}

	/**
	 * Extrae y devuelve el contenido del elemento dentro del div con clase "product
	 * attribute description".
	 *
	 * @param document El documento HTML a procesar.
	 * @return El contenido del elemento
	 */
	private static String scrapeDescription(Document document) {
		Element descriptionElement = document.selectFirst("div.product.attribute.description p");
		if (descriptionElement != null) {
			return descriptionElement.text().trim();
		}
		return null;
	}

	private static String scrapeUPC(Document document) {
		Element metaItem = document.selectFirst("li.item.pnn_issn span.data");
		if (metaItem != null) {
			return metaItem.text().trim();
		}
		return "0";
	}

	/**
	 * Extrae y devuelve el contenido del <span class="data"> dentro del
	 * <li class="item pnn_authors_display">.
	 *
	 * @param document El documento HTML a procesar.
	 * @return El contenido del <span class="data">, o una cadena vacía si no se
	 *         encuentra.
	 */
	private static String scrapeArtist(Document document) {
		Element metaItem = document.selectFirst("li.item.pnn_authors_display span.data");
		if (metaItem != null) {
			return metaItem.text().trim();
		}
		return "";
	}

	private static String scrapePrice(Document document) {
		Element metaItem = document.selectFirst("span.price");

		if (metaItem != null) {
			String titleContent = metaItem.text().trim();
			return removeCurrencySymbols(titleContent);
		}

		return "NA";
	}

	/**
	 * Elimina cualquier símbolo de moneda del contenido del título.
	 *
	 * @param titleContent El contenido del título a procesar.
	 * @return El contenido del título sin los símbolos de moneda.
	 */
	private static String removeCurrencySymbols(String titleContent) {
		// Elimina cualquier símbolo de moneda (€, $, ¥, £, etc.)
		return titleContent.replaceAll("[€$¥£]", "").trim();
	}

	/**
	 * Extrae y convierte la fecha de publicación de un documento HTML al formato
	 * MySQL.
	 *
	 * @param document El documento HTML a procesar.
	 * @return La fecha de publicación en formato MySQL, o una fecha predeterminada
	 *         si no se encuentra.
	 */
	private static String scrapeFechaPublicacion(Document document) {
		Element metaItem = document.selectFirst("li.item.pnn_release_date span.data");
		if (metaItem != null) {
			return convertirFechaMySQL(metaItem.text().trim());
		}
		return "2000-01-01";
	}

	/**
	 * Convierte una fecha en formato "1 ago. 2024" al formato MySQL "2024-08-01".
	 *
	 * @param fecha La fecha en formato "1 ago. 2024".
	 * @return La fecha en formato MySQL "2024-08-01".
	 */
	private static String convertirFechaMySQL(String fecha) {
		SimpleDateFormat formatoEntrada = new SimpleDateFormat("d MMM. yyyy", new java.util.Locale("es", "ES"));
		SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd");

		try {
			Date fechaDate = formatoEntrada.parse(fecha);
			return formatoSalida.format(fechaDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return "2000-01-01";
		}
	}

	/**
	 * Extrae y devuelve todos los números presentes en el contenido del título como
	 * una cadena.
	 *
	 * @param titleContent El contenido del título a procesar.
	 * @return Una cadena con los números encontrados en el contenido del título,
	 *         separados por comas.
	 */
	private static String extractNumbersAsString(String titleContent) {
		List<String> numbers = new ArrayList<>();
		Matcher matcher = Pattern.compile("\\d+").matcher(titleContent);

		while (matcher.find()) {
			numbers.add(matcher.group());
		}

		return String.join(", ", numbers);
	}

	private static String removeHashtagAndFollowing(String titleContent) {
		if (titleContent.contains("#")) {
			int hashtagIndex = titleContent.indexOf('#');
			titleContent = titleContent.substring(0, hashtagIndex);
		}

		titleContent = titleContent.replaceAll("\\(.*?\\)", "");

		// Elimina cualquier número
		titleContent = titleContent.replaceAll("\\d", "");

		// Retorna el contenido procesado, sin espacios extra al inicio o al final
		return titleContent.trim();
	}

	/**
	 * Formatea el título capitalizando la primera letra de cada palabra.
	 *
	 * @param title El título a formatear.
	 * @return El título formateado con la primera letra de cada palabra en
	 *         mayúscula.
	 */
	private static String capitalizeTitle(String title) {
		// Divide el título en palabras
		title.toLowerCase();
		String[] words = title.split(" ");
		StringBuilder capitalizedTitle = new StringBuilder();

		for (String word : words) {
			if (word.length() > 0) {
				// Capitaliza la primera letra de cada palabra y añade el resto en minúsculas
				capitalizedTitle.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase())
						.append(" ");
			}
		}

		// Elimina el espacio extra al final
		return capitalizedTitle.toString().trim();
	}

	/**
	 * Extrae el número que sigue al símbolo "#" en el título, o el primer número
	 * encontrado si no existe el símbolo "#". Excluye cualquier texto entre
	 * paréntesis si existe y maneja números que comienzan con '0'.
	 *
	 * @param document El documento HTML del cual extraer.
	 * @return El número extraído o null si no se encuentra.
	 */
	private static String extractNumeroFromTitle(Document document) {
		Element titleElement = document.selectFirst("span.base");
		if (titleElement != null) {
			String titleContent = titleElement.text().trim();
			return extractNumbersAsString(titleContent);

		}
		return "0";
	}

	/**
	 * Extrae y devuelve el valor del atributo href del div con clase
	 * "fotorama__stage__frame fotorama_vertical_ratio fotorama__loaded
	 * fotorama__loaded--img fotorama__active".
	 *
	 * @param document El documento HTML a procesar.
	 * @return El valor del atributo href, o una cadena vacía si no se encuentra.
	 */
	private static String scrapeAndPrintMainImage(Document document) {
		Elements imgElements = document.select("img[src$=.jpg]");

		for (Element imgElement : imgElements) {
			return imgElement.attr("src");
		}
		return "";
	}

	/**
	 * Extrae e imprime el nombre del editor del documento.
	 *
	 * @param document El documento HTML a analizar.
	 * @return El nombre del editor en mayúsculas, o null si no se encuentra.
	 */
	private static String scrapeAndPrintPublisher() {
		return "Panini Comics";
	}

//	public static String convertirFechaMySQL(String fechaSalida) {
//		try {
//			// Mapear nombres de los meses en inglés a números
//			String[] meses = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
//			// Parsear la fecha al formato adecuado
//			SimpleDateFormat formatoEntrada = new SimpleDateFormat("MM dd, yyyy");
//			// Remover el sufijo ordinal numérico antes de parsear
//			fechaSalida = fechaSalida.replaceAll("(?<=\\d)(st|nd|rd|th)", "");
//			// Dividir la fecha en partes: mes, día, año
//			String[] partesFecha = fechaSalida.split(" ");
//			// Convertir el nombre del mes a un número
//			int mesNum = -1;
//			for (int i = 0; i < meses.length; i++) {
//				if (meses[i].equals(partesFecha[0])) {
//					mesNum = i + 1; // Añadir 1 porque los meses en SimpleDateFormat comienzan en 0
//					break;
//				}
//			}
//			// Formatear la fecha al formato MySQL
//			SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd");
//			if (partesFecha.length >= 3) {
//				Date fecha = formatoEntrada
//						.parse(String.format("%02d", mesNum) + " " + partesFecha[1] + " " + partesFecha[2]);
//				return formatoSalida.format(fecha);
//			} else {
//				return "2000-01-01"; // Devuelve la fecha por defecto
//			}
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

}
