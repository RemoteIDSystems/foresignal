package ai.foresignal.notify;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * One message delivered to channels (OpenAPI {@code NotificationMessage}, prompt 018).
 *
 * <p>Every message is persisted as a {@code notification} row before fan-out; {@link #id()} is that
 * row's id (null before it is persisted, and for the synthetic message of a channel test).
 *
 * @param id the notification id, or null before persistence
 * @param event {@code alert}, {@code briefing}, {@code system} or {@code test}
 * @param subtype for example {@code alert.fired} or {@code system.source_failing}
 * @param title the title
 * @param shortTitle at most 40 characters, for watches, mesh and push; null derives one from the title
 * @param bodyMarkdown the body in Foresignal's markdown subset, or null
 * @param severity {@code info}, {@code warning} or {@code critical}
 * @param url the API link, or null
 * @param links at most three link buttons
 * @param data the full event data (the alert JSON for alerts), or null
 * @param tags the tags
 * @param attachments image attachments (prompt 055: the map of a geolocated alert, added at send time)
 * @param updateOf the notification this one updates, or null
 * @param origin the install id of the sending instance (loop guard)
 * @param createdAt when created
 * @param alertId the alert, or null
 * @param ruleId the rule, or null
 * @param briefingRunId the briefing run, or null
 */
public record NotificationMessage(
        Long id,
        String event,
        String subtype,
        String title,
        String shortTitle,
        String bodyMarkdown,
        String severity,
        String url,
        List<Link> links,
        Map<String, Object> data,
        List<String> tags,
        List<Attachment> attachments,
        Long updateOf,
        String origin,
        Instant createdAt,
        Long alertId,
        UUID ruleId,
        UUID briefingRunId) {

    /** {@code event} of alert messages. */
    public static final String ALERT = "alert";

    /** {@code event} of briefing messages (029). */
    public static final String BRIEFING = "briefing";

    /** {@code event} of system messages. */
    public static final String SYSTEM = "system";

    /** {@code event} of the synthetic channel test message. */
    public static final String TEST = "test";

    /** The longest short title. */
    public static final int SHORT_TITLE_MAX = 40;

    /** The most link buttons. */
    public static final int MAX_LINKS = 3;

    /** Normalizes: required fields present, lists copied, links capped at three, short title derived. */
    public NotificationMessage {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(subtype, "subtype");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(severity, "severity");
        links = links == null ? List.of() : List.copyOf(links.subList(0, Math.min(MAX_LINKS, links.size())));
        tags = tags == null ? List.of() : List.copyOf(tags);
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
        if (shortTitle == null || shortTitle.isBlank()) {
            shortTitle = Truncator.truncate(title, SHORT_TITLE_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
        } else if (shortTitle.length() > SHORT_TITLE_MAX) {
            shortTitle = Truncator.truncate(shortTitle, SHORT_TITLE_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
        }
    }

    /** True for an alert lifecycle update (it has an original). */
    public boolean isUpdate() {
        return updateOf != null;
    }

    /** A copy with another id. */
    public NotificationMessage withId(Long newId) {
        return new NotificationMessage(newId, event, subtype, title, shortTitle, bodyMarkdown, severity, url, links,
                data, tags, attachments, updateOf, origin, createdAt, alertId, ruleId, briefingRunId);
    }

    /** A copy with another title and body (channel templates, digests). */
    public NotificationMessage withText(String newTitle, String newBody) {
        // The short title is kept (the rule name for alerts, set by the router): a channel's title template changes
        // the title, not what watches, mesh and push show (checkpoint 041; it was re-derived from the templated
        // title, so a Web Push shortTitle read "[critical] Quakes" instead of "Quakes").
        return new NotificationMessage(id, event, subtype, newTitle, shortTitle, newBody, severity, url, links, data,
                tags, attachments, updateOf, origin, createdAt, alertId, ruleId, briefingRunId);
    }

    /** A new builder. */
    public static Builder builder(String event, String subtype, String title, String severity) {
        return new Builder(event, subtype, title, severity);
    }

    /**
     * A link button.
     *
     * @param label at most 80 characters
     * @param url the target
     */
    public record Link(String label, String url) {

        /** Requires both. */
        public Link {
            Objects.requireNonNull(label, "label");
            Objects.requireNonNull(url, "url");
            if (label.length() > 80) {
                label = label.substring(0, 80);
            }
        }
    }

    /**
     * An image attachment (prompt 055). The delivery stores only the media reference; the delivery worker
     * loads the bytes from the media store at send time, so a retry resends them. Types that send images
     * ({@link ChannelCapabilities#attachments()}) use {@link #bytes()}; the others ignore it and the message
     * carries a link to {@link #url()} instead.
     *
     * @param filename the file name, for example {@code map.png}
     * @param contentType the media type
     * @param bytes the content, or null when it is not loaded (link-only channels)
     * @param mediaId the media store id, or null
     * @param size the content's size in bytes
     * @param url {@code {publicBaseUrl}/v1/media/{id}}, or null
     */
    public record Attachment(String filename, String contentType, byte[] bytes, String mediaId, long size, String url) {

        /** The 018 shape: the size from the bytes, no URL. */
        public Attachment(String filename, String contentType, byte[] bytes, String mediaId) {
            this(filename, contentType, bytes, mediaId, bytes == null ? 0 : bytes.length, null);
        }

        /** True when the content is loaded. */
        public boolean loaded() {
            return bytes != null && bytes.length > 0;
        }
    }

    /** A copy carrying {@code newAttachments} (prompt 055: the delivery worker attaches the alert's map). */
    public NotificationMessage withAttachments(List<Attachment> newAttachments) {
        return new NotificationMessage(id, event, subtype, title, shortTitle, bodyMarkdown, severity, url, links, data,
                tags, newAttachments, updateOf, origin, createdAt, alertId, ruleId, briefingRunId);
    }

    /** A copy with other link buttons (still capped at three). */
    public NotificationMessage withLinks(List<Link> newLinks) {
        return new NotificationMessage(id, event, subtype, title, shortTitle, bodyMarkdown, severity, url, newLinks, data,
                tags, attachments, updateOf, origin, createdAt, alertId, ruleId, briefingRunId);
    }

    /** The first attachment whose bytes are loaded, or null. */
    public Attachment image() {
        for (Attachment a : attachments) {
            if (a.loaded()) {
                return a;
            }
        }
        return null;
    }

    /** The URL of the first attachment that has one (prompt 108 image links), or null. */
    public String imageUrl() {
        for (Attachment a : attachments) {
            if (a.url() != null) {
                return a.url();
            }
        }
        return null;
    }

    /** Builds a {@link NotificationMessage}. */
    public static final class Builder {
        private Long id;
        private final String event;
        private final String subtype;
        private final String title;
        private final String severity;
        private String shortTitle;
        private String body;
        private String url;
        private List<Link> links = List.of();
        private Map<String, Object> data;
        private List<String> tags = List.of();
        private Long updateOf;
        private String origin;
        private Instant createdAt;
        private Long alertId;
        private UUID ruleId;
        private UUID briefingRunId;

        private Builder(String event, String subtype, String title, String severity) {
            this.event = event;
            this.subtype = subtype;
            this.title = title;
            this.severity = severity;
        }

        /** Sets the id. */
        public Builder id(Long value) {
            this.id = value;
            return this;
        }

        /** Sets the short title. */
        public Builder shortTitle(String value) {
            this.shortTitle = value;
            return this;
        }

        /** Sets the markdown body. */
        public Builder body(String value) {
            this.body = value;
            return this;
        }

        /** Sets the API link. */
        public Builder url(String value) {
            this.url = value;
            return this;
        }

        /** Sets the link buttons. */
        public Builder links(List<Link> value) {
            this.links = value;
            return this;
        }

        /** Sets the event data. */
        public Builder data(Map<String, Object> value) {
            this.data = value;
            return this;
        }

        /** Sets the tags. */
        public Builder tags(List<String> value) {
            this.tags = value;
            return this;
        }

        /** Sets the original of an update. */
        public Builder updateOf(Long value) {
            this.updateOf = value;
            return this;
        }

        /** Sets the origin install id. */
        public Builder origin(String value) {
            this.origin = value;
            return this;
        }

        /** Sets the creation time. */
        public Builder createdAt(Instant value) {
            this.createdAt = value;
            return this;
        }

        /** Sets the alert. */
        public Builder alertId(Long value) {
            this.alertId = value;
            return this;
        }

        /** Sets the rule. */
        public Builder ruleId(UUID value) {
            this.ruleId = value;
            return this;
        }

        /** Sets the briefing run. */
        public Builder briefingRunId(UUID value) {
            this.briefingRunId = value;
            return this;
        }

        /** The message. */
        public NotificationMessage build() {
            return new NotificationMessage(id, event, subtype, title, shortTitle, body, severity, url, links, data, tags,
                    List.of(), updateOf, origin, createdAt, alertId, ruleId, briefingRunId);
        }
    }
}
