package ai.foresignal.mapping;

import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.NormalizedRecord;
import java.util.List;

/**
 * What {@link MappingEngine#apply} made of one document (prompt 059).
 *
 * @param records the records, in item order
 * @param indexes the item index of each record, parallel to {@code records}
 * @param rejections the items that produced no record because of an error
 * @param warnings non-fatal findings, at most {@link MappingEngine#MAX_WARNINGS}, without repeats
 * @param skipped items left out by {@code filter}
 * @param items how many items the document held
 */
public record MappingResult(List<NormalizedRecord> records, List<Integer> indexes, List<ItemRejection> rejections,
        List<MappingWarning> warnings, int skipped, int items) {

    /** Copies the lists and checks they line up. */
    public MappingResult {
        records = List.copyOf(records);
        indexes = List.copyOf(indexes);
        rejections = List.copyOf(rejections);
        warnings = List.copyOf(warnings);
        if (records.size() != indexes.size()) {
            throw new IllegalArgumentException("records and indexes must have the same size");
        }
    }

    /** This result as the push pipeline's {@link ParsedIngest}. */
    public ParsedIngest toParsedIngest() {
        return new ParsedIngest(records, indexes, rejections, items);
    }
}
