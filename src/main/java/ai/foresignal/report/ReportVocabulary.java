package ai.foresignal.report;

import java.util.List;

/**
 * The fixed vocabulary of reports (prompt 132, OpenAPI {@code Report}, {@code ReportBlock}, {@code ReportSource}).
 */
public final class ReportVocabulary {

    /** A draft being edited. */
    public static final String DRAFT = "draft";

    /** A draft under review (still editable). */
    public static final String REVIEW = "review";

    /** At least one revision is published and the latest is not retracted. */
    public static final String PUBLISHED = "published";

    /** The latest published revision was retracted. */
    public static final String RETRACTED = "retracted";

    /** Every report status. */
    public static final List<String> STATUSES = List.of(DRAFT, REVIEW, PUBLISHED, RETRACTED);

    /** The templates. */
    public static final List<String> TEMPLATES = List.of("situation-brief", "investigation-summary",
            "chronicle-recap", "weekly-assessment", "custom");

    /** The default template. */
    public static final String CUSTOM = "custom";

    /** The source types. */
    public static final List<String> SOURCE_TYPES = List.of("selection", "tag", "investigation", "chronicle",
            "situation", "workspace");

    /** The block types. */
    public static final List<String> BLOCK_TYPES = List.of("markdown", "bluf", "objects", "map", "timeline",
            "hypotheses", "forecasts", "evidence", "coverage", "attachments", "sources");

    /** Text written by a person. */
    public static final String AUTHOR = "author";

    /** Text built by the deterministic drafter. */
    public static final String DETERMINISTIC = "deterministic";

    /** The labeled narrative of the optional LLM tool loop. */
    public static final String LLM = "llm";

    /** The text origins of a block. */
    public static final List<String> TEXT_ORIGINS = List.of(AUTHOR, DETERMINISTIC, LLM);

    /** The footer of a rendition with any machine-generated block. */
    public static final String FOOTER = "Automated analysis inputs; verify before acting";

    /** The display-id prefix. */
    public static final String DISPLAY_PREFIX = "RPT-";

    /** The object-reference prefix. */
    public static final String REF_PREFIX = "report:";

    /** The most blocks a report holds. */
    public static final int MAX_BLOCKS = 200;

    /** The most refs a selection holds. */
    public static final int MAX_SELECTION = 500;

    private ReportVocabulary() {}

    /** True for a known status. */
    public static boolean isStatus(String value) {
        return value != null && STATUSES.contains(value);
    }

    /** True for an editable status ({@code draft} or {@code review}). */
    public static boolean editable(String status) {
        return DRAFT.equals(status) || REVIEW.equals(status);
    }
}
