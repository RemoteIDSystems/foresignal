package ai.foresignal.testsupport.raster;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An in-memory {@link SourceHttp} that answers the exact URLs a test registers with captured fixture
 * bytes (checkpoint 124 capture contract tests): {@code GET} and {@code download} by URL, ranged reads
 * by URL and byte range. Everything else answers 404, like S3, and every request is recorded so a test
 * can assert the production adapter asked for the real upstream URL.
 */
public final class FixtureHttp implements SourceHttp {

    private final Map<String, byte[]> bodies = new LinkedHashMap<>();
    private final Map<String, byte[]> ranges = new LinkedHashMap<>();
    private final List<String> requests = new ArrayList<>();
    private final Path downloads;

    /**
     * Creates the stub.
     *
     * @param downloads the directory {@link #download} writes into
     */
    public FixtureHttp(Path downloads) {
        this.downloads = downloads;
    }

    /** Answers {@code GET url} (and {@code download url}) with {@code body}. */
    public synchronized FixtureHttp serve(String url, byte[] body) {
        bodies.put(url, body);
        return this;
    }

    /** Answers a ranged read of {@code url} from {@code start} to {@code end} inclusive with {@code body}. */
    public synchronized FixtureHttp serveRange(String url, long start, long end, byte[] body) {
        ranges.put(url + " " + start + "-" + end, body);
        return this;
    }

    /** Every request, as {@code GET url}, {@code DOWNLOAD url} or {@code RANGE url a-b}, in order. */
    public synchronized List<String> requests() {
        return List.copyOf(requests);
    }

    @Override
    public synchronized UpstreamResponse get(URI uri) throws AdapterException {
        requests.add("GET " + uri);
        byte[] body = bodies.get(uri.toString());
        if (body == null) {
            throw new UpstreamHttpException(404, "not a registered fixture URL: " + uri);
        }
        return new UpstreamResponse(200, Map.of(), body, "application/octet-stream", false, false);
    }

    @Override
    public UpstreamResponse get(URI uri, Map<String, String> headers) throws AdapterException {
        return get(uri);
    }

    @Override
    public UpstreamResponse getConditional(URI uri, String stateKey) throws AdapterException {
        return get(uri);
    }

    @Override
    public UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers) {
        throw new UnsupportedOperationException("FixtureHttp serves GETs only");
    }

    @Override
    public synchronized UpstreamResponse getRange(URI uri, long start, long end, long maxBytes)
            throws AdapterException {
        requests.add("RANGE " + uri + " " + start + "-" + end);
        byte[] body = ranges.get(uri + " " + start + "-" + end);
        if (body == null) {
            throw new UpstreamHttpException(404, "not a registered fixture range: " + uri + " " + start + "-" + end);
        }
        return new UpstreamResponse(206, Map.of(), body, "application/octet-stream", false, false);
    }

    @Override
    public synchronized DownloadResult download(URI uri, Path target, long maxBytes) throws AdapterException {
        requests.add("DOWNLOAD " + uri);
        byte[] body = bodies.get(uri.toString());
        if (body == null) {
            throw new UpstreamHttpException(404, "not a registered fixture URL: " + uri);
        }
        try {
            Files.createDirectories(downloads);
            Path file = downloads.resolve(target.getFileName().toString());
            Files.write(file, body);
            return new DownloadResult(file, body.length, "application/octet-stream", null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public S3Listing listS3(String bucket, String prefix, String startAfter) {
        return new S3Listing(List.of(), false, null);
    }

    @Override
    public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers) {
        throw new UnsupportedOperationException("FixtureHttp serves GETs only");
    }
}
