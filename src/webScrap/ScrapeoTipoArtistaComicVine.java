package webScrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ScrapeoTipoArtistaComicVine {

	public static String buscarEnGoogle(String searchTerm) {
		try {
			String encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8");
			String urlString = "https://www.google.com/search?q=comic+vine+" + encodedSearchTerm;
			URI uri = new URI(urlString);
			URL url = uri.toURL();

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.127 Safari/537.36");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder content = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();

			String html = content.toString();
			int startIndex = html.indexOf("https://comicvine.gamespot.com/");
			if (startIndex != -1) {
				int endIndex = html.indexOf("\"", startIndex);
				String[] urls = html.substring(startIndex, endIndex).split("\"");
				String googleUrl = "https://comicvine.gamespot.com/";
				return encontrarURLRelevante(urls, googleUrl);
			} else {
				return null;
			}
		} catch (IOException | URISyntaxException e) {
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

	public static String[] displayArtistasInfo(String nombres) {
		String[] nombresArray = nombres.split(",\\s*");
		List<String> artistas = new ArrayList<>();
		List<String> guionistas = new ArrayList<>();
		List<Future<String[]>> futures = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(nombresArray.length);

		for (String nombre : nombresArray) {
			Callable<String[]> task = () -> {
				String[] resultados = new String[2];
				try {
					String urlReferencia = buscarEnGoogle(nombre);
					if (urlReferencia != null) {
						Document document = Jsoup.connect(urlReferencia).get();
						String descripcion = scrapeTitle(document);
						
						
						resultados = tipoArtista(descripcion, nombre);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return resultados;
			};
			futures.add(executor.submit(task));
		}

		for (int i = 0; i < nombresArray.length; i++) {
			try {
				String[] resultados = futures.get(i).get();
				if (!resultados[0].isEmpty()) {
					guionistas.add(nombresArray[i]);
				}
				if (!resultados[1].isEmpty()) {
					artistas.add(nombresArray[i]);
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();

		String artistasStr = String.join(", ", artistas);
		String guionistasStr = String.join(", ", guionistas);

		return new String[] { artistasStr, guionistasStr };
	}

	private static String scrapeTitle(Document document) {
		Element titleElement = document.selectFirst("h3.display-view");
		if (titleElement != null) {
			String titleContent = titleElement.text().trim();
			return titleContent;
		}
		return "";
	}

	private static String[] tipoArtista(String descripcion, String nombrePersona) {
		String[] filtrosArtista = { "artist", "cartoonist", "art", "illustrator", "pencilled", "penciler" };
		String[] filtroGuionista = { "writer", "writers" };

		StringBuilder artista = new StringBuilder();
		StringBuilder guionista = new StringBuilder();

		for (String string : filtrosArtista) {
			if (descripcion.toLowerCase().contains(string)) {
				artista.append(nombrePersona).append(", ");
				break;
			}
		}

		for (String string : filtroGuionista) {
			if (descripcion.toLowerCase().contains(string)) {
				guionista.append(nombrePersona).append(", ");
				break;
			}
		}

		if (artista.length() > 0) {
			artista.setLength(artista.length() - 2);
		}
		if (guionista.length() > 0) {
			guionista.setLength(guionista.length() - 2);
		}

		return new String[] { guionista.toString(), artista.toString() };
	}
}
