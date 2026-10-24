package ai.foresignal.social;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.HealthMapper;
import ai.foresignal.collect.HealthService;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceCredentialsChangedEvent;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.Budget;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The operator's social platform adapters (prompt 135, {@code /v1/social/platforms}): their settings
 * ({@code social_platform}), their state, and the 004/005 source each one is.
 *
 * <ul>
 *   <li><b>Seeding:</b> one row per adapter at startup with the catalog defaults. {@code bluesky-jetstream} is the
 *       only adapter on by default: when its row is first created (and {@code foresignal.social.default-enable} is
 *       true, off in tests), its source is enabled. Afterwards the operator's choice stands.
 *   <li><b>Settings:</b> {@code PUT} replaces the settings; {@code enabled}, the budget and the post retention are
 *       applied to the source ({@code SourceService}), the retention as the source's retention override. Enabling a
 *       keyed adapter without credentials leaves the source off and the state {@code credentials-required}; storing
 *       the credentials later ({@code PUT /v1/sources/{id}/credentials}) turns the source on.
 *   <li><b>State:</b> {@code disabled}, {@code credentials-required}, {@code budget-exhausted} (money cap or quota),
 *       {@code unavailable} (failing source), {@code degraded} (degraded source or a stream that is down), else
 *       {@code enabled}; a change emits {@code social.platform.state}.
 * </ul>
 */
@Service
public class SocialPlatformService {

    private static final Logger log = LoggerFactory.getLogger(SocialPlatformService.class);

    /** X modes. */
    public static final List<String> X_MODES = List.of("counts", "search", "counts+search");

    /** The longest post retention. */
    public static final Duration MAX_RETENTION = Duration.ofDays(3650);

    /** The most channels. */
    public static final int MAX_CHANNELS = 200;

    private static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern HOST = Pattern.compile("^[A-Za-z0-9.-]{1,253}(:\\d{1,5})?$");

    private final SocialPlatformRepository rows;
    private final SourceService sources;
    private final SourceRegistry registry;
    private final ObjectProvider<HealthService> health;
    private final StreamLedger ledger;
    private final QuotaLedger quotas;
    private final XSpend spend;
    private final StoredPosts stored;
    private final TermIndexService terms;
    private final SocialEvents events;
    private final Clock clock;
    private final boolean defaultEnable;

    /** Creates the service. */
    public SocialPlatformService(SocialPlatformRepository rows, SourceService sources, SourceRegistry registry,
            ObjectProvider<HealthService> health, StreamLedger ledger, QuotaLedger quotas, XSpend spend,
            StoredPosts stored, TermIndexService terms, SocialEvents events, Clock clock,
            @Value("${foresignal.social.default-enable:true}") boolean defaultEnable) {
        this.rows = rows;
        this.sources = sources;
        this.registry = registry;
        this.health = health;
        this.ledger = ledger;
        this.quotas = quotas;
        this.spend = spend;
        this.stored = stored;
        this.terms = terms;
        this.events = events;
        this.clock = clock;
        this.defaultEnable = defaultEnable;
    }

    /** Seeds the rows at startup. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            seed();
        } catch (RuntimeException e) {
            log.warn("Could not seed the social platforms: {}", e.toString());
        }
    }

    /**
     * Creates the missing rows with the catalog defaults; a newly created default-on adapter has its source enabled
     * (when {@code foresignal.social.default-enable} is true).
     *
     * @return the rows created
     */
    @Transactional
    public int seed() {
        int created = 0;
        Instant now = clock.instant();
        for (SocialPlatformCatalog.Definition d : SocialPlatformCatalog.all()) {
            if (rows.existsById(d.id()) || !registry.contains(d.id())) {
                continue;
            }
            SocialPlatformEntity row = new SocialPlatformEntity(d.id(), now);
            apply(row, SocialPlatformSettings.defaults(d));
            row.setEnabled(d.defaultEnabled() && defaultEnable);
            rows.save(row);
            created++;
            if (row.isEnabled()) {
                try {
                    sources.update(d.id(), SourceSettingsUpdate.enabled(true));
                    log.info("Social platform {} is on by default (free and open); disable it on"
                            + " /v1/social/platforms/{}", d.id(), d.id());
                } catch (RuntimeException e) {
                    log.warn("Could not enable {}: {}", d.id(), e.toString());
                }
            }
        }
        return created;
    }

    private static void apply(SocialPlatformEntity row, SocialPlatformSettings s) {
        row.setEnabled(s.enabled());
        row.setMode(s.mode());
        row.setChannels(s.channels());
        row.setDeletionSync(s.deletionSync());
        row.setPostRetention(s.postRetention() == null ? null : Durations.format(s.postRetention()));
        row.setMonthlyCapUsd(s.monthlyCapUsd());
        List<Map<String, Object>> prices = new ArrayList<>();
        for (SocialPlatformCatalog.Price p : s.prices()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("item", p.item());
            m.put("usd", p.usd());
            m.put("per", p.per());
            prices.add(m);
        }
        row.setPrices(prices);
        row.setPricesAsOf(s.pricesAsOf());
        row.setOptions(s.options());
    }

    /**
     * The settings of an adapter as adapters read them (catalog defaults when no row exists yet).
     *
     * @throws NotFoundException 404 {@code UNKNOWN_PLATFORM}
     */
    @Transactional(readOnly = true)
    public SocialPlatformSettings settings(String platformId) {
        SocialPlatformCatalog.Definition d = definition(platformId);
        SocialPlatformEntity row = rows.findById(platformId).orElse(null);
        return row == null ? SocialPlatformSettings.defaults(d) : settingsOf(row);
    }

    static SocialPlatformSettings settingsOf(SocialPlatformEntity row) {
        List<SocialPlatformCatalog.Price> prices = new ArrayList<>();
        for (Map<String, Object> m : row.getPrices()) {
            try {
                prices.add(new SocialPlatformCatalog.Price(String.valueOf(m.get("item")),
                        new BigDecimal(String.valueOf(m.get("usd"))), m.get("per") == null ? 1
                        : Integer.parseInt(String.valueOf(m.get("per")))));
            } catch (RuntimeException e) {
                // a damaged price is left out
            }
        }
        Duration retention;
        try {
            retention = row.getPostRetention() == null ? SocialVocabulary.DEFAULT_POST_RETENTION
                    : Durations.parse(row.getPostRetention());
        } catch (RuntimeException e) {
            retention = SocialVocabulary.DEFAULT_POST_RETENTION;
        }
        return new SocialPlatformSettings(row.getId(), row.isEnabled(), row.getMode(), row.getChannels(),
                row.isDeletionSync(), retention, row.getMonthlyCapUsd(), prices, row.getPricesAsOf(),
                row.getOptions());
    }

    /** The catalog definition, or 404 {@code UNKNOWN_PLATFORM}. */
    public static SocialPlatformCatalog.Definition definition(String platformId) {
        return SocialPlatformCatalog.get(platformId).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_PLATFORM, "No social platform adapter has the id '" + platformId + "'."));
    }

    /** Every adapter's view. */
    @Transactional
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SocialPlatformCatalog.Definition d : SocialPlatformCatalog.all()) {
            if (registry.contains(d.id())) {
                out.add(view(d.id()));
            }
        }
        return out;
    }

    /**
     * One adapter's view (OpenAPI {@code SocialPlatform}).
     *
     * @throws NotFoundException 404 {@code UNKNOWN_PLATFORM}
     */
    @Transactional
    public Map<String, Object> view(String platformId) {
        SocialPlatformCatalog.Definition d = definition(platformId);
        SocialPlatformEntity row = rows.findById(platformId).orElse(null);
        SocialPlatformSettings s = row == null ? SocialPlatformSettings.defaults(d) : settingsOf(row);
        SourceView source = sourceView(platformId);
        State state = state(d, s, source);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", d.id());
        out.put("network", d.network());
        out.put("name", d.name());
        out.put("state", state.state());
        out.put("stateReason", state.reason());
        out.put("defaultEnabled", d.defaultEnabled());
        out.put("type", d.type());
        out.put("mode", s.mode());
        List<Map<String, Object>> paths = new ArrayList<>();
        for (SocialPlatformCatalog.PathDef p : d.paths()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("path", p.path());
            m.put("collectionType", p.collectionType());
            m.put("enabled", s.enabled() && pathEnabled(d, s, p));
            m.put("feedsBaselines", SocialVocabulary.feedsBaselines(p.collectionType()));
            paths.add(m);
        }
        out.put("paths", paths);
        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("required", d.credentialsRequired());
        credentials.put("configured", source != null && source.credentialsSet());
        credentials.put("fields", d.credentialFields());
        out.put("credentials", credentials);
        out.put("health", healthOf(platformId));
        out.put("stream", d.type().startsWith("STREAMING") ? streamView(platformId) : null);
        out.put("quotas", quotaViews(d));
        out.put("spend", spendView(d, s));
        Map<String, Object> deletion = new LinkedHashMap<>();
        deletion.put("enabled", s.deletionSync());
        deletion.put("lastRunAt", row == null || row.getDeletionLastRunAt() == null ? null
                : row.getDeletionLastRunAt().toString());
        long tombstoned = 0;
        try {
            tombstoned = stored.tombstonedSince(platformId, clock.instant().minus(Duration.ofDays(30)));
        } catch (RuntimeException e) {
            log.debug("No tombstone count for {}: {}", platformId, e.toString());
        }
        deletion.put("tombstonedLast30d", tombstoned);
        out.put("deletionSync", deletion);
        Map<String, Object> termsView = new LinkedHashMap<>();
        termsView.put("summary", d.termsSummary());
        termsView.put("url", d.termsUrl());
        termsView.put("costs", d.costs());
        out.put("terms", termsView);
        out.put("settings", settingsView(s, source));
        out.put("updatedAt", (row == null ? clock.instant() : row.getUpdatedAt()).toString());
        if (row != null) {
            announce(row, state, d);
        }
        return out;
    }

    /** True when a path's switch (when it has one) is on and its mode allows it. */
    static boolean pathEnabled(SocialPlatformCatalog.Definition d, SocialPlatformSettings s,
            SocialPlatformCatalog.PathDef p) {
        if (SocialVocabulary.X_ADAPTER.equals(d.id())) {
            String mode = s.mode() == null ? "counts" : s.mode();
            if ("counts".equals(p.path())) {
                return mode.contains("counts") && s.option("standingCounts", true);
            }
            if ("search".equals(p.path())) {
                return mode.contains("search");
            }
        }
        return p.option() == null || s.option(p.option(), false);
    }

    private SourceView sourceView(String platformId) {
        try {
            return sources.view(platformId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Object healthOf(String platformId) {
        HealthService service = health.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            return HealthMapper.health(service.health(platformId));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Map<String, Object> streamView(String platformId) {
        StreamLedger.State st = ledger.state(platformId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lagSeconds", st.lagSeconds());
        out.put("cursor", st.cursor());
        out.put("connectedSince", st.connectedSince() == null ? null : st.connectedSince().toString());
        if (st.lastGap() == null) {
            out.put("lastGap", null);
        } else {
            Map<String, Object> gap = new LinkedHashMap<>();
            gap.put("from", st.lastGap().from().toString());
            gap.put("to", (st.lastGap().to() == null ? clock.instant() : st.lastGap().to()).toString());
            gap.put("replayed", st.lastGap().replayed());
            out.put("lastGap", gap);
        }
        return out;
    }

    private List<Map<String, Object>> quotaViews(SocialPlatformCatalog.Definition d) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SocialPlatformCatalog.QuotaDef q : d.quotas()) {
            Duration window = QuotaUnits.window(q);
            long used = 0;
            Instant resets = null;
            try {
                used = QuotaUnits.used(quotas, d.id(), q, window);
                resets = quotas.resetsAt(d.id(), QuotaUnits.unitKey(q), window);
            } catch (RuntimeException e) {
                log.debug("No quota use for {}: {}", d.id(), e.toString());
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("unit", q.unit());
            m.put("limit", q.limit());
            m.put("window", q.window());
            m.put("rolling", q.rolling());
            m.put("used", used);
            m.put("remaining", q.limit() == null ? null : Math.max(0, q.limit() - used));
            m.put("resetsAt", resets == null ? null : resets.toString());
            m.put("description", q.description());
            out.add(m);
        }
        return out;
    }

    private Map<String, Object> spendView(SocialPlatformCatalog.Definition d, SocialPlatformSettings s) {
        if (d.defaultMonthlyCapUsd() == null && s.monthlyCapUsd() == null) {
            return null;
        }
        XSpend.Month m;
        BigDecimal projected = null;
        try {
            m = spend.month(d.id());
            projected = spend.projected(d.id());
        } catch (RuntimeException e) {
            m = XSpend.Month.ZERO;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("currency", "USD");
        out.put("spentThisMonth", m.spentUsd());
        out.put("monthlyCapUsd", s.monthlyCapUsd());
        out.put("projectedThisMonth", projected);
        out.put("standingCountsEstimateMonthly", SocialVocabulary.X_ADAPTER.equals(d.id())
                ? XSpend.standingCountsMonthly(s, standingTopics(SocialVocabulary.X)) : null);
        out.put("pricesAsOf", s.pricesAsOf());
        out.put("refusedThisMonth", m.refused());
        return out;
    }

    /** The standing (not cue-only) topics that apply to a network. */
    int standingTopics(String network) {
        int n = 0;
        for (TermIndex.TopicEntry e : terms.current().topics()) {
            if (!e.block().cueOnly() && (e.block().platforms().isEmpty() || e.block().platforms().contains(network))) {
                n++;
            }
        }
        return n;
    }

    private static Map<String, Object> settingsView(SocialPlatformSettings s, SourceView source) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", s.enabled());
        out.put("mode", s.mode());
        out.put("channels", s.channels());
        out.put("deletionSync", s.deletionSync());
        out.put("postRetention", Durations.format(s.postRetention()));
        if (source != null && source.budget() != null) {
            Map<String, Object> budget = new LinkedHashMap<>();
            budget.put("perMinute", source.budget().perMinute());
            budget.put("perDay", source.budget().perDay());
            if (source.budget().mbPerDay() != null) {
                budget.put("mbPerDay", source.budget().mbPerDay());
            }
            out.put("budget", budget);
        } else {
            out.put("budget", null);
        }
        out.put("monthlyCapUsd", s.monthlyCapUsd());
        List<Map<String, Object>> prices = new ArrayList<>();
        for (SocialPlatformCatalog.Price p : s.prices()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("item", p.item());
            m.put("usd", p.usd());
            m.put("per", p.per());
            prices.add(m);
        }
        out.put("prices", prices);
        out.put("pricesAsOf", s.pricesAsOf());
        out.put("options", s.options());
        return out;
    }

    /**
     * An adapter state.
     *
     * @param state the state
     * @param reason why, or null
     */
    public record State(String state, String reason) {}

    /** The state of an adapter. */
    State state(SocialPlatformCatalog.Definition d, SocialPlatformSettings s, SourceView source) {
        if (!s.enabled()) {
            return new State("disabled", null);
        }
        if (source == null) {
            return new State("unavailable", "the adapter is not registered");
        }
        if (d.credentialsRequired() && !source.credentialsSet()) {
            return new State("credentials-required", "set " + String.join(", ", d.credentialFields())
                    + " with PUT /v1/sources/" + d.id() + "/credentials");
        }
        String exhausted = exhausted(d, s);
        if (exhausted != null) {
            return new State("budget-exhausted", exhausted);
        }
        if (!source.enabled()) {
            return new State("disabled", "the source is disabled");
        }
        HealthStatus status = source.health() == null ? null : source.health().status();
        if (status == HealthStatus.FAILING) {
            return new State("unavailable", source.health().statusReason());
        }
        if (status == HealthStatus.DEGRADED) {
            return new State("degraded", source.health().statusReason());
        }
        if (d.type().startsWith("STREAMING") && ledger.state(d.id()).lastGap() != null
                && ledger.state(d.id()).lastGap().to() == null) {
            return new State("degraded", "the stream is down since " + ledger.state(d.id()).lastGap().from());
        }
        return new State("enabled", null);
    }

    private String exhausted(SocialPlatformCatalog.Definition d, SocialPlatformSettings s) {
        try {
            if (s.monthlyCapUsd() != null) {
                XSpend.Month m = spend.month(d.id());
                if (m.spentUsd().compareTo(s.monthlyCapUsd()) >= 0) {
                    return "the monthly cap of $" + s.monthlyCapUsd().toPlainString() + " is spent";
                }
            }
            for (SocialPlatformCatalog.QuotaDef q : d.quotas()) {
                if (q.limit() == null || "hashtags".equals(q.unit()) || SocialVocabulary.BLUESKY_JETSTREAM.equals(
                        d.id()) || SocialVocabulary.X_ADAPTER.equals(d.id())) {
                    continue;
                }
                Duration window = QuotaUnits.window(q);
                if (QuotaUnits.used(quotas, d.id(), q, window) >= q.limit()) {
                    return q.description() + ": " + q.limit() + " " + q.unit() + " per " + q.window() + " used";
                }
            }
        } catch (RuntimeException e) {
            log.debug("No budget state for {}: {}", d.id(), e.toString());
        }
        return null;
    }

    private void announce(SocialPlatformEntity row, State state, SocialPlatformCatalog.Definition d) {
        String previous = row.getState();
        if (state.state().equals(previous)) {
            return;
        }
        row.setState(state.state());
        row.setStateReason(state.reason());
        rows.save(row);
        Map<String, Object> spendView = null;
        try {
            spendView = spendView(d, settingsOf(row));
        } catch (RuntimeException e) {
            spendView = null;
        }
        events.platformState(d.id(), state.state(), previous, state.reason(), spendView);
    }

    /** Re-reads every adapter's state and announces the changes (the minute job). */
    @Transactional
    public void refreshStates() {
        for (SocialPlatformCatalog.Definition d : SocialPlatformCatalog.all()) {
            SocialPlatformEntity row = rows.findById(d.id()).orElse(null);
            if (row == null || !registry.contains(d.id())) {
                continue;
            }
            announce(row, state(d, settingsOf(row), sourceView(d.id())), d);
        }
    }

    /**
     * Replaces an adapter's settings ({@code PUT /v1/social/platforms/{id}}).
     *
     * @param platformId the adapter
     * @param body the {@code SocialPlatformSettings} body
     * @return the updated view
     * @throws NotFoundException 404 {@code UNKNOWN_PLATFORM}
     * @throws ValidationException 400 {@code VALIDATION_FAILED}
     */
    @Transactional
    public Map<String, Object> update(String platformId, Map<String, Object> body) {
        SocialPlatformCatalog.Definition d = definition(platformId);
        if (!registry.contains(platformId)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PLATFORM, "The adapter '" + platformId
                    + "' is not registered.");
        }
        List<FieldError> errors = new ArrayList<>();
        Parsed parsed = parse(d, body == null ? Map.of() : body, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The platform settings are not valid.", errors);
        }
        Instant now = clock.instant();
        SocialPlatformEntity row = rows.findById(platformId).orElseGet(() -> new SocialPlatformEntity(platformId,
                now));
        apply(row, parsed.settings());
        row.setUpdatedAt(now);
        rows.save(row);
        boolean credentialsOk = !d.credentialsRequired() || sources.credentialsSet(platformId);
        boolean enable = parsed.settings().enabled() && credentialsOk;
        // The platform's budget object has no money cap of its own (its cap is monthlyCapUsd above), so the
        // source's Budget.monthlyCapUsd, set through updateSource, is kept.
        Budget budget = parsed.budget() == null ? null
                : parsed.budget().withMonthlyCapUsd(sourceMonthlyCap(platformId));
        sources.update(platformId, new SourceSettingsUpdate(enable, null, null, budget,
                parsed.settings().postRetention(), false, null, false));
        return view(platformId);
    }

    /** The source's current {@code Budget.monthlyCapUsd}, or null. */
    private BigDecimal sourceMonthlyCap(String platformId) {
        try {
            Budget current = sources.view(platformId).budget();
            return current == null ? null : current.monthlyCapUsd();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * A parsed body.
     *
     * @param settings the settings
     * @param budget the budget, or null to leave it
     */
    record Parsed(SocialPlatformSettings settings, Budget budget) {}

    static Parsed parse(SocialPlatformCatalog.Definition d, Map<String, Object> body, List<FieldError> errors) {
        Object enabled = body.get("enabled");
        if (!(enabled instanceof Boolean)) {
            errors.add(new FieldError("enabled", "is required and must be a boolean"));
        }
        String mode = body.get("mode") == null ? null : body.get("mode").toString();
        if (SocialVocabulary.X_ADAPTER.equals(d.id())) {
            if (mode == null) {
                mode = d.defaultMode();
            } else if (!X_MODES.contains(mode)) {
                errors.add(new FieldError("mode", "must be one of " + X_MODES));
            }
        } else if (mode != null) {
            errors.add(new FieldError("mode", "is only used by x"));
        }
        List<String> channels = new ArrayList<>();
        Object rawChannels = body.get("channels");
        if (rawChannels instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Object c = list.get(i);
                if (c == null || c.toString().isBlank() || c.toString().length() > 200) {
                    errors.add(new FieldError("channels[" + i + "]", "must be 1 to 200 characters"));
                } else {
                    channels.add(c.toString().strip());
                }
            }
            if (channels.size() > MAX_CHANNELS) {
                errors.add(new FieldError("channels", "must hold at most " + MAX_CHANNELS + " channels"));
            }
        } else if (rawChannels != null) {
            errors.add(new FieldError("channels", "must be an array of strings"));
        }
        boolean deletionSync = body.get("deletionSync") instanceof Boolean b ? b : d.deletionSyncDefault();
        Duration retention = SocialVocabulary.DEFAULT_POST_RETENTION;
        if (body.get("postRetention") != null) {
            try {
                retention = Durations.parsePositive(body.get("postRetention").toString());
                if (retention.compareTo(MAX_RETENTION) > 0) {
                    errors.add(new FieldError("postRetention", "must be at most 3650d"));
                }
            } catch (RuntimeException e) {
                errors.add(new FieldError("postRetention", "is not a duration (P7D or 7d)"));
            }
        }
        Budget budget = null;
        if (body.get("budget") instanceof Map<?, ?> b) {
            Integer perMinute = integer(b.get("perMinute"));
            Integer perDay = integer(b.get("perDay"));
            Integer mb = integer(b.get("mbPerDay"));
            if (perMinute == null || perMinute < 1 || perDay == null || perDay < 1) {
                errors.add(new FieldError("budget", "needs perMinute and perDay of at least 1"));
            } else {
                budget = new Budget(perMinute, perDay, mb);
            }
        } else if (body.get("budget") != null) {
            errors.add(new FieldError("budget", "must be an object"));
        }
        BigDecimal cap = d.defaultMonthlyCapUsd();
        if (body.containsKey("monthlyCapUsd")) {
            Object raw = body.get("monthlyCapUsd");
            if (raw == null) {
                cap = null;
            } else {
                try {
                    cap = new BigDecimal(raw.toString());
                    if (cap.signum() < 0) {
                        errors.add(new FieldError("monthlyCapUsd", "must be at least 0"));
                    }
                } catch (NumberFormatException e) {
                    errors.add(new FieldError("monthlyCapUsd", "must be a number"));
                }
            }
        }
        List<SocialPlatformCatalog.Price> prices = d.defaultPrices();
        if (body.get("prices") instanceof List<?> list) {
            prices = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof Map<?, ?> m && m.get("item") != null && !m.get("item").toString().isBlank()
                        && m.get("usd") != null) {
                    try {
                        BigDecimal usd = new BigDecimal(m.get("usd").toString());
                        Integer per = m.get("per") == null ? 1 : integer(m.get("per"));
                        if (usd.signum() < 0 || per == null || per < 1) {
                            errors.add(new FieldError("prices[" + i + "]", "needs usd ≥ 0 and per ≥ 1"));
                        } else {
                            prices.add(new SocialPlatformCatalog.Price(m.get("item").toString(), usd, per));
                        }
                    } catch (NumberFormatException e) {
                        errors.add(new FieldError("prices[" + i + "].usd", "must be a number"));
                    }
                } else {
                    errors.add(new FieldError("prices[" + i + "]", "needs an item and usd"));
                }
            }
        } else if (body.get("prices") != null) {
            errors.add(new FieldError("prices", "must be an array"));
        }
        String pricesAsOf = body.containsKey("pricesAsOf") ? (body.get("pricesAsOf") == null ? null
                : body.get("pricesAsOf").toString()) : d.pricesAsOf();
        if (pricesAsOf != null && !DATE.matcher(pricesAsOf).matches()) {
            errors.add(new FieldError("pricesAsOf", "must be a date YYYY-MM-DD"));
        }
        Map<String, Object> options = new LinkedHashMap<>();
        if (body.get("options") instanceof Map<?, ?> o) {
            o.forEach((k, v) -> options.put(String.valueOf(k), v));
            Object instances = options.get("instances");
            if (instances instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    Object host = list.get(i);
                    if (host == null || !HOST.matcher(host.toString().strip()).matches()) {
                        errors.add(new FieldError("options.instances[" + i + "]", "must be an instance host name"));
                    }
                }
            } else if (instances != null) {
                errors.add(new FieldError("options.instances", "must be an array of host names"));
            }
            for (String flag : List.of("replay", "standingCounts", "fullArchive", "comments")) {
                if (options.get(flag) != null && !(options.get(flag) instanceof Boolean)) {
                    errors.add(new FieldError("options." + flag, "must be a boolean"));
                }
            }
        } else if (body.get("options") != null) {
            errors.add(new FieldError("options", "must be an object"));
        }
        return new Parsed(new SocialPlatformSettings(d.id(), Boolean.TRUE.equals(enabled), mode, channels,
                deletionSync, retention, cap, prices, pricesAsOf, options), budget);
    }

    private static Integer integer(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return v == null ? null : Integer.valueOf(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Enables or disables the source of an enabled adapter when its credentials are stored or removed. */
    @EventListener
    public void onCredentials(SourceCredentialsChangedEvent event) {
        SocialPlatformCatalog.Definition d = SocialPlatformCatalog.get(event.sourceId()).orElse(null);
        if (d == null || !d.credentialsRequired()) {
            return;
        }
        try {
            SocialPlatformEntity row = rows.findById(d.id()).orElse(null);
            if (row != null && row.isEnabled()) {
                sources.update(d.id(), SourceSettingsUpdate.enabled(event.present()));
            }
        } catch (RuntimeException e) {
            log.warn("Could not follow the credentials of {}: {}", d.id(), e.toString());
        }
    }

    /**
     * Requires an adapter to be collecting (137 cues use it).
     *
     * @throws NotFoundException 404 {@code UNKNOWN_PLATFORM}
     * @throws ai.foresignal.api.error.ConflictException 409 {@code PLATFORM_DISABLED}
     */
    public void requireEnabled(String platformId) {
        SocialPlatformSettings s = settings(platformId);
        SourceView source = sourceView(platformId);
        if (!s.enabled() || source == null || !source.enabled()) {
            throw new ai.foresignal.api.error.ConflictException(ErrorCodes.PLATFORM_DISABLED, "The social platform"
                    + " adapter '" + platformId + "' is disabled; enable it on /v1/social/platforms/" + platformId
                    + ".");
        }
    }

    /** Records that deletion sync ran for an adapter. */
    @Transactional
    public void deletionRan(String platformId, Instant at) {
        rows.findById(platformId).ifPresent(row -> {
            row.setDeletionLastRunAt(at);
            rows.save(row);
        });
    }
}
