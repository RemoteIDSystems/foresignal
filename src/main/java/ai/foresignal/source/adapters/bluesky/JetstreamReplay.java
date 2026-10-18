package ai.foresignal.source.adapters.bluesky;

import ai.foresignal.source.formats.social.SocialJson;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jetstream Network Replay planning (prompt 135; bsky.network/docs/jetstream-replay and the
 * {@code network.bsky.jetstream.planSnapshot} lexicon, verified 2026-10-17): {@code POST
 * /xrpc/network.bsky.jetstream.planSnapshot} on the instance's HTTPS host with {@code Authorization: Bearer {apiToken}}
 * and {@code {kinds, collections, afterSeq}}; the answer names {@code sealedTipSeq}, {@code plannedThroughSeq} and the
 * matching {@code segments}.
 *
 * <p><strong>Not applied:</strong> the byte format of {@code getSegment} could not be verified, so Foresignal does
 * not decode segments; the plan is reported in the gap's reason and the gap stays "not replayed". Decoding segments
 * is left for when the format is documented.
 */
final class JetstreamReplay {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private JetstreamReplay() {}

    /**
     * Describes what a replay could cover from {@code afterSeq}, without applying it.
     *
     * @param ctx the stream context (budgeted HTTP, credentials)
     * @param wsBase the instance's WebSocket base URL
     * @param afterSeq the last seq seen, or null
     * @return a short description for the gap reason
     */
    static String describe(StreamContext ctx, String wsBase, Long afterSeq) {
        String token = ctx.credentials() == null ? null : ctx.credentials().get("apiToken");
        if (token == null || token.isBlank()) {
            return "network replay is on but no apiToken is set";
        }
        if (afterSeq == null) {
            return "network replay needs a stored seq";
        }
        String https = wsBase.replaceFirst("^wss://", "https://").replaceFirst("^ws://", "http://")
                .replaceAll("/+$", "");
        String body = "{\"kinds\":[\"commit\"],\"collections\":[\"" + BlueskyRecords.POST + "\",\""
                + BlueskyRecords.REPOST + "\"],\"afterSeq\":" + afterSeq + "}";
        try {
            UpstreamResponse response = ctx.http().post(URI.create(https + "/xrpc/network.bsky.jetstream.planSnapshot"),
                    "application/json", body.getBytes(StandardCharsets.UTF_8),
                    Map.of("Authorization", "Bearer " + token.strip()));
            if (response.status() != 200) {
                return "network replay plan answered HTTP " + response.status();
            }
            JsonNode plan = JSON.readTree(response.body());
            JsonNode segments = plan.get("segments");
            return "network replay could cover seq " + afterSeq + " to "
                    + SocialJson.text(plan, "plannedThroughSeq") + " in " + (segments == null ? 0 : segments.size())
                    + " segment(s) (not applied: the segment format is not verified)";
        } catch (AdapterException | RuntimeException e) {
            return "network replay plan failed: " + e.getMessage();
        }
    }
}
