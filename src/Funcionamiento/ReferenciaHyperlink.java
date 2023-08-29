package Funcionamiento;

/**
 * Clase que representa un hipervínculo con su texto de visualización y URL.
 */
public class ReferenciaHyperlink {
    private String displayText; // Texto de visualización del hipervínculo
    private String url; // URL del hipervínculo

    /**
     * Constructor de la clase.
     *
     * @param displayText El texto de visualización del hipervínculo.
     * @param url La URL del hipervínculo.
     */
    public ReferenciaHyperlink(String displayText, String url) {
        this.displayText = displayText;
        this.url = url;
    }

    /**
     * Obtiene el texto de visualización del hipervínculo.
     *
     * @return El texto de visualización del hipervínculo.
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * Obtiene la URL del hipervínculo.
     *
     * @return La URL del hipervínculo.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Establece el texto de visualización del hipervínculo.
     *
     * @param displayText El nuevo texto de visualización del hipervínculo.
     */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     * Establece la URL del hipervínculo.
     *
     * @param url La nueva URL del hipervínculo.
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
