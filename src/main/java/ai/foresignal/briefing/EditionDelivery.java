package ai.foresignal.briefing;

import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.notify.types.EmailChannel;
import ai.foresignal.security.user.PermissionResolver;
import ai.foresignal.security.user.Permissions;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Delivers an edition run to its {@code deliveryTargets} (prompt NP-004), each in its own shape, over the existing 018
 * notify channels.
 *
 * <ul>
 *   <li><b>email:</b> a {@code briefing} notification with the whole edition. The email body is the sanitized
 *       {@link BriefingHtmlRenderer} page ({@value EmailChannel#EDITION_HTML}), the text part the markdown; the run's
 *       audio is attached by the 141 {@code AudioAttachments}; a "Read online" link goes to the hosted site
 *       (NP-005).
 *   <li><b>push:</b> a {@code briefing} notification with the headline, a summary of at most
 *       {@value #SUMMARY_CHARS} characters and the link to the site; never the whole body, the audio, the citations
 *       or the statistics.
 *   <li><b>alert:</b> an {@code alert} notification, subtype {@value #ALERT_SUBTYPE}, in the alert shape: severity
 *       ({@code options.severity}: {@code info}, {@code warning} or {@code critical}; default {@code info}),
 *       headline, summary and link. It is neither an alert lifecycle update nor an {@code alert.fired}, so no
 *       enrichment wait or rule grouping applies.
 *   <li><b>website</b> and <b>podcast</b> are pulled, not pushed: their result records the URL where the run is
 *       available ({@code getEditionSite}, {@code getBriefingPodcast}).
 * </ul>
 *
 * <p>Each channel target is published on its own, to that channel alone, through
 * {@link NotificationService#publish(NotificationMessage, java.util.function.Predicate, java.util.Collection)}, so the
 * channel's rate limit, quiet hours, backlog cap and the notification pause apply exactly as to any send. An owned
 * edition may use only its owner's personal channels, and the global ones when the owner holds
 * {@code channels.global}; any other channel is skipped. One target failing never stops the others; every target's
 * outcome is returned for {@code BriefingRun.deliveryResults}.
 */
@Component
public class EditionDelivery {

    private static final Logger log = LoggerFactory.getLogger(EditionDelivery.class);

    /** The longest push and alert summary. */
    public static final int SUMMARY_CHARS = 280;

    /** The subtype of edition alerts. */
    public static final String ALERT_SUBTYPE = "edition.alert";

    /** A target handed to its channel. */
    public static final String QUEUED = "queued";

    /** A pulled target the run is available at. */
    public static final String AVAILABLE = "available";

    /** A target that was not attempted. */
    public static final String SKIPPED = "skipped";

    /** A target whose send failed. */
    public static final String FAILED = "failed";

    private static final Set<String> SEVERITIES = Set.of("info", "warning", "critical");

    private final NotificationService notifications;
    private final NotificationDeliveryRepository deliveries;
    private final NotificationChannelRepository channels;
    private final BriefingLinks links;
    private final BriefingHtmlRenderer html;
    private final ObjectProvider<UserRepository> users;
    private final ObjectProvider<PermissionResolver> permissions;

    /** Creates the component. */
    public EditionDelivery(NotificationService notifications, NotificationDeliveryRepository deliveries,
            NotificationChannelRepository channels, BriefingLinks links, BriefingHtmlRenderer html,
            ObjectProvider<UserRepository> users, ObjectProvider<PermissionResolver> permissions) {
        this.notifications = notifications;
        this.deliveries = deliveries;
        this.channels = channels;
        this.links = links;
        this.html = html;
        this.users = users;
        this.permissions = permissions;
    }

    /**
     * The outcome of a run's delivery.
     *
     * @param results one {@code BriefingRun.deliveryResults} entry per target, in target order
     * @param deliveries the channel deliveries enqueued
     */
    public record Outcome(List<Map<String, Object>> results, int deliveries) {}

    /**
     * Delivers {@code run} to every target of {@code input}.
     *
     * @param title the run's title (the notification title)
     * @param input the definition (its {@code edition().deliveryTargets()})
     * @param run the completed run
     * @param edition the edition context, or null for a briefing no user owns
     * @param runView the {@code BriefingRun} object of the run, as plain JSON
     */
    public Outcome deliver(String title, BriefingInput input, BriefingRunEntity run, EditionContext edition,
            Map<String, Object> runView) {
        List<Map<String, Object>> results = new ArrayList<>();
        int total = 0;
        for (DeliveryTarget target : input.edition().deliveryTargets()) {
            Map<String, Object> result = result(target);
            try {
                total += deliverOne(target, title, run, edition, runView, result);
            } catch (RuntimeException e) {
                log.warn("Delivery target {} of briefing run {} failed", target.toMap(), run.getId(), e);
                result.put("status", FAILED);
                result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            results.add(result);
        }
        return new Outcome(results, total);
    }

    private int deliverOne(DeliveryTarget target, String title, BriefingRunEntity run, EditionContext edition,
            Map<String, Object> runView, Map<String, Object> result) {
        String site = links.editionSite(run.getBriefingId());
        switch (target.kind()) {
            case DeliveryTarget.WEBSITE -> {
                result.put("status", AVAILABLE);
                result.put("url", site);
                return 0;
            }
            case DeliveryTarget.PODCAST -> {
                if (!(run.getAudio() != null && BriefingAudioService.OK.equals(run.getAudio().get("status")))) {
                    result.put("status", SKIPPED);
                    result.put("error", "the run has no synthesized audio");
                    return 0;
                }
                result.put("status", AVAILABLE);
                result.put("url", links.podcast(run.getBriefingId()));
                return 0;
            }
            default -> {
                // A channel target.
            }
        }
        Optional<NotificationChannelEntity> channel = channels.findById(target.channelId());
        if (channel.isEmpty()) {
            return skipped(result, "the channel does not exist");
        }
        if (!channel.get().isEnabled()) {
            return skipped(result, "the channel is disabled");
        }
        if (!mayUse(edition, channel.get())) {
            return skipped(result, "the channel is not one of the edition owner's channels");
        }
        NotificationMessage message = switch (target.format()) {
            case DeliveryTarget.EMAIL -> email(title, run, edition, runView, site);
            case DeliveryTarget.PUSH -> push(title, run, runView, site);
            default -> alert(title, run, runView, site, target.options());
        };
        NotificationMessage published = notifications.publish(message, c -> false, List.of(target.channelId()));
        int count = published.id() == null ? 0 : deliveries.findByNotificationIdOrderByIdAsc(published.id()).size();
        result.put("status", count > 0 ? QUEUED : SKIPPED);
        result.put("notificationId", published.id());
        if (count == 0) {
            result.put("error", "the channel took no delivery");
        }
        return count;
    }

    private static int skipped(Map<String, Object> result, String why) {
        result.put("status", SKIPPED);
        result.put("error", why);
        return 0;
    }

    /**
     * True when an edition may send to {@code channel}: any channel for a briefing no user owns; for an owned edition,
     * the owner's personal channels, and the global channels when the owner holds {@code channels.global}.
     */
    boolean mayUse(EditionContext edition, NotificationChannelEntity channel) {
        if (edition == null || !edition.owned()) {
            return true;
        }
        UserRepository repo = users.getIfAvailable();
        String principal = repo == null ? null
                : repo.findById(edition.ownerUserId()).map(u -> u.getPrincipalId()).orElse(null);
        if (principal != null && principal.equals(channel.getOwnerPrincipalId())) {
            return true;
        }
        if (channel.getOwnerPrincipalId() != null) {
            return false;
        }
        PermissionResolver resolver = permissions.getIfAvailable();
        return resolver != null && resolver.forUser(edition.ownerUserId()).has(Permissions.CHANNELS_GLOBAL);
    }

    /** The whole edition, its sanitized page and its audio (through the run's {@code audio}), with the site link. */
    NotificationMessage email(String title, BriefingRunEntity run, EditionContext edition, Map<String, Object> runView,
            String site) {
        String markdown = run.getContentMarkdown().strip() + "\n\n[Read online](" + DigestRenderer.url(site) + ")";
        Map<String, Object> data = new LinkedHashMap<>(runView);
        data.put(EmailChannel.EDITION_HTML, html.page(title, markdown, "Foresignal edition " + run.getId(),
                edition == null ? null : edition.language()));
        data.put("siteUrl", site);
        return NotificationMessage.builder(NotificationMessage.BRIEFING, BriefingRunner.SUBTYPE, title, "info")
                .shortTitle(run.getBriefingName())
                .body(markdown)
                .url(site)
                .links(List.of(new NotificationMessage.Link("Read online", site)))
                .data(data)
                .tags(List.of("briefing", "edition"))
                .briefingRunId(run.getId())
                .build();
    }

    /** The headline, a short summary and the link: never the body, audio, citations or statistics. */
    NotificationMessage push(String title, BriefingRunEntity run, Map<String, Object> runView, String site) {
        return NotificationMessage.builder(NotificationMessage.BRIEFING, BriefingRunner.SUBTYPE, title, "info")
                .shortTitle(run.getBriefingName())
                .body(summary(run.getContentMarkdown()))
                .url(site)
                .links(List.of(new NotificationMessage.Link("Read the edition", site)))
                .data(brief(runView, site))
                .tags(List.of("briefing", "edition"))
                .briefingRunId(run.getId())
                .build();
    }

    /** The alert shape: severity, headline, summary and link. */
    NotificationMessage alert(String title, BriefingRunEntity run, Map<String, Object> runView, String site,
            Map<String, Object> options) {
        String severity = options.get("severity") instanceof String s && SEVERITIES.contains(s) ? s : "info";
        return NotificationMessage.builder(NotificationMessage.ALERT, ALERT_SUBTYPE, title, severity)
                .shortTitle(run.getBriefingName())
                .body(summary(run.getContentMarkdown()) + "\n\n[Read the edition](" + DigestRenderer.url(site) + ")")
                .url(site)
                .links(List.of(new NotificationMessage.Link("Read the edition", site)))
                .data(brief(runView, site))
                .tags(List.of("briefing", "edition", "alert"))
                .briefingRunId(run.getId())
                .build();
    }

    /** The run's identity and period, without its content, audio, citations or statistics. */
    static Map<String, Object> brief(Map<String, Object> runView, String site) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : List.of("id", "briefingId", "status", "periodFrom", "periodTo", "mode")) {
            out.put(key, runView.get(key));
        }
        out.put("siteUrl", site);
        return out;
    }

    /**
     * The first paragraph of {@code markdown} that is not a heading, as plain text, cut at a word to at most
     * {@value #SUMMARY_CHARS} characters with an ellipsis.
     */
    public static String summary(String markdown) {
        String text = "";
        for (String block : (markdown == null ? "" : markdown).split("\\n\\s*\\n")) {
            String b = block.strip();
            if (!b.isEmpty() && !b.startsWith("#")) {
                text = MarkdownRenderer.render(b, MarkdownRenderer.Format.PLAIN).replaceAll("\\s+", " ").strip();
                if (!text.isEmpty()) {
                    break;
                }
            }
        }
        if (text.length() <= SUMMARY_CHARS) {
            return text;
        }
        int cut = text.lastIndexOf(' ', SUMMARY_CHARS - 1);
        return text.substring(0, cut > SUMMARY_CHARS / 2 ? cut : SUMMARY_CHARS - 1).strip() + "…";
    }

    private static Map<String, Object> result(DeliveryTarget target) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", target.kind());
        out.put("channelId", target.channelId() == null ? null : target.channelId().toString());
        out.put("format", target.format());
        out.put("status", null);
        out.put("notificationId", null);
        out.put("url", null);
        out.put("error", null);
        return out;
    }
}
