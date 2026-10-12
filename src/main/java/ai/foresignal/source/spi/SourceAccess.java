package ai.foresignal.source.spi;

/**
 * How a source reaches its data and what enabling it involves (OpenAPI {@code SourceAccess}).
 *
 * <p>Shown to the operator before a source is enabled or an external provider is approved. Prompt
 * 024 copies it from an EDP manifest's {@code source.access} and {@code source.terms}.
 *
 * @param route {@code official}, {@code unofficial-api}, {@code scrape}, {@code browser} or
 *     {@code other}; required
 * @param termsSummary a one-line summary of the upstream's terms, or null
 * @param termsUrl where the terms are, or null
 * @param fragility {@code low}, {@code medium} or {@code high}, or null when not stated
 */
public record SourceAccess(String route, String termsSummary, String termsUrl, String fragility) {

    /** Requires a route. */
    public SourceAccess {
        if (route == null || route.isBlank()) {
            throw new IllegalArgumentException("SourceAccess needs a route");
        }
    }
}
