package ai.foresignal.source.formats.cap;

import ai.foresignal.source.spi.NormalizedRecord;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * How one source maps CAP alerts to records (prompt 067): the record kind, which {@code info} block to
 * read, which parameters to keep, how long the free text may be, what else counts as a cancellation, and
 * an optional source-specific addition to the record.
 *
 * @param kind the record kind
 * @param languages preferred {@code info} languages, best first ({@code en-US}, or a primary tag such as
 *     {@code en}); empty means the first block
 * @param parameterAllowlist the {@code parameter} names copied to the payload; empty keeps none
 * @param textMaxBytes the UTF-8 cap of {@code headline}, {@code description} and {@code instruction}
 * @param cancellation a source's extra cancellation rule, or null for {@code msgType = Cancel} only
 * @param extender a source's additions to the record, or null
 */
public record CapMappingProfile(
        String kind,
        List<String> languages,
        Set<String> parameterAllowlist,
        int textMaxBytes,
        CancellationRule cancellation,
        RecordExtender extender) {

    /** The text cap of prompt 067: 4 KB. */
    public static final int DEFAULT_TEXT_MAX_BYTES = 4 * 1024;

    /** Checks the kind and copies the collections. */
    public CapMappingProfile {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("A CAP mapping profile needs a kind");
        }
        if (textMaxBytes < 16) {
            throw new IllegalArgumentException("textMaxBytes must be at least 16");
        }
        languages = languages == null ? List.of() : List.copyOf(languages);
        parameterAllowlist = parameterAllowlist == null ? Set.of() : Set.copyOf(parameterAllowlist);
    }

    /** A profile of {@code kind} with the defaults: first info block, no parameters, 4 KB text. */
    public static CapMappingProfile of(String kind) {
        return builder(kind).build();
    }

    /** A builder for a profile of {@code kind}. */
    public static Builder builder(String kind) {
        return new Builder(kind);
    }

    /** A source's additional rule for "this message cancels its thread". */
    @FunctionalInterface
    public interface CancellationRule {

        /**
         * Whether {@code alert} cancels its thread.
         *
         * @param alert the message
         * @param info the chosen info block, or null when the message has none
         */
        boolean cancels(CapAlert alert, CapAlert.Info info);
    }

    /** A source's additions to the mapped record (extra payload keys, a fallback geometry). */
    @FunctionalInterface
    public interface RecordExtender {

        /**
         * Adds to {@code record}, which already carries the shared mapping.
         *
         * @param alert the message
         * @param info the chosen info block, or null
         * @param geometry the geometry the shared mapping found, or null
         * @param record the builder; a {@code put} replaces a shared key, {@code geometry} replaces the
         *     geometry
         */
        void extend(CapAlert alert, CapAlert.Info info, Geometry geometry, NormalizedRecord.Builder record);
    }

    /** Fluent construction of a {@link CapMappingProfile}. */
    public static final class Builder {
        private final String kind;
        private List<String> languages = List.of();
        private Set<String> parameters = Set.of();
        private int textMaxBytes = DEFAULT_TEXT_MAX_BYTES;
        private CancellationRule cancellation;
        private RecordExtender extender;

        private Builder(String kind) {
            this.kind = kind;
        }

        /** Preferred languages, best first. */
        public Builder languages(String... languages) {
            this.languages = List.of(languages);
            return this;
        }

        /** The parameter names kept in the payload. */
        public Builder parameters(String... names) {
            this.parameters = Set.of(names);
            return this;
        }

        /** The text cap in UTF-8 bytes. */
        public Builder textMaxBytes(int textMaxBytes) {
            this.textMaxBytes = textMaxBytes;
            return this;
        }

        /** The source's extra cancellation rule. */
        public Builder cancellation(CancellationRule cancellation) {
            this.cancellation = cancellation;
            return this;
        }

        /** The source's additions to the record. */
        public Builder extender(RecordExtender extender) {
            this.extender = extender;
            return this;
        }

        /** The profile. */
        public CapMappingProfile build() {
            return new CapMappingProfile(kind, languages, parameters, textMaxBytes, cancellation, extender);
        }
    }
}
