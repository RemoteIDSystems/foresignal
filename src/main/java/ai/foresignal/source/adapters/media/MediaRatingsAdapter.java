package ai.foresignal.source.adapters.media;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * {@code media-ratings} (prompt 133): the upstream path of the {@code wikidata} and {@code partisan-web} rating
 * imports. Its own fetch writes nothing (the imports run through {@code ProbeService.probeCall} so their requests are
 * budgeted, carry Foresignal's User-Agent and follow backoff); ratings live in the outlet registry, never in records.
 *
 * <p>Hosts: {@code query.wikidata.org} (the Wikidata Query Service; its User-Agent policy is met by Foresignal's
 * identifying User-Agent with the operator's contact), {@code api.github.com} and {@code raw.githubusercontent.com}
 * (The Partisan Web data files). Budget 30 requests a minute and 1,000 a day.
 */
@Component
public class MediaRatingsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "media-ratings";

    /** The declared kind (no record is ever written). */
    public static final String KIND = "media-rating";

    private static final SourceDescriptor DESCRIPTOR = SourceDescriptor.builder(ID)
            .name("Media rating imports")
            .description("The budgeted upstream path of the media outlet rating imports (Wikidata, The Partisan Web)."
                    + " It writes no records: ratings are stored in the outlet registry (/v1/media/outlets) with their"
                    + " provider, license and date. Imports run from /v1/media/ratings/import and on their schedule.")
            .homepage("https://www.wikidata.org/wiki/Wikidata:Data_access")
            .attribution("Wikidata (CC0); The Partisan Web (CC BY 4.0)")
            .licence("per provider (see /v1/media/providers)")
            .mode(Mode.POLLING)
            .egress(Egress.OPEN)
            .kind(KIND)
            .interval(Duration.ofHours(24), Duration.ofHours(24))
            .defaultBudget(Budget.of(30, 1000))
            .maxResponseBytes(16 * 1024 * 1024)
            .fetchTimeout(Duration.ofMinutes(5))
            .historyTier(HistoryTier.STANDARD)
            .defaultIngestPolicy(IngestPolicy.all())
            .observedAtMode(ObservedAtMode.RECEIVED)
            .recordTtl(null)
            .zeroResultsNormal(true)
            .defaultRetention(Duration.ofDays(90))
            .build();

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** Writes nothing: the imports use this source's upstream path through probe contexts. */
    @Override
    public FetchResult fetch(FetchContext ctx) {
        return FetchResult.of(List.of());
    }
}
