package ai.foresignal.source.formats.news;

/**
 * How a news adapter resolves an article's outlet (prompt 133) without reading the store itself (Architecture §3):
 * the media outlet registry implements it, and adapters receive it through an {@code ObjectProvider}. A domain the
 * registry does not know is added as an {@code unrated} outlet with the country and language the article gives.
 */
public interface NewsOutletLookup {

    /**
     * The outlet id of {@code domain}, registering an {@code unrated} outlet when the domain is unknown.
     *
     * @param domain the article's domain ({@link ArticleUrls#domain})
     * @param countryIso2 the source country, or null
     * @param language the language code, or null
     * @return the outlet id, or null when the registry cannot answer (the payload then carries no outlet id)
     */
    String resolve(String domain, String countryIso2, String language);
}
