package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.KitUrls;

/**
 * Settings shared by the fire sources of prompt 075 ({@code nifc-perimeters}, {@code inciweb-incidents},
 * {@code cwfis-*}).
 */
public final class FireSources {

    /**
     * The test-only property moving every fire upstream URL under one base as {@code {base}/{host}{path}?{query}}
     * (blank in production).
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.fire.base-url";

    private FireSources() {}

    /**
     * {@code url} under the test base, or unchanged when {@code base} is blank.
     *
     * @param url the production URL
     * @param base the value of {@value #BASE_URL_PROPERTY}
     */
    public static String rebased(String url, String base) {
        return KitUrls.rebased(url, base);
    }
}
