package ai.foresignal.plugin;

import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.NormalizedRecord;
import java.util.List;
import java.util.Objects;

/**
 * A parser plugin's answer, mapped to records (prompt 141).
 *
 * @param parsed the records and per-item rejections, mapped by the {@code native} ingest format's record mapper
 * @param complete the plugin's {@code complete} flag: true when the records are the upstream's whole current set
 */
public record ParserOutput(ParsedIngest parsed, boolean complete) {

    /** Checks the parse result is present. */
    public ParserOutput {
        Objects.requireNonNull(parsed);
    }

    /** The records, in output order. */
    public List<NormalizedRecord> records() {
        return parsed.records();
    }

    /** The items that did not become records. */
    public List<ItemRejection> rejections() {
        return parsed.rejections();
    }
}
