package ai.foresignal.source.spi;

/** A source that pushes to Foresignal over a held-open connection. */
public interface StreamingAdapter extends SourceAdapter {

    /**
     * Opens the stream and pushes records to {@code sink} until the returned handle is closed.
     *
     * <p>{@code sink.accept} may block when the ingest queue is full; see {@link RecordSink}.
     *
     * @throws AdapterException when the stream could not be opened
     */
    StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException;
}
