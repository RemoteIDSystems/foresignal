package ai.foresignal.inbound.format;

import ai.foresignal.source.spi.NormalizedRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * What an {@link IngestFormat} made of one request.
 *
 * @param records the records, in request order
 * @param indexes the request item index of each record, parallel to {@code records}; several
 *     records may share an index (a CAP {@code Update} yields a record and tombstones)
 * @param rejections the items refused by the format
 * @param items how many items the request carried
 */
public record ParsedIngest(
        List<NormalizedRecord> records, List<Integer> indexes, List<ItemRejection> rejections, int items) {

    /** Copies the lists and checks they line up. */
    public ParsedIngest {
        records = records == null ? List.of() : List.copyOf(records);
        indexes = indexes == null ? List.of() : List.copyOf(indexes);
        rejections = rejections == null ? List.of() : List.copyOf(rejections);
        if (records.size() != indexes.size()) {
            throw new IllegalArgumentException("records and indexes must have the same size");
        }
    }

    /** A request with no items. */
    public static ParsedIngest empty() {
        return new ParsedIngest(List.of(), List.of(), List.of(), 0);
    }

    /** A fresh builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Accumulates records and rejections in request order. Not thread-safe. */
    public static final class Builder {

        private final List<NormalizedRecord> records = new ArrayList<>();
        private final List<Integer> indexes = new ArrayList<>();
        private final List<ItemRejection> rejections = new ArrayList<>();
        private int items;

        private Builder() {}

        /** Adds a record that came from item {@code index}. */
        public Builder add(int index, NormalizedRecord record) {
            records.add(record);
            indexes.add(index);
            items = Math.max(items, index + 1);
            return this;
        }

        /** Refuses item {@code index}. */
        public Builder reject(int index, String externalId, String reason, String detail) {
            rejections.add(new ItemRejection(index, externalId, reason, detail));
            items = Math.max(items, index + 1);
            return this;
        }

        /** Sets the item count explicitly, for items that produced neither a record nor a rejection. */
        public Builder items(int count) {
            items = Math.max(items, count);
            return this;
        }

        /** The records added so far. */
        public int recordCount() {
            return records.size();
        }

        /** The result. */
        public ParsedIngest build() {
            return new ParsedIngest(records, indexes, rejections, items);
        }
    }
}
