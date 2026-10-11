package ai.elaro.source.spi;

/**
 * What a streaming adapter is given when its stream is started.
 *
 * <p>Same surface as {@link FetchContext}, plus one addition: a stream's cost is not one request
 * but a continuing flow of bytes, and nothing in the request path can see it. The connection
 * attempt is budgeted and metered by {@code SourceHttp.openWebSocket}; everything that arrives
 * afterwards is only visible to the adapter, so the adapter reports it.
 */
public interface StreamContext extends FetchContext {

    /**
     * Reports bytes received on an open stream, so a streaming source's spend is measured rather
     * than left at the single connection request.
     *
     * <p>Call it per batch rather than per frame: the meter is cheap but not free, and per-minute
     * accounting cannot tell the difference.
     *
     * @param bytes bytes received since the last call; a non-positive value is ignored
     */
    void recordReceivedBytes(long bytes);
}
