package ai.foresignal.collect;

import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.SourceDescriptor;

/**
 * Supplies the {@link MediaSink} behind {@code FetchContext.media()} (prompt 043).
 *
 * <p>Declared here and implemented by the media store ({@code ai.foresignal.media}), so the collector
 * hands adapters a sink without depending on the media package.
 */
public interface MediaSinkFactory {

    /**
     * The sink for one fetch, probe or stream of {@code descriptor}'s source.
     *
     * @param probing true for a probe: the sink writes nothing and returns previews
     */
    MediaSink forSource(SourceDescriptor descriptor, boolean probing);
}
