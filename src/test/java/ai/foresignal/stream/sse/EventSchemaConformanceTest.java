package ai.foresignal.stream.sse;

import static ai.foresignal.stream.sse.SseFixtures.T;
import static ai.foresignal.stream.sse.SseFixtures.box;
import static ai.foresignal.stream.sse.SseFixtures.limits;
import static ai.foresignal.stream.sse.SseFixtures.touch;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.notify.NotificationJson;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.types.WebhookChannel;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertSnapshots;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.MatchRow;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.databind.JsonNode;

/**
 * Event payloads, as Foresignal's own serializers produce them, against the schemas the spec
 * declares for them (prompt 020).
 *
 * <p>SSE payloads come from the code paths that write them: {@link SseClient} for the control and
 * live-lane events, {@link SseHub#open} for {@code hello}, {@link RecordMapper#streamChange} for
 * {@code change}, {@link AlertMapper#event} for the alert lifecycle and {@link NotificationJson} for
 * {@code notification} and {@code system}. The webhook body is rendered by the {@code webhook}
 * channel type itself. Each instance is serialized with Jackson and validated with a JSON Schema
 * 2020-12 validator against the schema that {@code x-foresignal-sse-events} maps its event name to.
 *
 * <p>{@link #SSE_EVENTS} is the table of event names Foresignal emits. An event name the spec lists
 * for an implemented stream without an entry here fails, and so does an entry the spec does not
 * list: a new SSE event type cannot ship without a schema.
 */
class EventSchemaConformanceTest {

    private static final SpecSchemas SCHEMAS = new SpecSchemas();
    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final UUID RULE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** The implemented SSE operations whose event maps this test checks. */
    private static final List<String> STREAMS = List.of("streamChanges", "streamAlerts", "streamEvents");

    /** Every SSE event name Foresignal emits today, with the payloads its serializers produce. */
    static final Map<String, Supplier<List<JsonNode>>> SSE_EVENTS = Map.ofEntries(
            entry("hello", EventSchemaConformanceTest::hello),
            entry(ChangesTopic.EVENT, EventSchemaConformanceTest::changes),
            entry("live", () -> controlFrames("live")),
            entry("touch", () -> controlFrames("touch")),
            entry("resync", () -> controlFrames("resync")),
            entry("filter-applied", () -> controlFrames("filter-applied")),
            entry("heartbeat", () -> controlFrames("heartbeat")),
            entry("reset", EventSchemaConformanceTest::resets),
            entry("alert", () -> alertEvents("alert")),
            entry("alert.updated", () -> alertEvents("alert.updated")),
            entry("alert.resolved", () -> alertEvents("alert.resolved")),
            entry("alert.enriched", EventSchemaConformanceTest::enrichedAlerts),
            entry("notification", () -> List.of(toTree(NotificationJson.toMap(NotifyFixtures.message())),
                    toTree(NotificationJson.toMap(NotifyFixtures.update("alert.resolved"))))),
            entry("system", () -> List.of(toTree(NotificationJson.toMap(systemMessage())))),
            // 033: InboxTopic sends the device's badge as an id-less control event (checkpoint 041).
            entry("badge", () -> List.of(toTree(new ai.foresignal.notify.inbox.InboxService.Badge(3, 1, 42L).toMap()),
                    toTree(new ai.foresignal.notify.inbox.InboxService.Badge(0, 0, null).toMap()))),
            // 101: IntelTopic sends IntelTopic.payload(IntelEvent) on topic intel (checkpoint 124).
            entry("intel.event.created", () -> intelEvents(ai.foresignal.intel.IntelEvent.CREATED)),
            entry("intel.event.updated", () -> intelEvents(ai.foresignal.intel.IntelEvent.UPDATED)),
            entry("intel.event.resolved", () -> intelEvents(ai.foresignal.intel.IntelEvent.RESOLVED)),
            entry("intel.event.merged", () -> intelEvents(ai.foresignal.intel.IntelEvent.MERGED)),
            // 102: AnalystTopic sends {note} and {change, proposal} on topic analyst (checkpoint 124).
            entry(AnalystTopic.NOTE, EventSchemaConformanceTest::analystNotes),
            entry(AnalystTopic.PROPOSAL, EventSchemaConformanceTest::analystProposals),
            // 116: EvidenceTopic sends EvidenceEvent.payload() on topic evidence.
            entry(ai.foresignal.evidence.EvidenceEvent.CREATED,
                    () -> evidenceEvents(ai.foresignal.evidence.EvidenceEvent.CREATED)),
            entry(ai.foresignal.evidence.EvidenceEvent.MERGED,
                    () -> evidenceEvents(ai.foresignal.evidence.EvidenceEvent.MERGED)),
            entry(ai.foresignal.evidence.EvidenceEvent.FLAGGED,
                    () -> evidenceEvents(ai.foresignal.evidence.EvidenceEvent.FLAGGED)),
            // 118: SituationsTopic sends SituationEvent.payload() on topic situations.
            entry(ai.foresignal.situation.SituationEvent.CREATED,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.CREATED)),
            entry(ai.foresignal.situation.SituationEvent.UPDATED,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.UPDATED)),
            entry(ai.foresignal.situation.SituationEvent.ACTIVATED,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.ACTIVATED)),
            entry(ai.foresignal.situation.SituationEvent.ESCALATED,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.ESCALATED)),
            entry(ai.foresignal.situation.SituationEvent.DORMANT,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.DORMANT)),
            entry(ai.foresignal.situation.SituationEvent.CLOSED,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.CLOSED)),
            entry(ai.foresignal.situation.SituationEvent.MERGED,
                    () -> situationEvents(ai.foresignal.situation.SituationEvent.MERGED)),
            // 119: AnalyticsTopic sends FindingEvent.payload() on topic analytics.
            entry(ai.foresignal.analytics.finding.FindingEvent.CREATED,
                    () -> findingEvents(ai.foresignal.analytics.finding.FindingEvent.CREATED)),
            entry(ai.foresignal.analytics.finding.FindingEvent.UPDATED,
                    () -> findingEvents(ai.foresignal.analytics.finding.FindingEvent.UPDATED)),
            entry(ai.foresignal.analytics.finding.FindingEvent.DISMISSED,
                    () -> findingEvents(ai.foresignal.analytics.finding.FindingEvent.DISMISSED)),
            // 120: AnalyticsTopic sends precursor lifecycle events with the PrecursorEvent payload.
            entry(ai.foresignal.analytics.finding.FindingEvent.PRECURSOR_VALIDATED,
                    () -> precursorEvents(ai.foresignal.analytics.finding.FindingEvent.PRECURSOR_VALIDATED)),
            entry(ai.foresignal.analytics.finding.FindingEvent.PRECURSOR_STALE,
                    () -> precursorEvents(ai.foresignal.analytics.finding.FindingEvent.PRECURSOR_STALE)),
            entry(ai.foresignal.analytics.finding.FindingEvent.PRECURSOR_RETIRED,
                    () -> precursorEvents(ai.foresignal.analytics.finding.FindingEvent.PRECURSOR_RETIRED)),
            // 126: AnalyticsTopic sends the outlook and index summaries themselves (FindingEvent.payload()).
            entry(ai.foresignal.analytics.finding.FindingEvent.OUTLOOK_PUBLISHED,
                    EventSchemaConformanceTest::outlookPublishedEvents),
            entry(ai.foresignal.analytics.finding.FindingEvent.INDEX_CHANGED,
                    EventSchemaConformanceTest::indexChangedEvents),
            entry(ai.foresignal.analytics.finding.FindingEvent.INDEX_WEIGHTS_PROPOSED,
                    EventSchemaConformanceTest::indexWeightsProposedEvents),
            // 127: the projected-impact summaries (ImpactViews.event), sent as the payload itself.
            entry(ai.foresignal.analytics.finding.FindingEvent.IMPACT_PROJECTED,
                    () -> impactEvents(ai.foresignal.analytics.finding.FindingEvent.IMPACT_PROJECTED)),
            entry(ai.foresignal.analytics.finding.FindingEvent.IMPACT_SUPERSEDED,
                    () -> impactEvents(ai.foresignal.analytics.finding.FindingEvent.IMPACT_SUPERSEDED)),
            // 122: ForecastsTopic sends the forecast_log payloads written by ForecastEvents on topic forecasts.
            entry(ai.foresignal.forecast.ForecastEvents.OPENED,
                    () -> questionEvents(ai.foresignal.forecast.ForecastEvents.OPENED)),
            entry(ai.foresignal.forecast.ForecastEvents.CLOSED,
                    () -> questionEvents(ai.foresignal.forecast.ForecastEvents.CLOSED)),
            entry(ai.foresignal.forecast.ForecastEvents.RESOLVED,
                    () -> questionEvents(ai.foresignal.forecast.ForecastEvents.RESOLVED)),
            entry(ai.foresignal.forecast.ForecastEvents.ANNULLED,
                    () -> questionEvents(ai.foresignal.forecast.ForecastEvents.ANNULLED)),
            entry(ai.foresignal.forecast.ForecastEvents.SUBMITTED, EventSchemaConformanceTest::submittedEvents),
            // 125: HypothesesTopic sends the hypothesis_log payloads written by HypothesisEvents on topic hypotheses.
            entry(ai.foresignal.hypothesis.HypothesisEvents.SET_CREATED,
                    () -> hypothesisSetEvents("created", false)),
            entry(ai.foresignal.hypothesis.HypothesisEvents.SET_REVISED,
                    () -> hypothesisSetEvents("indicator-observed", false)),
            entry(ai.foresignal.hypothesis.HypothesisEvents.SET_BAND_CHANGED,
                    () -> hypothesisSetEvents("indicator-observed", true)),
            entry(ai.foresignal.hypothesis.HypothesisEvents.SET_RESOLVED, () -> hypothesisSetEvents(null, false)),
            entry(ai.foresignal.hypothesis.HypothesisEvents.INDICATOR_OBSERVED,
                    () -> indicatorEvents(ai.foresignal.hypothesis.HypothesisVocabulary.OBSERVED)),
            entry(ai.foresignal.hypothesis.HypothesisEvents.INDICATOR_ABSENT,
                    () -> indicatorEvents(ai.foresignal.hypothesis.HypothesisVocabulary.ABSENT)),
            entry(ai.foresignal.hypothesis.HypothesisEvents.ASSUMPTION_BROKEN,
                    EventSchemaConformanceTest::assumptionEvents),
            // 128: AssessmentsTopic sends the assessment_log payloads written by AssessmentEvents on topic assessments.
            entry(ai.foresignal.cycle.AssessmentEvents.PUBLISHED, EventSchemaConformanceTest::assessmentPublishedEvents),
            entry(ai.foresignal.cycle.AssessmentEvents.RUN_FAILED, EventSchemaConformanceTest::cycleRunFailedEvents),
            // 129: WorkspaceTopic sends id-less control events built by its payload helpers.
            entry(ai.foresignal.object.WorkspaceTopic.CONTEXT_EVENT, EventSchemaConformanceTest::workspaceContextEvents),
            entry(ai.foresignal.object.WorkspaceTopic.PRESENCE_EVENT,
                    EventSchemaConformanceTest::workspacePresenceEvents),
            // 130: MeTopic sends the payloads MeEvents builds, stored with the personal inbox item.
            entry(ai.foresignal.collab.MeEvents.SHARE_RECEIVED, EventSchemaConformanceTest::shareReceivedEvents),
            entry(ai.foresignal.collab.MeEvents.MENTION, () -> commentEvents("mention")),
            entry(ai.foresignal.collab.MeEvents.COMMENT_REPLY, () -> commentEvents("reply")),
            entry(ai.foresignal.collab.MeEvents.SAVED_SEARCH_MATCHES,
                    EventSchemaConformanceTest::savedSearchMatchesEvents),
            // 132: MeTopic sends the payloads MeEvents.report and MeEvents.packageReceived build.
            entry(ai.foresignal.collab.MeEvents.REPORT_RECEIVED, () -> reportEvents(false)),
            entry(ai.foresignal.collab.MeEvents.REPORT_RETRACTED, () -> reportEvents(true)),
            entry(ai.foresignal.collab.MeEvents.PACKAGE_RECEIVED, EventSchemaConformanceTest::packageReceivedEvents),
            // 134: MeTopic sends the payloads MeEvents.chronicleLinkFound and MeEvents.chronicleAccess build.
            entry(ai.foresignal.collab.MeEvents.CHRONICLE_LINK_FOUND, EventSchemaConformanceTest::linkFoundEvents),
            entry(ai.foresignal.collab.MeEvents.CHRONICLE_ACCESS_REQUESTED, () -> accessEvents("pending")),
            entry(ai.foresignal.collab.MeEvents.CHRONICLE_ACCESS_GRANTED, () -> accessEvents("granted")),
            entry(ai.foresignal.collab.MeEvents.CHRONICLE_ACCESS_DECLINED, () -> accessEvents("declined")),
            // 131: ChronicleTopic sends ChronicleThreadService.item for thread items and the status snapshot.
            entry(ai.foresignal.chronicle.ChronicleTopic.ITEM_EVENT, EventSchemaConformanceTest::chronicleItemEvents),
            entry(ai.foresignal.chronicle.ChronicleTopic.STATUS_EVENT,
                    EventSchemaConformanceTest::chronicleStatusEvents),
            // 135: SocialTopic sends SocialEvents.velocityBody (from the finding) and the platformStateBody control event.
            entry(ai.foresignal.social.SocialEvents.VELOCITY, EventSchemaConformanceTest::socialVelocityEvents),
            entry(ai.foresignal.social.SocialEvents.PLATFORM_STATE,
                    EventSchemaConformanceTest::socialPlatformStateEvents),
            // 136: SocialTopic sends the bodies NarrativeService, MediaReuseDetector and CoordinationRunner build.
            entry(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_EMERGED,
                    () -> narrativeEvents(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_EMERGED)),
            entry(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_UPDATED,
                    () -> narrativeEvents(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_UPDATED)),
            entry(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_CLAIM,
                    EventSchemaConformanceTest::claimExtractedEvents),
            entry(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_MEDIA_REUSE,
                    EventSchemaConformanceTest::mediaReuseEvents),
            entry(ai.foresignal.social.narrative.NarrativeVocabulary.EVENT_COORDINATION,
                    EventSchemaConformanceTest::coordinationEvents),
            // 137: SocialTopic sends the bodies ComparisonService, SocialCueService and AttentionGapDetector build.
            entry(ai.foresignal.social.correlation.CorrelationVocabulary.EVENT_COMPARISON,
                    EventSchemaConformanceTest::comparisonEvents),
            entry(ai.foresignal.social.correlation.CorrelationVocabulary.EVENT_CUE, EventSchemaConformanceTest::cueEvents),
            entry(ai.foresignal.social.correlation.CorrelationVocabulary.EVENT_GAP,
                    EventSchemaConformanceTest::attentionGapEvents),
            // 138: ExternalTopic sends ExternalEvent.payload() on topic external.
            entry(ai.foresignal.inbound.edp.ExternalEvent.SOURCE_STATE, EventSchemaConformanceTest::externalStateEvents),
            entry(ai.foresignal.inbound.edp.ExternalEvent.BINDING_DISABLED,
                    EventSchemaConformanceTest::bindingDisabledEvents));

    /** {@code external.source.state} as {@code ExternalEvent.sourceState} builds it (prompt 138). */
    static List<JsonNode> externalStateEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-18T06:00:00Z");
        List<JsonNode> out = new ArrayList<>();
        for (String change : List.of("registered", "approved", "suspended", "manifest-changed")) {
            out.add(toTree(ai.foresignal.inbound.edp.ExternalEvent.sourceState(
                    ai.foresignal.inbound.edp.ExternalSourceChangedEvent.of("ext-hormuz-transits", "active", change, at))
                    .payload()));
        }
        out.add(toTree(ai.foresignal.inbound.edp.ExternalEvent.sourceState(
                ai.foresignal.inbound.edp.ExternalSourceChangedEvent.of("ext-hormuz-transits", null, "deleted", at))
                .payload()));
        return out;
    }

    /** {@code external.binding.disabled} as {@code ExternalEvent.bindingDisabled} builds it (prompt 138). */
    static List<JsonNode> bindingDisabledEvents() {
        return List.of(toTree(ai.foresignal.inbound.edp.ExternalEvent.bindingDisabled("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                "ext-hormuz-transits", "breaking manifest update: stream transits kind changed",
                java.time.Instant.parse("2026-10-18T06:00:00Z")).payload()));
    }

    /** A comparison row (prompt 137). */
    static ai.foresignal.social.correlation.ComparisonStore.Comparison comparisonRow(String status) {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        Map<String, Object> place = new java.util.LinkedHashMap<>();
        place.put("ref", null);
        place.put("name", "Aden");
        place.put("geom", Map.of("type", "Point", "coordinates", List.of(45.03, 12.78)));
        place.put("precision", "locality");
        place.put("confidence", 0.9);
        place.put("regionCode", "YE");
        return new ai.foresignal.social.correlation.ComparisonStore.Comparison("01JA8Q3V7S2K9W4T6Y1B5N0M3H",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3D", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "explosion", "conflict", place, 45.03,
                12.78, "YE", at.minusSeconds(21_600), at.plusSeconds(86_400), status, List.of(), Map.of("p", 0.9, "lo",
                0.8, "hi", 0.95, "methods", List.of("declared"), "ok", true), Map.of("socialFirst", at.toString()),
                null, List.of(), "organic", null, null, List.of("bluesky"), 2, null, 2, at.plusSeconds(172_800), null,
                at, at);
    }

    /** {@code comparison.updated} as {@code ComparisonService.comparisonEvent} builds it. */
    static List<JsonNode> comparisonEvents() {
        return List.of(toTree(ai.foresignal.social.correlation.ComparisonService.comparisonEvent(
                comparisonRow("corroborated"), "pending")), toTree(
                ai.foresignal.social.correlation.ComparisonService.comparisonEvent(comparisonRow("pending"), null)));
    }

    /** {@code cue.completed} as {@code SocialCueService.cueEvent} builds it. */
    static List<JsonNode> cueEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        Map<String, Object> query = new java.util.LinkedHashMap<>();
        query.put("platform", "bluesky-search");
        query.put("query", "\"Aden\" AND \"explosion\"");
        query.put("language", "en");
        query.put("estimatedCostUsd", 0.0);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("platform", "bluesky-search");
        result.put("posts", 4);
        result.put("costUsd", 0.0);
        result.put("error", null);
        ai.foresignal.social.correlation.ComparisonStore.Cue cue = new ai.foresignal.social.correlation.ComparisonStore
                .Cue("01JA8Q3V7S2K9W4T6Y1B5N0M3J", "finding", "finding:01JA8Q3V7S2K9W4T6Y1B5N0M3K", "platform-apis",
                List.of(query), List.of("bluesky-search"), at.minusSeconds(21_600), at.plusSeconds(86_400), "done",
                null, List.of(result), List.of("01JA8Q3V7S2K9W4T6Y1B5N0M3C"), java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ONE, Map.of(), "system", at, at, at.plusSeconds(60), null);
        return List.of(toTree(ai.foresignal.social.correlation.SocialCueService.cueEvent(cue)));
    }

    /** {@code attention.gap} as {@code AttentionGapDetector.gapEvent} builds it. */
    static List<JsonNode> attentionGapEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        ai.foresignal.social.correlation.AttentionGapDetector.Candidate c =
                new ai.foresignal.social.correlation.AttentionGapDetector.Candidate("finding:01JA8Q3V7S2K9W4T6Y1B5N0M3K",
                        "Outage change point", "finding:outage", "significant", "YE", at.minusSeconds(21_600));
        return List.of(toTree(ai.foresignal.social.correlation.AttentionGapDetector.gapEvent(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3L", c, 120.0,
                new ai.foresignal.social.correlation.AttentionGapDetector.Attention(3, 40, List.of("bluesky")), at)));
    }

    /** A narrative row as the narrative store reads it (prompt 136). */
    static ai.foresignal.social.narrative.NarrativeStore.Narrative narrativeRow() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        Map<String, Object> platform = new java.util.LinkedHashMap<>();
        platform.put("platform", "bluesky");
        platform.put("posts", 3);
        platform.put("uniqueAuthors", 2);
        platform.put("firstObservedAt", at.toString());
        platform.put("earliestPostRef", "record:bluesky-jetstream/bluesky%3Aa1");
        return new ai.foresignal.social.narrative.NarrativeStore.Narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                "Reported explosion and fire in Aden", "claims", null, null, null, null, 0, List.of("red-sea"),
                List.of(), List.of(), List.of("YE"), List.of("en"), List.of(platform), at, at.plusSeconds(120), 3, 2,
                2, 1.0, null, List.of(), List.of(), List.of(), "unassessed", "emerging", null, null, null, "CLEAR",
                null, false, null, 0, at, at);
    }

    /** {@code narrative.emerged} and {@code narrative.updated} as {@code NarrativeService.narrativeBody} builds them. */
    static List<JsonNode> narrativeEvents(String event) {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:05:00Z");
        Map<String, Object> summary = ai.foresignal.social.narrative.NarrativeViews.summary(narrativeRow());
        return List.of(toTree(ai.foresignal.social.narrative.NarrativeService.narrativeBody(event, summary,
                event.endsWith("updated") ? List.of("state", "postCount") : List.of(), at)));
    }

    /** {@code claim.extracted} as {@code NarrativeService.claimBody} builds it. */
    static List<JsonNode> claimExtractedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:05:00Z");
        Map<String, Object> place = new java.util.LinkedHashMap<>();
        place.put("ref", null);
        place.put("name", "Aden");
        place.put("geom", Map.of("type", "Point", "coordinates", List.of(45.03, 12.78)));
        place.put("precision", "locality");
        place.put("confidence", 0.9);
        place.put("regionCode", "YE");
        ai.foresignal.social.narrative.NarrativeStore.Claim claim =
                new ai.foresignal.social.narrative.NarrativeStore.Claim("01JA8Q3V7S2K9W4T6Y1B5N0M3D",
                        "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "explosion|asserted|aden", "explosion", "conflict", "asserted",
                        false, place, 45.03, 12.78, "YE", List.of(), Map.of("from", at.toString(), "precision",
                        "minute"), null, 3, 2, at, "deterministic", "01JA8Q3V7S2K9W4T6Y1B5N0M3E", 0.8, "unassessed",
                        null, "account:bluesky:k", "F", 6, "CLEAR", at, at);
        return List.of(toTree(ai.foresignal.social.narrative.NarrativeService.claimBody(
                ai.foresignal.social.narrative.NarrativeViews.claim(claim), at)));
    }

    /** {@code media.reuse} as {@code MediaReuseDetector.body} builds it. */
    static List<JsonNode> mediaReuseEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:05:00Z");
        ai.foresignal.social.narrative.NarrativeStore.Fingerprint fp =
                new ai.foresignal.social.narrative.NarrativeStore.Fingerprint("01JA8Q3V7S2K9W4T6Y1B5N0M3F",
                        "f8f8f0cee0f4a84f06370a22038f63f0b36e2ed596621e1d33e6b39c4e9c9b22", 100,
                        at.minusSeconds(86_400L * 1000), "record:x/x%3A1", "x:1", 2, List.of("x"), List.of(), null,
                        List.of(), at, at);
        return List.of(toTree(ai.foresignal.social.narrative.MediaReuseDetector.body("01JA8Q3V7S2K9W4T6Y1B5N0M3G",
                fp, narrativeRow(), new ai.foresignal.social.narrative.MediaReuseDetector.Reuse("earlier", null), at)));
    }

    /** {@code coordination.detected} as {@code CoordinationRunner.body} builds it. */
    static List<JsonNode> coordinationEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:05:00Z");
        ai.foresignal.social.narrative.CoordinationDetector.Indicator sig =
                new ai.foresignal.social.narrative.CoordinationDetector.Indicator(0.5, 0.1, 1e-9, 1e-7, 80, true);
        ai.foresignal.social.narrative.CoordinationDetector.Indicator none =
                ai.foresignal.social.narrative.CoordinationDetector.Indicator.unavailable(80);
        ai.foresignal.social.narrative.CoordinationDetector.Indicators ind =
                new ai.foresignal.social.narrative.CoordinationDetector.Indicators("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                        at.minusSeconds(86_400), at, sig, sig, none, none);
        return List.of(toTree(ai.foresignal.social.narrative.CoordinationRunner.body("01JA8Q3V7S2K9W4T6Y1B5N0M3G", ind,
                at)));
    }

    /** {@code social.velocity} payloads as {@code VelocityDetector} and {@code SocialEvents} build them (prompt 135). */
    static List<JsonNode> socialVelocityEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        ai.foresignal.social.SocialBaseline.Point point = new ai.foresignal.social.SocialBaseline.Point(at, 60, 10.0,
                1.0, 6.0, 15.8, 8, true);
        ai.foresignal.social.VelocityDetector.Candidate c = new ai.foresignal.social.VelocityDetector.Candidate(
                "topic:red-sea", "bluesky", point, 25, "significant");
        return List.of(toTree(ai.foresignal.social.SocialEvents.velocityBody(
                ai.foresignal.social.VelocityDetector.eventPayload("01JA8Q3V7S2K9W4T6Y1B5N0M3C", c, at))));
    }

    /** {@code social.platform.state} payloads as {@code SocialEvents.platformStateBody} builds them (prompt 135). */
    static List<JsonNode> socialPlatformStateEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        Map<String, Object> spend = new java.util.LinkedHashMap<>();
        spend.put("currency", "USD");
        spend.put("spentThisMonth", 25);
        spend.put("monthlyCapUsd", 25);
        spend.put("projectedThisMonth", null);
        spend.put("standingCountsEstimateMonthly", 10.95);
        spend.put("pricesAsOf", "2026-10-17");
        spend.put("refusedThisMonth", 3);
        return List.of(
                toTree(ai.foresignal.social.SocialEvents.platformStateBody("x", "budget-exhausted", "enabled",
                        "the monthly cap of $25 is spent", spend, at)),
                toTree(ai.foresignal.social.SocialEvents.platformStateBody("bluesky-jetstream", "enabled", null, null,
                        null, at)));
    }

    /** A {@code PrincipalSummary} as {@code PrincipalViews.summary} writes it (prompt 130). */
    static Map<String, Object> principalSummary() {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("id", "01JA8Q3V7S2K9W4T6Y1B5N0M3D");
        out.put("handle", "jsmith");
        out.put("displayName", "Jo Smith");
        out.put("kind", "person");
        out.put("avatarMediaId", null);
        return out;
    }

    /** {@code share.received} payloads as {@code MeEvents.shareReceived} writes them (prompt 130). */
    static List<JsonNode> shareReceivedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        return List.of(
                toTree(ai.foresignal.collab.MeEvents.shareReceived("investigation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", null,
                        "commenter", principalSummary(), "Please review.", at)),
                toTree(ai.foresignal.collab.MeEvents.shareReceived("saved-search:01JA8Q3V7S2K9W4T6Y1B5N0M3E", null,
                        "viewer", principalSummary(), null, at)));
    }

    /** {@code mention} and {@code comment.reply} payloads as {@code MeEvents.comment} writes them (prompt 130). */
    static List<JsonNode> commentEvents(String reason) {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        return List.of(
                toTree(ai.foresignal.collab.MeEvents.comment(reason, "01JA8Q3V7S2K9W4T6Y1B5N0M3F", null,
                        "situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", principalSummary(), "@jsmith please check", at)),
                toTree(ai.foresignal.collab.MeEvents.comment(reason, "01JA8Q3V7S2K9W4T6Y1B5N0M3G",
                        "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "investigation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", principalSummary(),
                        "x".repeat(400), at)));
    }

    /** {@code saved-search.matches} payloads as {@code MeEvents.savedSearchMatches} writes them (prompt 130). */
    static List<JsonNode> savedSearchMatchesEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        List<String> refs = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            refs.add("record:test-src/e" + i);
        }
        return List.of(
                toTree(ai.foresignal.collab.MeEvents.savedSearchMatches("01JA8Q3V7S2K9W4T6Y1B5N0M3H", "Convoys", 25,
                        refs, at)),
                toTree(ai.foresignal.collab.MeEvents.savedSearchMatches("01JA8Q3V7S2K9W4T6Y1B5N0M3H", "Convoys", 1,
                        List.of("evidence-item:01JA8Q3V7S2K9W4T6Y1B5N0M3J"), at)));
    }

    /** {@code report.received} and {@code report.retracted} payloads as {@code MeEvents.report} writes them (132). */
    static List<JsonNode> reportEvents(boolean retracted) {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        return List.of(
                toTree(ai.foresignal.collab.MeEvents.report("report:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "Red Sea weekly",
                        "AMBER", 2, principalSummary(), retracted ? null : "Please read.",
                        retracted ? "Superseded" : null, at)),
                toTree(ai.foresignal.collab.MeEvents.report("report:01JA8Q3V7S2K9W4T6Y1B5N0M3D", "Port note", "RED",
                        null, null, null, null, at)));
    }

    /** {@code package.received} payloads as {@code MeEvents.packageReceived} writes them (prompt 132). */
    static List<JsonNode> packageReceivedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        return List.of(
                toTree(ai.foresignal.collab.MeEvents.packageReceived("01JA8Q3V7S2K9W4T6Y1B5N0M3E",
                        "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "report", "fs-eu-1", "Red Sea weekly", "AMBER", "valid", at)),
                toTree(ai.foresignal.collab.MeEvents.packageReceived("01JA8Q3V7S2K9W4T6Y1B5N0M3G",
                        "01JA8Q3V7S2K9W4T6Y1B5N0M3H", "chronicle-extract", "fs-eu-2", "CHR-2026-0001 Gulf", "GREEN",
                        "valid", at)));
    }

    /** {@code chronicle.link.found} payloads with an opaque and a full link, as prompt 134's notices build them. */
    static List<JsonNode> linkFoundEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        Map<String, Object> opaque = ai.foresignal.chronicle.network.Disclosure.opaque("01JA8Q3V7S2K9W4T6Y1B5N0M3K",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "medium", List.of("evidence", "area-time"), "partial", false, null, null,
                null);
        Map<String, Object> full = new java.util.LinkedHashMap<>();
        full.put("linkId", "01JA8Q3V7S2K9W4T6Y1B5N0M3M");
        full.put("disclosure", "full");
        full.put("a", "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        full.put("b", "01JA8Q3V7S2K9W4T6Y1B5N0M3D");
        full.put("other", null);
        full.put("strength", "high");
        full.put("dimensions", List.of("evidence"));
        full.put("scores", Map.of("evidence", Map.of("jaccard", 0.5, "containment", 0.8, "sharedEstimate", 60.0)));
        full.put("temporal", "concurrent");
        full.put("timeOverlap", "full");
        full.put("sharedSituations", List.of());
        full.put("sharedCanonicalEvents", List.of("canonical-event:01JA8Q3V7S2K9W4T6Y1B5N0M3E"));
        full.put("sharedEntities", List.of());
        full.put("sharedKeyCount", 60);
        full.put("status", "active");
        full.put("firstFound", at.toString());
        full.put("lastUpdated", at.toString());
        return List.of(
                toTree(ai.foresignal.collab.MeEvents.chronicleLinkFound("01JA8Q3V7S2K9W4T6Y1B5N0M3C", opaque, at)),
                toTree(ai.foresignal.collab.MeEvents.chronicleLinkFound("01JA8Q3V7S2K9W4T6Y1B5N0M3C", full, at)));
    }

    /** {@code chronicle.access.*} payloads as {@code MeEvents.chronicleAccess} writes them (prompt 134). */
    static List<JsonNode> accessEvents(String status) {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        boolean requested = "pending".equals(status);
        return List.of(toTree(ai.foresignal.collab.MeEvents.chronicleAccess("01JA8Q3V7S2K9W4T6Y1B5N0M3R",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3K", status, requested ? "jsmith" : null,
                requested ? "Working the same front" : null, requested ? "01JA8Q3V7S2K9W4T6Y1B5N0M3C" : null, at)));
    }

    /** A saved thread item with an id, as the SSE topic reads it (prompt 131). */
    static ai.foresignal.chronicle.ChronicleEventEntity chronicleItem(String type, long id) {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        ai.foresignal.chronicle.ChronicleEventEntity row = new ai.foresignal.chronicle.ChronicleEventEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", type, at, "Chronicle item " + type, at);
        org.springframework.test.util.ReflectionTestUtils.setField(row, "id", id);
        return row;
    }

    /** {@code chronicle.item} payloads as {@code ChronicleThreadService.item} writes them (prompt 131). */
    static List<JsonNode> chronicleItemEvents() {
        ai.foresignal.chronicle.ChronicleEventEntity first = chronicleItem("first-appearance", 7);
        first.setSeq(42L);
        first.setLayerId("01JA8Q3V7S2K9W4T6Y1B5N0M3D");
        first.setRef("record:acled/YEM123@42");
        first.setSnapshot(Map.of("sourceId", "acled", "externalId", "YEM123"));
        ai.foresignal.chronicle.ChronicleEventEntity gap = chronicleItem("capture-gap", 8);
        gap.setSummary("global retention removed versions");
        gap.setGap(Map.of("from", "2026-10-16T00:00:00Z", "to", "2026-10-17T00:00:00Z", "reason", "clamp released"));
        ai.foresignal.chronicle.ChronicleEventEntity backfill = chronicleItem("backfill", 9);
        backfill.setBackfilled(true);
        return List.of(toTree(ai.foresignal.chronicle.ChronicleTopic.frameData(first)),
                toTree(ai.foresignal.chronicle.ChronicleTopic.frameData(gap)),
                toTree(ai.foresignal.chronicle.ChronicleTopic.frameData(backfill)));
    }

    /** {@code chronicle.status} payloads as {@code ChronicleThreadService.statusChange} writes them (prompt 131). */
    static List<JsonNode> chronicleStatusEvents() {
        ai.foresignal.chronicle.ChronicleEventEntity activated = chronicleItem("status-change", 10);
        Map<String, Object> event = new java.util.LinkedHashMap<>();
        event.put("chronicleId", "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        event.put("status", "active");
        event.put("previousStatus", "proposed");
        event.put("capState", "ok");
        event.put("bytesUsed", 0L);
        event.put("reason", "activated");
        event.put("at", "2026-10-17T06:00:00Z");
        activated.setSnapshot(event);
        ai.foresignal.chronicle.ChronicleEventEntity first = chronicleItem("status-change", 11);
        Map<String, Object> proposed = new java.util.LinkedHashMap<>(event);
        proposed.put("status", "proposed");
        proposed.put("previousStatus", null);
        first.setSnapshot(proposed);
        return List.of(toTree(ai.foresignal.chronicle.ChronicleTopic.frameData(activated)),
                toTree(ai.foresignal.chronicle.ChronicleTopic.frameData(first)));
    }

    /** {@code workspace.context} payloads as {@code WorkspaceTopic.contextPayload} writes them (prompt 129). */
    static List<JsonNode> workspaceContextEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        Map<String, Object> patch = new java.util.LinkedHashMap<>();
        patch.put("focusRef", "situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        patch.put("asOf", null);
        return List.of(
                toTree(ai.foresignal.object.WorkspaceTopic.contextPayload("01JA8Q3V7S2K9W4T6Y1B5N0M3C", 2, patch,
                        "globe-1", "01JA8Q3V7S2K9W4T6Y1B5N0M3D", at)),
                toTree(ai.foresignal.object.WorkspaceTopic.contextPayload("01JA8Q3V7S2K9W4T6Y1B5N0M3C", 3, Map.of(),
                        null, null, at)));
    }

    /** {@code workspace.presence} payloads as {@code WorkspaceTopic.presencePayload} writes them (prompt 129). */
    static List<JsonNode> workspacePresenceEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        ai.foresignal.object.WorkspaceFilter globe = new ai.foresignal.object.WorkspaceFilter(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "globe-1", "globe", "01JA8Q3V7S2K9W4T6Y1B5N0M3D", at);
        return List.of(
                toTree(ai.foresignal.object.WorkspaceTopic.presencePayload("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                        List.of(globe), "joined", "globe-1", at)),
                toTree(ai.foresignal.object.WorkspaceTopic.presencePayload("01JA8Q3V7S2K9W4T6Y1B5N0M3C", List.of(),
                        "left", "globe-1", at)),
                toTree(ai.foresignal.object.WorkspaceTopic.presencePayload("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                        List.of(globe), null, null, at)));
    }

    /** {@code assessment.published} payloads as {@code AssessmentEvents.publishedPayload} writes them (prompt 128). */
    static List<JsonNode> assessmentPublishedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        List<JsonNode> out = new ArrayList<>();
        ai.foresignal.cycle.AssessmentEntity a = new ai.foresignal.cycle.AssessmentEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                "weekly", at.minusSeconds(604_800), at, at);
        a.setCycleId("01JA8Q3V7S2K9W4T6Y1B5N0M3D");
        a.setStatus("ok");
        a.setHeadline("A coalition response within 72 hours is likely.");
        out.add(toTree(ai.foresignal.cycle.AssessmentEvents.publishedPayload(a, at)));
        ai.foresignal.cycle.AssessmentEntity onDemand = new ai.foresignal.cycle.AssessmentEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3E", "on-demand", at.minusSeconds(3600), at, at);
        onDemand.setSituationId("01JA8Q3V7S2K9W4T6Y1B5N0M3F");
        onDemand.setPrivateAssessment(true);
        out.add(toTree(ai.foresignal.cycle.AssessmentEvents.publishedPayload(onDemand, at)));
        return out;
    }

    /** {@code cycle.run_failed} payloads as {@code AssessmentEvents.failedPayload} writes them (prompt 128). */
    static List<JsonNode> cycleRunFailedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T06:00:00Z");
        ai.foresignal.cycle.CycleRunEntity run = new ai.foresignal.cycle.CycleRunEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3D", null, "daily", "schedule", at.minusSeconds(86_400), at, at);
        ai.foresignal.cycle.CycleRunEntity situation = new ai.foresignal.cycle.CycleRunEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3E", null, "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "on-demand", "situation",
                at.minusSeconds(3600), at, at);
        return List.of(toTree(ai.foresignal.cycle.AssessmentEvents.failedPayload(run, "the cycle was deleted", at)),
                toTree(ai.foresignal.cycle.AssessmentEvents.failedPayload(situation, null, at)));
    }

    /** {@code set.*} payloads as {@code HypothesisEvents.setPayload} writes them (prompt 125). */
    static List<JsonNode> hypothesisSetEvents(String cause, boolean bandChange) {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.hypothesis.HypothesisSetEntity set = new ai.foresignal.hypothesis.HypothesisSetEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", at);
        set.setQuestion("Saudi or coalition kinetic response within 7 days?");
        set.setKind("exclusive");
        set.setCadence("daily");
        set.setOrigin("operator");
        List<ai.foresignal.hypothesis.HypothesisEntity> hyps = new ArrayList<>();
        String[] ids = {"01JA8Q3V7S2K9W4T6Y1B5N0M3D", "01JA8Q3V7S2K9W4T6Y1B5N0M3E"};
        double[] p = {0.49, 0.51};
        for (int i = 0; i < 2; i++) {
            ai.foresignal.hypothesis.HypothesisEntity h = new ai.foresignal.hypothesis.HypothesisEntity(ids[i],
                    set.getId(), at);
            h.setLabel(i == 0 ? "within 72 h" : "other");
            h.setProbability(p[i]);
            h.setConfidence("moderate");
            hyps.add(h);
        }
        List<Map<String, Object>> bands = bandChange ? List.of(Map.of("hypothesisId", ids[0], "label", "within 72 h",
                "fromTerm", "unlikely", "toTerm", "roughly even chance", "probability", 0.49)) : List.of();
        List<JsonNode> out = new ArrayList<>();
        out.add(toTree(ai.foresignal.hypothesis.HypothesisEvents.setPayload(set, hyps,
                cause == null ? null : "01JA8Q3V7S2K9W4T6Y1B5N0M3F", cause, bands, at)));
        set.setPrivateSet(true);
        out.add(toTree(ai.foresignal.hypothesis.HypothesisEvents.setPayload(set, hyps, null, cause, bands, at)));
        return out;
    }

    /** {@code indicator.*} payloads as {@code HypothesisEvents.indicatorPayload} writes them (prompt 125). */
    static List<JsonNode> indicatorEvents(String state) {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.hypothesis.IndicatorEntity i = new ai.foresignal.hypothesis.IndicatorEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3G", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", at);
        i.setDescription("Airspace restrictions over southern Saudi Arabia");
        i.setSignalFamily("aviation-ops");
        i.setState(state, at);
        if ("observed".equals(state)) {
            i.setObservedEvidence(Map.of("source", "alert", "alertId", 12, "ref", "record:faa-tfr/abc@42"));
        }
        return List.of(toTree(ai.foresignal.hypothesis.HypothesisEvents.indicatorPayload(i, "pending",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3F", false, at)),
                toTree(ai.foresignal.hypothesis.HypothesisEvents.indicatorPayload(i, null, null, true, at)));
    }

    /** {@code assumption.broken} payloads as {@code HypothesisEvents.assumptionPayload} writes them (prompt 125). */
    static List<JsonNode> assumptionEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.hypothesis.KeyAssumptionEntity a = new ai.foresignal.hypothesis.KeyAssumptionEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3H", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", at);
        a.setText("The coalition can strike at short notice.");
        a.setLinchpin(true);
        a.setStatus("broken");
        return List.of(toTree(ai.foresignal.hypothesis.HypothesisEvents.assumptionPayload(a, "solid", false, at)),
                toTree(ai.foresignal.hypothesis.HypothesisEvents.assumptionPayload(a, null, true, at)));
    }

    /** {@code question.*} payloads as {@code ForecastEvents.questionPayload} writes them (prompt 122). */
    static List<JsonNode> questionEvents(String type) {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        List<JsonNode> out = new java.util.ArrayList<>();
        for (String questionType : List.of("binary", "numeric")) {
            ai.foresignal.forecast.ForecastQuestionEntity q = new ai.foresignal.forecast.ForecastQuestionEntity(
                    "01JA8Q3V7S2K9W4T6Y1B5N0M3C", at);
            q.setTitle("Will battles be recorded in Yemen in March?");
            q.setType(questionType);
            q.setResolutionCriteria("Yes if recorded.");
            q.setResolver(Map.of("type", "manual", "version", "1.0.0"));
            q.setOpensAt(at);
            q.setClosesAt(at.plusSeconds(86_400));
            q.setResolveBy(at.plusSeconds(86_400 * 30));
            q.setCategory("conflict");
            q.setTags(List.of("yemen"));
            q.setOrigin("agent");
            q.setSituationId("01JA8Q3V7S2K9W4T6Y1B5N0M3D");
            if ("binary".equals(questionType)) {
                q.setEnsembleProbability(0.62);
                q.setResolution(Map.of("outcome", "yes", "resolvedAt", at.toString(), "resolvedBy", "operator",
                        "evidence", List.of("alert:1")));
            } else {
                q.setResolution(Map.of("value", 12.5, "resolvedAt", at.toString(), "resolvedBy", "operator",
                        "evidence", List.of("series:a/b")));
            }
            out.add(toTree(ai.foresignal.forecast.ForecastEvents.questionPayload(type, q, 3, at)));
        }
        return out;
    }

    /** {@code forecast.submitted} payloads as {@code ForecastEvents.submittedPayload} writes them (prompt 122). */
    static List<JsonNode> submittedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.forecast.ForecastEntity binary = new ai.foresignal.forecast.ForecastEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3E", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "operator:ada", "operator", at, 42);
        binary.setProbability(0.7);
        ai.foresignal.forecast.ForecastEntity numeric = new ai.foresignal.forecast.ForecastEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "climatology", "climatology", at, 42);
        numeric.setQuantiles(Map.of("p05", 1.0, "p25", 2.0, "p50", 3.0, "p75", 4.0, "p95", 5.0));
        return List.of(toTree(ai.foresignal.forecast.ForecastEvents.submittedPayload(binary, false)),
                toTree(ai.foresignal.forecast.ForecastEvents.submittedPayload(numeric, true)));
    }

    /** {@code situation.*} payloads as {@code SituationEvent.payload()} writes them (prompt 118), located and not. */
    static List<JsonNode> situationEvents(String type) {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.situation.SituationEntity located = new ai.foresignal.situation.SituationEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "Red Sea shipping disruption", "maritime", at);
        located.setRegionCodes(List.of("YE", "YE-HD"));
        located.setGeom(ai.foresignal.situation.SituationGeometry.of(List.of(GEOMETRY.createPoint(
                new org.locationtech.jts.geom.Coordinate(43.0, 14.0)))));
        located.setCentroid(ai.foresignal.situation.SituationGeometry.centroid(located.getGeom()));
        located.setMemberCount(3);
        located.setSignalFamilies(List.of("ais-behaviour", "gnss-interference"));
        located.setCorroborationShare(0.5);
        located.setSummary("3 signals from 2 families across YE since 2026-03-01 12:00 UTC. Last activity just now.");
        located.setCreatedAt(at);
        located.setUpdatedAt(at);
        ai.foresignal.situation.SituationEntity bare = new ai.foresignal.situation.SituationEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3D", "Unlocated", "mixed", at);
        bare.setMergedInto("01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        bare.setCreatedAt(at);
        bare.setUpdatedAt(at);
        List<JsonNode> out = new java.util.ArrayList<>();
        for (ai.foresignal.situation.SituationEntity situation : List.of(located, bare)) {
            ai.foresignal.situation.SituationEvent event = new ai.foresignal.situation.SituationEvent(7, type,
                    situation.getId(), situation.getVersion(), ai.foresignal.situation.SituationMapper.summary(situation,
                    at), "emerging", "info", situation.getMergedInto(), at);
            out.add(toTree(event.payload()));
        }
        return out;
    }

    /** {@code finding.*} payloads as {@code FindingEvent.payload()} writes them (prompt 119), linked and not. */
    static List<JsonNode> findingEvents(String type) {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.analytics.finding.AnalyticFindingEntity f = new ai.foresignal.analytics.finding
                .AnalyticFindingEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "change-point", "Change point: transits down",
                        "significant", at, at);
        f.setRegionCodes(List.of("YE"));
        List<JsonNode> out = new java.util.ArrayList<>();
        for (String situation : java.util.Arrays.asList("01JA8Q3V7S2K9W4T6Y1B5N0M3D", null)) {
            out.add(toTree(new ai.foresignal.analytics.finding.FindingEvent(5, type, f.getId(),
                    ai.foresignal.analytics.finding.FindingViews.summary(f, situation), at).payload()));
        }
        return out;
    }

    /** {@code outlook.published} payloads as {@code FindingEvent.payload()} sends them (prompt 126), open and resolved. */
    static List<JsonNode> outlookPublishedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-19T06:00:00Z");
        ai.foresignal.outlook.regional.RegionalOutlookEntity o = new ai.foresignal.outlook.regional
                .RegionalOutlookEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "YE", "country", "conflict:battles", "conflict",
                        at, "regional-outlook 1.0.0", 42L);
        List<java.util.Map<String, Object>> windows = new java.util.ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            java.util.Map<String, Object> w = new java.util.LinkedHashMap<>();
            w.put("index", i);
            w.put("windowStart", java.time.LocalDate.of(2026, 10, 26).plusWeeks(4L * (i - 1)).toString());
            w.put("windowEnd", java.time.LocalDate.of(2026, 11, 22).plusWeeks(4L * (i - 1)).toString());
            w.put("quantiles", java.util.Map.of("p05", 2.0, "p10", 3.0, "p25", 5.0, "p50", 8.0, "p75", 12.0, "p90",
                    16.0, "p95", 20.0));
            w.put("tails", java.util.Map.of("p01", 1.0, "p99", 28.0));
            w.put("questionId", i == 1 ? "01JA8Q3V7S2K9W4T6Y1B5N0M3D" : null);
            w.put("status", i == 1 ? "resolved" : "open");
            w.put("actual", i == 1 ? 25L : null);
            w.put("withinP10P90", i == 1 ? Boolean.FALSE : null);
            w.put("exceedance", i == 1 ? "above-p95" : null);
            windows.add(w);
        }
        o.setWindows(windows);
        return List.of(toTree(new ai.foresignal.analytics.finding.FindingEvent(11,
                ai.foresignal.analytics.finding.FindingEvent.OUTLOOK_PUBLISHED, o.getId(),
                ai.foresignal.outlook.regional.OutlookViews.published(o), at).payload()));
    }

    /** {@code index.changed} payloads (prompt 126), with and without the previous day's value. */
    static List<JsonNode> indexChangedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-10-17T03:45:00Z");
        List<JsonNode> out = new java.util.ArrayList<>();
        for (Double previous : java.util.Arrays.asList(55.0, null)) {
            ai.foresignal.outlook.index.RegionalIndexValueEntity v = new ai.foresignal.outlook.index
                    .RegionalIndexValueEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "instability", "YE",
                            java.time.LocalDate.of(2026, 10, 16), 62.5, at);
            v.setDelta24h(previous == null ? null : 7.5);
            out.add(toTree(new ai.foresignal.analytics.finding.FindingEvent(12,
                    ai.foresignal.analytics.finding.FindingEvent.INDEX_CHANGED, v.getId(),
                    ai.foresignal.outlook.OutlookEventSummaries.changed(v, previous, "fitted"), at).payload()));
        }
        return out;
    }

    /** {@code impact.projected} and {@code impact.superseded} payloads (prompt 127), with and without a question. */
    static List<JsonNode> impactEvents(String event) {
        java.time.Instant at = java.time.Instant.parse("2026-09-10T15:10:00Z");
        List<JsonNode> out = new java.util.ArrayList<>();
        for (boolean superseded : new boolean[] {false, true}) {
            ai.foresignal.analytics.finding.AnalyticFindingEntity f = new ai.foresignal.analytics.finding
                    .AnalyticFindingEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "projected-impact", "Projection", "notable",
                            at, at);
            java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
            stats.put("hazardKey", "nhc-wind-probs:34");
            stats.put("hazardType", "tropical-wind-34");
            stats.put("sourceRef", "record:nhc-wind-probs/34kt:40-50@12");
            stats.put("impactFamily", "outage");
            stats.put("admin1", "US-TX");
            stats.put("pImpact", 0.22);
            f.setStats(stats);
            f.setUpdatedAt(at);
            if (superseded) {
                f.setSupersededBy("01JA8Q3V7S2K9W4T6Y1B5N0M3D");
            }
            out.add(toTree(new ai.foresignal.analytics.finding.FindingEvent(13, event, f.getId(),
                    ai.foresignal.impact.ImpactViews.event(f, at), at).payload()));
        }
        return out;
    }

    /** {@code index.weights_proposed} payloads (prompt 126), with and without skill. */
    static List<JsonNode> indexWeightsProposedEvents() {
        java.time.Instant at = java.time.Instant.parse("2026-11-01T04:30:00Z");
        List<JsonNode> out = new java.util.ArrayList<>();
        for (boolean withSkill : new boolean[] {true, false}) {
            ai.foresignal.outlook.index.RegionalIndexFitEntity f = new ai.foresignal.outlook.index
                    .RegionalIndexFitEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "instability", at);
            f.setProposedWeights(java.util.Map.of("conflict", 0.6, "outages", 0.4));
            f.setWeightsVersion(2);
            if (withSkill) {
                java.util.Map<String, Object> skill = new java.util.LinkedHashMap<>();
                skill.put("evaluatedAt", at.toString());
                skill.put("n", 120);
                skill.put("pinballLoss", 1.2);
                skill.put("logScore", 2.1);
                skill.put("baselinePinballLoss", 1.5);
                skill.put("baselineLogScore", 2.4);
                skill.put("skillPinball", 0.2);
                skill.put("skillLog", 0.125);
                skill.put("baseline", "seasonal-naive outlook");
                f.setSkill(skill);
            }
            out.add(toTree(new ai.foresignal.analytics.finding.FindingEvent(13,
                    ai.foresignal.analytics.finding.FindingEvent.INDEX_WEIGHTS_PROPOSED, f.getId(),
                    ai.foresignal.outlook.OutlookEventSummaries.proposed(f), at).payload()));
        }
        return out;
    }

    /** {@code precursor.*} payloads as {@code FindingEvent.payload()} writes them (prompt 120), with and without lift. */
    static List<JsonNode> precursorEvents(String type) {
        java.time.Instant at = java.time.Instant.parse("2026-03-01T12:00:00Z");
        List<JsonNode> out = new java.util.ArrayList<>();
        for (Double lift : java.util.Arrays.asList(3.2, null)) {
            ai.foresignal.precursor.PrecursorEntity p = new ai.foresignal.precursor.PrecursorEntity(
                    "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "k", "event", at);
            p.setStatus(type.substring("precursor.".length()));
            p.setStatusReason(lift == null ? null : "validated on 2026-01-01 to 2026-03-01");
            p.setDescription("In YE, GNSS interference alerts was followed by maritime disruption events.");
            p.setLift(lift);
            out.add(toTree(new ai.foresignal.analytics.finding.FindingEvent(9, type, p.getId(),
                    ai.foresignal.precursor.PrecursorViews.summary(p), at).payload()));
        }
        return out;
    }

    /** {@code intel.event.*} payloads as {@code IntelTopic.payload} writes them (prompt 101, checkpoint 124). */
    static List<JsonNode> intelEvents(String change) {
        ai.foresignal.intel.IntelligenceEventEntity e = new ai.foresignal.intel.IntelligenceEventEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "seismic", "M5.1 near Austin", Instant.parse("2026-03-01T12:00:00Z"));
        e.setSeverity("warning");
        e.setStatus(ai.foresignal.intel.IntelEvent.RESOLVED.equals(change) ? "resolved" : "active");
        e.setConfidence(0.62);
        e.setConfidenceLabel("moderate");
        e.setEvidenceQuality("moderate");
        e.setMemberCount(3);
        e.setUpdatedAt(Instant.parse("2026-03-01T12:05:00Z"));
        String mergedInto = ai.foresignal.intel.IntelEvent.MERGED.equals(change) ? "01JA8Q3V7S2K9W4T6Y1B5N0M3D" : null;
        return List.of(toTree(ai.foresignal.intel.IntelTopic.payload(new ai.foresignal.intel.IntelEvent(7, e.getId(),
                change, 2, ai.foresignal.intel.IntelEventMapper.summary(e), mergedInto))));
    }

    /** The analyst topic over one stored note and one stored proposal (mocked repositories). */
    private static AnalystTopic analystTopic() {
        Instant at = Instant.parse("2026-03-01T12:00:00Z");
        ai.foresignal.analyst.AnalystNoteEntity note = new ai.foresignal.analyst.AnalystNoteEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "01JA8Q3V7S2K9W4T6Y1B5N0M3D", "01JA8Q3V7S2K9W4T6Y1B5N0M3E",
                "Port congestion", at);
        note.setBody("Two ports report delays.");
        note.setClaims(List.of(Map.of("text", "Delays at two ports", "basis", "evidence", "citations",
                List.of("ais-hub:1@42"))));
        note.setCitations(List.of("ais-hub:1@42"));
        note.setModel("test-model");
        ai.foresignal.analyst.AnalystProposalEntity proposal = new ai.foresignal.analyst.AnalystProposalEntity(
                "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "01JA8Q3V7S2K9W4T6Y1B5N0M3E", "01JA8Q3V7S2K9W4T6Y1B5N0M3D",
                "propose_watch", at, at.plus(Duration.ofDays(7)));
        proposal.setPayload(Map.of("name", "Port watch"));
        proposal.setRationale("Delays may grow.");
        proposal.setCitations(List.of("ais-hub:1@42"));
        ai.foresignal.analyst.AnalystNoteRepository notes = mock(ai.foresignal.analyst.AnalystNoteRepository.class);
        when(notes.findById(note.getId())).thenReturn(java.util.Optional.of(note));
        ai.foresignal.analyst.AnalystProposalRepository proposals =
                mock(ai.foresignal.analyst.AnalystProposalRepository.class);
        when(proposals.findById(proposal.getId())).thenReturn(java.util.Optional.of(proposal));
        return new AnalystTopic(mock(ai.foresignal.notify.NotificationBus.class), mock(SourceRegistry.class),
                mock(ai.foresignal.store.NotificationRepository.class), provider(notes), provider(proposals));
    }

    @SuppressWarnings("unchecked")
    private static <T> org.springframework.beans.factory.ObjectProvider<T> provider(T value) {
        org.springframework.beans.factory.ObjectProvider<T> p = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(value);
        return p;
    }

    /** {@code analyst.note} payloads as {@code AnalystTopic.payload} writes them (prompt 102, checkpoint 124). */
    static List<JsonNode> analystNotes() {
        NotificationMessage message = NotificationMessage.builder(AnalystTopic.NOTE, AnalystTopic.NOTE,
                "Port congestion", "info").data(Map.of("noteId", "01JA8Q3V7S2K9W4T6Y1B5N0M3C")).build();
        return List.of(toTree(analystTopic().payload(message)));
    }

    /** {@code analyst.proposal} payloads as {@code AnalystTopic.payload} writes them (prompt 102, checkpoint 124). */
    static List<JsonNode> analystProposals() {
        List<JsonNode> out = new ArrayList<>();
        for (String change : List.of("created", "executed", "rejected")) {
            NotificationMessage message = NotificationMessage.builder(AnalystTopic.PROPOSAL, AnalystTopic.PROPOSAL,
                    "Analyst proposal", "info").data(Map.of("proposalId", "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "change",
                    change)).build();
            out.add(toTree(analystTopic().payload(message)));
        }
        return out;
    }

    /** {@code canonical.*} payloads as {@code EvidenceEvent.payload()} writes them (prompt 116). */
    static List<JsonNode> evidenceEvents(String type) {
        ai.foresignal.evidence.EvidenceEvent event = new ai.foresignal.evidence.EvidenceEvent(type,
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "seismic", "open", java.time.Instant.parse("2026-03-01T12:00:00Z"),
                List.of("TR"), 3, 2, List.of("LATE_REVISION"), List.of("LATE_REVISION"), null,
                List.of("01JA8Q3V7S2K9W4T6Y1B5N0M3D"), List.of("usgs-earthquakes"));
        return List.of(toTree(event.payload()));
    }

    // ------------------------------------------------------------------------- the table

    @Test
    @DisplayName("every current event name of the implemented streams has a payload factory, and every factory is in the spec")
    void tableMatchesTheSpec() {
        int current = OpenApiSpec.currentPrompt();
        Set<String> specEvents = new LinkedHashSet<>();
        for (String operationId : STREAMS) {
            JsonNode events = sseEvents(operationId);
            for (String name : events.propertyNames()) {
                JsonNode prompt = events.get(name).get("prompt");
                if (prompt == null || Integer.parseInt(prompt.asString()) <= current) {
                    specEvents.add(name);
                }
            }
        }
        assertThat(SSE_EVENTS.keySet())
                .as("event names emitted by the code (this table) versus x-foresignal-sse-events")
                .containsExactlyInAnyOrderElementsOf(specEvents);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"streamChanges", "streamAlerts", "streamEvents"})
    @DisplayName("every payload of every current event conforms to the schema its stream maps it to")
    void payloadsConform(String operationId) {
        JsonNode events = sseEvents(operationId);
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (String name : events.propertyNames()) {
            Supplier<List<JsonNode>> factory = SSE_EVENTS.get(name);
            if (factory == null) {
                continue; // a later prompt's event; tableMatchesTheSpec covers the current ones
            }
            String ref = events.get(name).get("schema").get("$ref").asString();
            List<JsonNode> payloads = factory.get();
            assertThat(payloads).as("payloads for " + name).isNotEmpty();
            for (JsonNode payload : payloads) {
                checked++;
                for (String error : SCHEMAS.validateRef(ref, payload)) {
                    problems.add(operationId + " " + name + " " + ref + ": " + error + " in " + payload);
                }
            }
        }
        assertThat(checked).isPositive();
        assertThat(problems).isEmpty();
    }

    // ------------------------------------------------------------------------- non-SSE events

    @ParameterizedTest(name = "AlertEvent type {0}")
    @ValueSource(strings = {"fired", "updated", "acknowledged", "snoozed", "resolved"})
    @DisplayName("an AlertEvent of every lifecycle type conforms to AlertEvent")
    void alertEventsConform(String type) {
        for (AlertView view : List.of(alert(type, "r"), alert(type, null))) {
            JsonNode payload = toTree(new AlertMapper().event(new AlertEvent(type, view, view.suppressed())));

            assertThat(SCHEMAS.validateComponent("AlertEvent", payload)).as(payload.toString()).isEmpty();
            assertThat(payload.get("type").asString()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("notification messages (alert, lifecycle update, system) conform to NotificationMessage")
    void notificationMessagesConform() {
        for (NotificationMessage message : List.of(NotifyFixtures.message(), NotifyFixtures.update("alert.resolved"),
                systemMessage())) {
            JsonNode payload = toTree(NotificationJson.toMap(message));

            assertThat(SCHEMAS.validateComponent("NotificationMessage", payload)).as(payload.toString()).isEmpty();
        }
    }

    @Test
    @DisplayName("the webhook channel's default body conforms to WebhookEnvelope")
    void webhookBodyConforms() {
        WebhookChannel webhook = new WebhookChannel();
        for (NotificationMessage message : List.of(NotifyFixtures.message(), systemMessage())) {
            PreparedRequest request = webhook.render(message, NotifyFixtures.settings(
                    Map.of("url", "https://receiver.example/hook"), Map.of("hmacSecret", "s3cret")));
            JsonNode body = SseFixtures.JSON.readTree(request.bodyText());

            assertThat(SCHEMAS.validateComponent("WebhookEnvelope", body)).as(body.toString()).isEmpty();
            assertThat(request.headers()).containsKeys("X-Foresignal-Event", "X-Foresignal-Delivery",
                    "X-Foresignal-Timestamp", "X-Foresignal-Signature");
        }
    }

    @Test
    @DisplayName("the validator rejects a payload that breaks its schema, so a pass means something")
    void validatorRejectsNonConformingPayloads() {
        JsonNode bad = SseFixtures.JSON.readTree("{\"reason\":\"not-a-reason\"}");

        assertThat(SCHEMAS.validateComponent("ResyncEvent", bad)).isNotEmpty();
        assertThat(SCHEMAS.validateComponent("HeartbeatEvent", SseFixtures.JSON.readTree("{\"x\":1}"))).isNotEmpty();
    }

    // ------------------------------------------------------------------------- factories

    private static JsonNode sseEvents(String operationId) {
        return OpenApiSpec.routes().stream()
                .filter(op -> operationId.equals(op.operationId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no operation " + operationId))
                .node().get("x-foresignal-sse-events");
    }

    private static JsonNode toTree(Object value) {
        return SseFixtures.JSON.readTree(SseFixtures.JSON.writeValueAsString(value));
    }

    private static List<JsonNode> data(List<SseFrame> frames, String event) {
        return frames.stream().filter(f -> f.event().equals(event)).map(f -> SseFixtures.JSON.readTree(f.data())).toList();
    }

    /** A live update with a geometry and a quality block, and one without either. */
    private static List<LiveUpdate> liveUpdates() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("gs", 412.5);
        properties.put("track", 271);
        properties.put("squawk", null);
        Map<String, Object> quality = new LinkedHashMap<>();
        quality.put("accuracyMeters", 12.0);
        quality.put("positionSource", "adsb");
        return List.of(
                new LiveUpdate("src", "a", "aircraft", T, T.plusSeconds(360), 3,
                        GEOMETRY.createPoint(new Coordinate(-97.75, 30.25)), box(-97.75, 30.25), box(-97.8, 30.2),
                        properties, quality),
                SseFixtures.live("b", T, box(1, 1), null));
    }

    /** Runs one client through every control event, the way SseClientTest does. */
    private static List<JsonNode> controlFrames(String event) {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.limits = limits(1, 0, 0);
        b.filter = StreamFilter.changesOnly(new ChangeFilter(List.of(), List.of(), List.of(), null, null, false, true,
                true, Set.of()));
        SseClient client = b.build();
        client.pump();
        for (LiveUpdate update : liveUpdates()) {
            client.offerLive(update);
        }
        client.offerTouch(touch("c", T, box(1, 1)), List.of("aircraft"));
        b.clock.advance(Duration.ofSeconds(1));
        client.pump();
        client.resync("gap");
        client.applyFilter(client.filter().next(List.of(ChangesTopic.NAME),
                Map.of(ChangesTopic.NAME, client.filter().changes())), Map.of());
        b.clock.advance(Duration.ofSeconds(15));
        client.pump();
        List<JsonNode> out = data(b.sender.frames, event);
        assertThat(out).as(event + " frames in " + b.sender.events()).isNotEmpty();
        return out;
    }

    /** {@code reset} for each reason: buffer_overflow, replay_cap and resync_required. */
    private static List<JsonNode> resets() {
        List<SseFrame> frames = new ArrayList<>();

        SseFixtures.ClientBuilder overflow = new SseFixtures.ClientBuilder();
        overflow.limits = limits(1, 0, 0);
        SseClient a = overflow.build();
        a.pump();
        a.offerFifo(ChangesTopic.NAME, f -> SseFixtures.fifo(1));
        a.offerFifo(ChangesTopic.NAME, f -> SseFixtures.fifo(2));
        a.pump();
        frames.addAll(overflow.sender.frames);

        SseFixtures.ClientBuilder cap = new SseFixtures.ClientBuilder();
        cap.limits = limits(0, 1, 0);
        cap.replays.put(ChangesTopic.NAME, new ReplayRequest(0, true));
        cap.driver = (topic, request, filter, sink) -> {
            for (long seq = 1; seq <= 2; seq++) {
                if (!sink.emit(SseFixtures.fifo(seq))) {
                    return new ReplayOutcome.CapReached();
                }
            }
            return new ReplayOutcome.Done(2);
        };
        cap.build().pump();
        frames.addAll(cap.sender.frames);

        SseFixtures.ClientBuilder floor = new SseFixtures.ClientBuilder();
        floor.replays.put(ChangesTopic.NAME, new ReplayRequest(3, true));
        floor.driver = (topic, request, filter, sink) -> new ReplayOutcome.ResyncRequired(77);
        floor.build().pump();
        frames.addAll(floor.sender.frames);

        List<JsonNode> out = data(frames, "reset");
        assertThat(out).extracting(n -> n.get("reason").asString())
                .contains("buffer_overflow", "replay_cap", "resync_required");
        return out;
    }

    /** {@code change} events as {@link ChangesTopic} renders them: from a loaded row and from the bus summary. */
    private static List<JsonNode> changes() {
        RecordMapper mapper = new RecordMapper(mock(SourceRegistry.class), SseFixtures.JSON);
        RecordRow row = new RecordRow("usgs-earthquakes", "us7000abcd", "earthquake", 2, 41L, T, T, T.minusSeconds(30),
                T, T.plusSeconds(86_400), false, "M 5.1 - 10 km N of Somewhere",
                "{\"type\":\"Point\",\"coordinates\":[-97.75,30.25,10.0]}", "{\"mag\":5.1,\"place\":\"Somewhere\"}",
                null, null, Boolean.FALSE, 0, null, null, T, T, PersistReason.CHANGE.code(), 40L,
                HistoryTier.STANDARD.code(), 7L, new double[] {-97.75, 30.25, -97.75, 30.25}, null);
        ChangeSummary first = new ChangeSummary(42L, null, HistoryTier.TRACK, "src", "e42", "aircraft", 1,
                PersistReason.FIRST, T, null, null, false, 0, box(1, 1), null, null);
        ChangeSummary tombstone = SseFixtures.change(43L, "src", "aircraft", null, box(1, 1), box(1, 1), true);

        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.payloads = (topic, seqs) -> Map.of(41L, row.payload());
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, 41L, ChangesTopic.EVENT,
                mapper.streamChange(row, box(-97.8, 30.2), null, false), 41L));
        client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, 42L, ChangesTopic.EVENT,
                mapper.streamChange(first, false), null));
        client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, 43L, ChangesTopic.EVENT,
                mapper.streamChange(tombstone, true), null));
        client.pump();
        List<JsonNode> out = data(b.sender.frames, ChangesTopic.EVENT);
        assertThat(out).hasSize(3);
        assertThat(out.getFirst().has("payload")).as("deferred payload loaded").isTrue();
        return out;
    }

    /** {@code hello} as {@link SseHub#open} builds it for an anonymous caller (with a stream key). */
    @SuppressWarnings("unchecked")
    private static List<JsonNode> hello() {
        EventTopic<ChangeFilter> topic = mock(EventTopic.class);
        when(topic.name()).thenReturn(ChangesTopic.NAME);
        when(topic.cursorKey()).thenReturn(ChangesTopic.CURSOR_KEY);
        when(topic.head()).thenReturn(0L);
        when(topic.replay(any(), any(), any())).thenAnswer(inv -> new ReplayOutcome.Done(0));
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.STREAM_LIMITS), eq(StreamLimits.class), any()))
                .thenReturn(StreamLimits.defaults());
        ChangeBus bus = mock(ChangeBus.class);
        when(bus.lastPublishedSeq()).thenReturn(99L);
        SseHub hub = new SseHub(List.of(topic), settings, bus, SseFixtures.JSON);
        FakeSender sender = new FakeSender();
        SseClient client = hub.open(new SseHub.OpenRequest(StreamPrincipal.anonymous("127.0.0.1"),
                SseClient.CursorMode.COMPOSITE, StreamFilter.changesOnly(ChangeFilter.all(Set.of())), Map.of(), true),
                sender);
        try {
            await().atMost(Duration.ofSeconds(5)).until(() -> !sender.named("hello").isEmpty());
        } finally {
            client.close();
        }
        return data(sender.frames, "hello");
    }

    /** Alert lifecycle events under their SSE names, as {@link AlertsTopic} publishes them. */
    private static List<JsonNode> alertEvents(String sseName) {
        List<JsonNode> out = new ArrayList<>();
        for (String type : List.of(AlertEvent.FIRED, AlertEvent.UPDATED, AlertEvent.ACKNOWLEDGED, AlertEvent.SNOOZED,
                AlertEvent.RESOLVED)) {
            AlertEvent event = AlertEvent.of(type, alert(type, "Quakes"));
            if (event.sseEvent().equals(sseName)) {
                out.add(toTree(new AlertMapper().event(event)));
            }
        }
        return out;
    }

    /**
     * {@code alert.enriched} payloads (prompt 030): the full {@code Alert} as {@link AlertsTopic} sends it,
     * with a summary and its citations, and an alert whose rule was deleted.
     */
    private static List<JsonNode> enrichedAlerts() {
        List<JsonNode> out = new ArrayList<>();
        for (String ruleName : java.util.Arrays.asList("Quakes", null)) {
            AlertView a = alert(AlertEvent.FIRED, ruleName);
            AlertView enriched = new AlertView(a.id(), a.ruleId(), a.ruleName(), a.seq(), a.sourceId(), a.externalId(),
                    a.kind(), a.firedAt(), a.observedAt(), a.message(), a.severity(), a.state(), a.rev() + 1,
                    a.acknowledgedAt(), a.acknowledgedBy(), a.resolvedAt(), a.resolvedBy(), a.resolvedSeq(), a.feedback(),
                    a.feedbackAt(), a.note(), a.snoozedUntil(), a.suppressed(), a.suppressedReason(), a.tags(),
                    a.recordSnapshot(), "A M5.1 near Somewhere [usgs-earthquakes:us7000abcd@41] [alert:7].",
                    T.plusSeconds(5), "[{\"id\":\"usgs-earthquakes:us7000abcd\",\"seq\":41,\"title\":\"M5.1\","
                            + "\"type\":\"record\"},{\"id\":\"alert:7\",\"seq\":null,\"title\":\"Quakes\","
                            + "\"type\":\"alert\"}]");
            out.add(toTree(new AlertMapper().alert(enriched, null)));
        }
        return out;
    }

    /** An alert in the state that {@code type} leaves it in; {@code ruleName} null means the rule was deleted. */
    private static AlertView alert(String type, String ruleName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mag", 5.1);
        String snapshot = AlertSnapshots.build(new MatchRow(41L, 40L, "usgs-earthquakes", "us7000abcd", "earthquake", 2,
                PersistReason.CHANGE.code(), T, null, T, null, false,
                "{\"type\":\"Point\",\"coordinates\":[-97.75,30.25]}", "{\"mag\":5.1}", null), payload, SseFixtures.JSON);
        boolean acknowledged = AlertEvent.ACKNOWLEDGED.equals(type);
        boolean resolved = AlertEvent.RESOLVED.equals(type);
        boolean snoozed = AlertEvent.SNOOZED.equals(type);
        Instant later = T.plusSeconds(60);
        return new AlertView(7L, RULE, ruleName, 41L, "usgs-earthquakes", "us7000abcd", "earthquake", T, T,
                "M5.1 near Somewhere", "warning", resolved ? "resolved" : acknowledged ? "acknowledged" : "firing", 3L,
                acknowledged ? later : null, acknowledged ? "token:admin" : null, resolved ? later : null,
                resolved ? "inverse_edge" : null, resolved ? 44L : null,
                AlertEvent.UPDATED.equals(type) ? "useful" : null, AlertEvent.UPDATED.equals(type) ? later : null,
                AlertEvent.UPDATED.equals(type) ? "checked" : null, snoozed ? later.plusSeconds(3600) : null, false,
                null, List.of("quake"), snapshot);
    }

    /** A system notification, as {@code notify.SystemEvent} produces them (019). */
    private static NotificationMessage systemMessage() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("consumer", "trigger.cursor");
        return NotificationMessage.builder(NotificationMessage.SYSTEM, "system.retention_consumer_lag",
                        "Retention consumer trigger.cursor is lagging", "warning")
                .id(9L).body("The clamp was released.").data(data).origin(NotifyFixtures.ORIGIN).createdAt(T).build();
    }
}
