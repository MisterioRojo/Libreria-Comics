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
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import alarmas.AlarmaList;
import comicManagement.Comic;
import funcionesAuxiliares.Utilidades;
import javafx.scene.control.TextArea;

/**
 * Clase principal para realizar el web scraping de información específica de la
 * página web "https://www.previewsworld.com". Extrae detalles de un cómic, como
 * el título, número, creadores, imagen principal, fecha de salida, valor (SRP),
 * editorial y URL de referencia.
 */
public class WebScraperPreviewsWorld {

	public static int verificarCodigoRespuesta(String urlString) throws IOException, URISyntaxException {
		try {
			
			if(urlString == null) {
				return 404;
			}
			
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
            String urlString = "https://www.google.com/search?q=" + encodedSearchTerm + "+previews+world";
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

            // Buscar la URL de Previews World
            int startIndex = html.indexOf("https://www.previewsworld.com/");
            if (startIndex != -1) {
                int endIndex = html.indexOf("\"", startIndex);
                String urlCandidate = html.substring(startIndex, endIndex);

                // Verificar si la URL contiene el patrón [A-Z]{3}\d{6}
                Pattern pattern = Pattern.compile("[A-Z]{3}\\d{6}");
                Matcher matcher = pattern.matcher(urlCandidate);

                if (matcher.find()) {
                    return urlCandidate; // Devolver la URL si contiene el patrón
                }
            }

            return null; // Devolver null si no se encontró una URL con el patrón
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

	public static String encontrarURLRelevante(String[] urls, String searchTerm, String googleUrl) {
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

		if (codigoBusqueda.contains("previewsworld.com/Catalog/")) {
			codigoBusqueda = Utilidades.extractCodeFromUrl(codigoBusqueda);
			if (codigoBusqueda.matches("[A-Z]{3}\\d{6}")) {
				url = "https://www.previewsworld.com/Catalog/" + codigoBusqueda;
			} else {
				url = buscarEnGoogle(codigoBusqueda);
			}
		} else if (codigoBusqueda.matches("[A-Z]{3}\\d{6}")) {
			codigoBusqueda = codigoBusqueda.trim();
			url = "https://www.previewsworld.com/Catalog/" + codigoBusqueda;
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
			
			int codigoRespuesta = verificarCodigoRespuesta(urlReferencia);

			if (codigoRespuesta == 404) {
				String mensaje = "No se encontró el cómic con Diamond Code: " + diamondCode;
				AlarmaList.mostrarMensajePront(mensaje, false, prontInfo);

				return null;
			}
			
			Document document = Jsoup.connect(urlReferencia).get();

			String titulo = scrapeTitle(document);

			String codigo = scrapeCode(document);

			String issueKey = scrapeDescription(document);

			String numero = extractNumeroFromTitle(document);
			// Scraping de la etiqueta div con class "SRP"
			String precio = scrapeAndPrintSRP(document);

			String[] artistas = scrapeCreators(document);
			
			// Variables para almacenar los creadores
			String writer = artistas[0];
			String artist = artistas[1];
			String variant = artistas[2];

			// Scraping de la etiqueta div con class "ReleaseDate"
			String fecha = scrapeAndPrintReleaseDate(document);

			// Scraping de la etiqueta img con id "MainContentImage"
			String portadaImagen = scrapeAndPrintMainImage(document);

			// Scraping de la etiqueta div con class "Publisher"
			String editorial = scrapeAndPrintPublisher(document);

			if (editorial.equalsIgnoreCase("Marvel Comics")) {
				editorial = "Marvel";
			}

			variant = Comic.limpiarCampo(variant);
			titulo = Comic.limpiarCampo(titulo);
			editorial = Comic.limpiarCampo(editorial);
			writer = Comic.limpiarCampo(writer);
			artist = Comic.limpiarCampo(artist);
			issueKey = Comic.limpiarCampo(issueKey);

			return new Comic.ComicGradeoBuilder("", titulo).codigoComic(codigo).precioComic(precio).numeroComic(numero)
					.fechaGradeo(fecha).editorComic(editorial).keyComentarios(issueKey).firmaComic("")
					.artistaComic(artist).guionistaComic(writer).varianteComic(variant)
					.direccionImagenComic(portadaImagen).urlReferenciaComic(urlReferencia).build();

		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
    public static String[] scrapeCreators(Document document) {
        try {

            // Variables para almacenar los creadores
            String writer = null;
            String artist = null;
            String variant = null;

            // Scraping del elemento <div> con la clase "Creators"
            Element creatorsDiv = document.selectFirst("div.Creators");
            String creatorsText = creatorsDiv != null ? creatorsDiv.text() : null;

            if (creatorsText != null && !creatorsText.isEmpty()) {
                // Procesar el texto del div "Creators"
                String[] parts = creatorsText.split("\\(|\\)");
                for (int i = 0; i < parts.length; i++) {
                    String type = parts[i].trim();
                    if (i + 1 < parts.length) {
                        String value = parts[i + 1].trim();

                        // Almacenar en la variable correspondiente según el tipo
                        switch (type) {
                            case "W":
                                writer = value.replace("-", "");
                                break;
                            case "A":
                                artist = value.replace("-", "");
                                break;
                            case "CA":
                                variant = value.replace("-", "");
                                break;
                            case "A/CA":
                                artist = value.replace("-", "");
                                variant = value.replace("-", "");
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else {
                // Si no existe el div "Creators" o está vacío, buscar en el div "Text"
                Element textDiv = document.selectFirst("div.Text");
                if (textDiv != null) {
                    StringBuilder contentAfterDiv = new StringBuilder();
                    Element nextElement = textDiv.nextElementSibling();
                    
                    while (nextElement != null && !nextElement.tagName().equals("br")) {
                        contentAfterDiv.append(nextElement.text()).append(" ");
                        nextElement = nextElement.nextElementSibling();
                    }

                    String[] parts = contentAfterDiv.toString().trim().split("\\(|\\)");
                    for (int i = 0; i < parts.length; i++) {
                        String type = parts[i].trim();
                        if (i + 1 < parts.length) {
                            String value = parts[i + 1].trim();

                            // Almacenar en la variable correspondiente según el tipo
                            switch (type) {
                                case "W":
                                    writer = value.replace("-", "");
                                    break;
                                case "A":
                                    artist = value.replace("-", "");
                                    break;
                                case "CA":
                                    variant = value.replace("-", "");
                                    break;
                                case "A/CA":
                                    artist = value.replace("-", "");
                                    variant = value.replace("-", "");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }

            // Crear y devolver el array con los resultados
            return new String[]{writer, artist, variant};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
		Element titleElement = document.selectFirst("h1.Title");
		if (titleElement != null) {
			String titleContent = titleElement.text().trim();
			titleContent = capitalizeTitle(titleContent);
			titleContent = removeHashtagAndFollowing(titleContent);
			return titleContent;
		}
		return null;
	}

	private static String scrapeCode(Document document) {
		Element codeElement = document.selectFirst("div.ItemCode");
		if (codeElement != null) {
			String titleContent = codeElement.text().trim();
			return titleContent;
		}
		return null;
	}

	private static String scrapeDescription(Document document) {
		// Selecciona el elemento con la clase "Creators"
		Element creatorsElement = document.selectFirst("div.Creators");
		// Selecciona el elemento con la clase "ReleaseDate"
		Element releaseDateElement = document.selectFirst("div.ReleaseDate");

		if (creatorsElement != null && releaseDateElement != null) {
			// Utiliza un StringBuilder para acumular el texto
			StringBuilder descriptionBuilder = new StringBuilder();

			// Encuentra el primer nodo siguiente al elemento creators
			Node currentNode = creatorsElement.nextSibling();

			// Recorre los nodos hasta encontrar el elemento ReleaseDate
			while (currentNode != null
					&& !(currentNode instanceof Element && ((Element) currentNode).hasClass("ReleaseDate"))) {
				// Si el nodo es un nodo de texto, añade el texto al StringBuilder
				if (currentNode.nodeName().equals("#text")) {
					descriptionBuilder.append(currentNode.toString().trim());
				} else if (currentNode.nodeName().equals("br")) {
					// Si el nodo es un <br>, añade un espacio para separar líneas
					descriptionBuilder.append(" ");
				}

				// Avanza al siguiente nodo
				currentNode = currentNode.nextSibling();
			}

			// Retorna el texto extraído
			return descriptionBuilder.toString().trim();
		}

		return "";
	}

	/**
	 * Elimina el símbolo "#" y todo lo que le sigue hasta un espacio, así como los
	 * paréntesis y todo lo que contienen, del contenido del título.
	 *
	 * @param titleContent El contenido del título a procesar.
	 * @return El contenido del título sin el "#" y el texto siguiente, ni los
	 *         paréntesis y su contenido.
	 */
	private static String removeHashtagAndFollowing(String titleContent) {
		// Elimina el símbolo "#" y todo lo que le sigue hasta un espacio
		if (titleContent.contains("#")) {
			int hashtagIndex = titleContent.indexOf('#');
			titleContent = titleContent.substring(0, hashtagIndex);
		}

		// Elimina los paréntesis y todo su contenido
		titleContent = titleContent.replaceAll("\\(.*?\\)", "");

		// Retorna el contenido procesado, en minúsculas y sin espacios extra al inicio
		// o al final
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
		Element titleElement = document.selectFirst("h1.Title");
		if (titleElement != null) {
			String titleContent = titleElement.text().trim();

			// Buscar el índice del símbolo "#"
			int hashtagIndex = titleContent.indexOf('#');
			if (hashtagIndex != -1 && hashtagIndex + 1 < titleContent.length()) {
				// Obtener la subcadena que sigue al símbolo "#"
				String subStringAfterHashtag = titleContent.substring(hashtagIndex + 1).trim();

				// Dividir el texto en partes usando espacio como delimitador
				String[] parts = subStringAfterHashtag.split("\\s+", 2); // divide en dos partes

				if (parts.length > 0) {
					String firstPart = parts[0].trim();

					// Limpiar cualquier carácter no numérico
					return firstPart.replaceAll("[^\\d]", "");
				}
			} else {
				// Si no hay símbolo "#", buscar el primer número en el texto
				java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(0*\\d+)\\b")
						.matcher(titleContent);
				if (matcher.find()) {
					String number = matcher.group(1).replaceFirst("^0+", ""); // Eliminar ceros a la izquierda
					return number.isEmpty() ? "0" : number; // Devolver "0" si el resultado está vacío
				}
			}
		}
		return "0";
	}

	/**
	 * Extrae e imprime la URL de la imagen principal del documento.
	 *
	 * @param document El documento HTML a analizar.
	 * @return La URL de la imagen principal, o null si no se encuentra.
	 * @throws IOException
	 */
	private static String scrapeAndPrintMainImage(Document document) throws IOException {
		Element mainImageElement = document.selectFirst("#MainContentImage");
		if (mainImageElement != null) {
			String mainImageUrl = "https://www.previewsworld.com" + mainImageElement.attr("src");

//			String imagen = Utilidades.descargarImagen(mainImageUrl, DOCUMENTS_PATH);

			return mainImageUrl;
		}
		return null;
	}

	/**
	 * Extrae e imprime la fecha de lanzamiento del documento.
	 *
	 * @param document El documento HTML a analizar.
	 * @return La fecha de lanzamiento formateada, o null si no se encuentra.
	 */
	private static String scrapeAndPrintReleaseDate(Document document) {
		Element releaseDateElement = document.selectFirst("div.ReleaseDate");
		if (releaseDateElement != null) {

			String releaseDateText = releaseDateElement.text().replace("In Shops: ", "").replace("N/A", "")
					.replace("TBD", "").trim();
			Date releaseDate = parseReleaseDate(releaseDateText);
			return formatDateAsString(releaseDate);
		}
		return null;
	}

	/**
	 * Extrae e imprime el precio sugerido de venta (SRP) del documento.
	 *
	 * @param document El documento HTML a analizar.
	 * @return El valor SRP, o null si no se encuentra.
	 */
	private static String scrapeAndPrintSRP(Document document) {
		Element srpElement = document.selectFirst("div.SRP");
		if (srpElement != null) {
			String srpValue = srpElement.text().replaceAll("[^0-9.]", "").trim();
			return srpValue;
		}
		return null;
	}

	/**
	 * Extrae e imprime el nombre del editor del documento.
	 *
	 * @param document El documento HTML a analizar.
	 * @return El nombre del editor en mayúsculas, o null si no se encuentra.
	 */
	private static String scrapeAndPrintPublisher(Document document) {
		Element publisherElement = document.selectFirst("div.Publisher");
		if (publisherElement != null) {
			String publisherName = capitalizeFirstLetter(publisherElement.text().trim().toLowerCase());

			if (publisherName.equalsIgnoreCase("Boom! Studios")) {
				publisherName = "Boom Studios";
			}

			if (publisherName.contains("comics")) {
				publisherName = publisherName.replace("comics", "").trim();
			} else if (publisherName.contains("comic")) {
				publisherName = publisherName.replace("comic", "").trim();
			}

			return publisherName;
		}
		return null;
	}

	/**
	 * Extrae el contenido de texto de un elemento HTML específico identificado por
	 * el selector.
	 *
	 * @param document El documento HTML a analizar.
	 * @param selector El selector CSS para el elemento objetivo.
	 * @return El contenido de texto del elemento seleccionado, o null si no se
	 *         encuentra.
	 */
	private static String scrapeElementText(Document document, String selector) {
		Elements elements = document.select(selector);
		if (!elements.isEmpty()) {
			Element element = elements.first();
			return element.text();
		}
		return null;
	}

	/**
	 * Analiza una cadena de texto de fecha en el formato "MMM d, yyyy" a un objeto
	 * Date.
	 *
	 * @param dateText El texto de la fecha a analizar.
	 * @return El objeto Date analizado o null si hay un error de análisis.
	 */
	private static Date parseReleaseDate(String dateText) {
		// Verificar si el texto de la fecha es "N/A" o está vacío
		if ("N/A".equals(dateText) || dateText.trim().isEmpty()) {
			// Si es "N/A" o está vacío, devolver la fecha predeterminada
			return getDefaultDate();
		}

		try {
			// Intentar parsear la fecha con el formato esperado
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
			Date parsedDate = dateFormat.parse(dateText);
			if (parsedDate != null) {
				return parsedDate;
			} else {
				// Si el parseo fue nulo, devolver la fecha predeterminada
				return getDefaultDate();
			}
		} catch (ParseException e) {
			// En caso de error de análisis, imprime el error y devuelve la fecha
			// predeterminada
			e.printStackTrace();
			return getDefaultDate();
		}
	}

	private static Date getDefaultDate() {
		try {
			SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd");
			return defaultFormat.parse("2000-01-01");
		} catch (ParseException ex) {
			// En caso de que ocurra un error al parsear la fecha predeterminada, devuelve
			// null
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Formatea un objeto Date como una cadena en el formato "yyyy-MM-dd".
	 *
	 * @param date El objeto Date a formatear.
	 * @return La cadena de fecha formateada, o null si la fecha de entrada es null.
	 */
	private static String formatDateAsString(Date date) {
		if (date != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return dateFormat.format(date);
		}
		return null;
	}

	/**
	 * Capitaliza la primera letra de cada palabra en la cadena de entrada.
	 *
	 * @param input La cadena de entrada a capitalizar.
	 * @return La cadena de entrada con la primera letra de cada palabra en
	 *         mayúscula.
	 */
	private static String capitalizeFirstLetter(String input) {
		StringBuilder result = new StringBuilder();

		input = input.toLowerCase();

		boolean capitalizeNext = true;

		for (char ch : input.toCharArray()) {
			if (Character.isWhitespace(ch)) {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				ch = Character.toTitleCase(ch);
				capitalizeNext = false;
			}

			result.append(ch);
		}

		return result.toString();
	}

}
