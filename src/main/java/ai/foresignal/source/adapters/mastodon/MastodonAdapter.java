package ai.foresignal.source.adapters.mastodon;

import ai.foresignal.social.SocialPlatformSettings;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.TermIndex;
import ai.foresignal.source.formats.social.SocialDescriptors;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code mastodon} (prompt 135): for every instance in the platform option {@code instances}, hashtag streams over the
 * multiplexed WebSocket {@code /api/v1/streaming} (subscribe messages {@code {"type":"subscribe","stream":"hashtag",
 * "tag":…}}; the streaming base from {@code /api/v2/instance} {@code configuration.urls.streaming}; since Mastodon 4.2
 * a <em>user</em> token with {@code read:statuses} is required), {@code /api/v1/trends/tags} hourly (daily
 * {@code uses} and {@code accounts} history of trending tags that match a tracked topic, as {@code series-point}
 * records {@code mastodon-trend:{instance}:{tag}}), and the statuses of followed accounts every
 * {@value #ACCOUNT_POLL_MINUTES} minutes. Verified on docs.joinmastodon.org 2026-10-17. Off by default.
 *
 * <p>A hashtag stream is complete for its filter on that instance, but it is not the platform: its posts never count
 * for a denominator. Followed hashtags are the topics' hashtags (for topics that apply to Mastodon), the topics'
 * {@code mastodon-hashtag} channels and the platform channels written {@code #tag}.
 */
@Component
public class MastodonAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.MASTODON_ADAPTER;

    /** Test-only override: every instance is reached at this base URL (http or https). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.mastodon.base-url";

    /** How often followed accounts are polled. */
    public static final int ACCOUNT_POLL_MINUTES = 15;

    /** How often trends are read. */
    public static final Duration TRENDS_EVERY = Duration.ofHours(1);

    private static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(15),
            Duration.ofMinutes(10), 0.2);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String override;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;

    /** Creates the adapter. */
    @Autowired
    public MastodonAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override,
            ObjectProvider<SocialRuntime> runtimes) {
        this.override = override == null || override.isBlank() ? null : override.strip().replaceAll("/+$", "");
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public MastodonAdapter(String override, SocialRuntime runtime) {
        this.override = override == null || override.isBlank() ? null : override.strip().replaceAll("/+$", "");
        this.runtimes = null;
        this.fixed = runtime;
    }

    SocialRuntime runtime() {
        return fixed != null ? fixed : runtimes == null ? null : runtimes.getIfAvailable();
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The HTTPS base of an instance (or the test override). */
    String httpBase(String instance) {
        return override != null ? override : "https://" + instance;
    }

    /** True when a test override is set (streaming base URLs then follow it too). */
    boolean overridden() {
        return override != null;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        SocialRuntime runtime = runtime();
        if (runtime == null) {
            throw new AdapterException(ID + ": the social runtime is not available");
        }
        SocialPlatformSettings settings = runtime.settings(ID);
        List<String> instances = settings.listOption("instances").stream()
                .map(s -> s.toLowerCase(Locale.ROOT)).distinct().toList();
        MastodonSession session = new MastodonSession(this, ctx, sink, runtime, instances,
                hashtags(runtime.index(), settings), accounts(runtime.index(), instances));
        session.open();
        return session;
    }

    /** The hashtags followed on every instance. */
    static List<String> hashtags(TermIndex index, SocialPlatformSettings settings) {
        Set<String> out = new LinkedHashSet<>();
        for (TermIndex.TopicEntry e : index.topics()) {
            SocialTopicBlock b = e.block();
            if (b.cueOnly() || (!b.platforms().isEmpty() && !b.platforms().contains(SocialVocabulary.MASTODON))) {
                continue;
            }
            for (String tag : b.hashtags()) {
                out.add(bare(tag));
            }
            for (SocialTopicBlock.Channel c : b.channels()) {
                if ("mastodon-hashtag".equals(c.type())) {
                    out.add(bare(c.id()));
                }
            }
        }
        for (String channel : settings.channels()) {
            if (channel.startsWith("#")) {
                out.add(bare(channel));
            }
        }
        out.removeIf(String::isBlank);
        return List.copyOf(out);
    }

    /** The followed accounts by instance: {@code user@host} handles of topics, or bare users on the first instance. */
    static Map<String, List<String>> accounts(TermIndex index, List<String> instances) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (TermIndex.TopicEntry e : index.topics()) {
            if (e.block().cueOnly()) {
                continue;
            }
            for (SocialTopicBlock.Account a : e.block().accounts()) {
                if (!SocialVocabulary.MASTODON.equals(a.platform())) {
                    continue;
                }
                String handle = a.handle().startsWith("@") ? a.handle().substring(1) : a.handle();
                int at = handle.indexOf('@');
                String host = at > 0 ? handle.substring(at + 1).toLowerCase(Locale.ROOT) : instances.isEmpty() ? null
                        : instances.get(0);
                String user = at > 0 ? handle.substring(0, at) : handle;
                if (host != null && instances.contains(host)) {
                    out.computeIfAbsent(host, k -> new ArrayList<>()).add(user);
                }
            }
        }
        return out;
    }

    private static String bare(String tag) {
        String t = tag.strip();
        while (t.startsWith("#")) {
            t = t.substring(1);
        }
        return t.toLowerCase(Locale.ROOT);
    }

    /** The token for an instance: its entry in {@code instanceTokens} ({@code host=token,…}), else {@code accessToken}. */
    static String token(Map<String, String> credentials, String instance) {
        if (credentials == null) {
            return null;
        }
        String per = credentials.get("instanceTokens");
        if (per != null) {
            for (String pair : per.split(",")) {
                int eq = pair.indexOf('=');
                if (eq > 0 && pair.substring(0, eq).strip().equalsIgnoreCase(instance)) {
                    return pair.substring(eq + 1).strip();
                }
            }
        }
        String token = credentials.get("accessToken");
        return token == null || token.isBlank() ? null : token.strip();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Mastodon")
                .description("Mastodon instances the operator lists: hashtag streams (a user token with read:statuses"
                        + " per instance), trending tags hourly and followed accounts' statuses. Each instance's rules"
                        + " apply. Off by default.")
                .homepage("https://docs.joinmastodon.org/")
                .attribution("The Mastodon instances listed in the platform settings")
                .licence("Each instance's terms; posts remain their authors'")
                .termsUrl("https://docs.joinmastodon.org/methods/streaming/")
                .mode(Mode.STREAMING)
                .credential(CredentialField.required("accessToken",
                        "A user access token with read:statuses (and read:accounts for followed accounts)"))
                .credential(CredentialField.optional("instanceTokens",
                        "Per-instance tokens as host=token pairs separated by commas; accessToken covers the rest"))
                .defaultBudget(Budget.of(20, 3000))
                .reconnect(RECONNECT)
                .kind(SeriesPoint.KIND)
                .kindRetention(SeriesPoint.KIND, SeriesPoint.RETENTION);
        SocialDescriptors.posts(b, SocialVocabulary.MASTODON);
        SeriesPoint.declareFieldSpecs(b);
        return b.build();
    }
}
