package ai.foresignal.mcp.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** An {@link IdpDocumentFetcher} serving documents from memory and recording every fetch (prompt 141 tests). */
final class FakeIdpFetcher implements IdpDocumentFetcher {

    private final Map<String, String> documents = new ConcurrentHashMap<>();
    private final List<String> fetched = new ArrayList<>();

    /** Serves {@code body} at {@code uri}. */
    FakeIdpFetcher serve(String uri, String body) {
        documents.put(uri, body);
        return this;
    }

    /** Stops serving {@code uri} (it answers 404). */
    FakeIdpFetcher remove(String uri) {
        documents.remove(uri);
        return this;
    }

    @Override
    public synchronized String fetch(URI uri) throws IOException {
        fetched.add(uri.toString());
        String body = documents.get(uri.toString());
        if (body == null) {
            throw new IOException(uri.getHost() + " answered 404 for " + uri.getPath());
        }
        return body;
    }

    /** How often {@code uri} was fetched. */
    synchronized long count(String uri) {
        return fetched.stream().filter(uri::equals).count();
    }

    /** Every fetch, in order. */
    synchronized List<String> fetched() {
        return List.copyOf(fetched);
    }
}
