package ai.foresignal.source.adapters.celestrak;

// Origin: treating an error page or an empty body as a failure (never as data) follows God's Eye View
// (MIT, Copyright (c) 2026 Bilawal Sidhu): server/providers/space/celestrak.js (fetchUpstream). See
// THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamHaltException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.util.OptionalInt;

/**
 * CelesTrak's usage policy of 2026-05-22 as code (prompt 064; {@code https://celestrak.org/usage-policy.php}),
 * shared by {@code celestrak-gp} and {@code celestrak-socrates}.
 *
 * <ul>
 *   <li>One conditional GET per call ({@code getConditional}), so validators are sent if CelesTrak ever
 *       publishes them; calls are made one after another, never concurrently.
 *   <li>A 304, and a 403 whose body says "GP data has not updated since your last successful download", are
 *       <em>not modified</em>: no error and no backoff.
 *   <li>Any other non-200, any redirect (the sources do not follow redirects) and a 200 whose body is not the
 *       expected format throw {@link UpstreamHaltException}: more than 50 errors in 2 h firewalls the address,
 *       and machine clients must stop and alert a human. The source is held for 24 h; fetch-now clears it.
 * </ul>
 */
final class CelestrakPolicy {

    /** The body CelesTrak sends with a 403 for a repeat download inside one update. */
    static final String NOT_UPDATED = "has not updated since your last successful download";

    private CelestrakPolicy() {}

    /**
     * Fetches {@code uri} once under the policy.
     *
     * @param stateKey the conditional-GET validator key
     * @return the 200 response, or null when CelesTrak says nothing changed
     * @throws UpstreamHaltException on any status the policy says to stop on
     * @throws AdapterException on a network failure (ordinary failure and backoff)
     */
    static UpstreamResponse get(FetchContext ctx, URI uri, String stateKey) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().getConditional(uri, stateKey);
        } catch (UpstreamHaltException e) {
            throw e;
        } catch (AdapterException e) {
            OptionalInt status = e.httpStatus();
            if (status.isEmpty()) {
                throw e;
            }
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (status.getAsInt() == 403 && message.contains(NOT_UPDATED)) {
                ctx.log().debug("{}: {} not updated since the last download", ctx.sourceId(), uri);
                return null;
            }
            // The health reason stays fixed; CelesTrak's own words go to the log so a halt can be diagnosed.
            ctx.log().warn("{}: {} answered {}: {}", ctx.sourceId(), uri, status.getAsInt(),
                    message.length() > 300 ? message.substring(0, 300) : message);
            throw halt(status.getAsInt());
        }
        if (response.notModified()) {
            return null;
        }
        if (response.status() != 200) {
            throw halt(response.status());
        }
        return response;
    }

    /** The halt of an unexpected status. */
    static UpstreamHaltException halt(int status) {
        return new UpstreamHaltException(status, reason(Integer.toString(status)));
    }

    /** The halt of a 200 whose body is not CSV or JSON, after forgetting the validators it stored. */
    static UpstreamHaltException unexpectedBody(FetchContext ctx, String stateKey, String what) {
        SourceHttp.forgetValidators(ctx.state(), stateKey);
        return new UpstreamHaltException(0, reason("200 with " + what));
    }

    /** The health reason: "CelesTrak returned {status}; automatic retries stopped per CelesTrak usage policy …". */
    static String reason(String status) {
        return "CelesTrak returned " + status + "; " + UpstreamHaltException.MARKER
                + " per CelesTrak usage policy — investigate, then use fetch-now";
    }
}
