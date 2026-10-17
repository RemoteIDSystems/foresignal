package ai.foresignal.outlook;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The record identities of the DERIVED source {@code foresignal-outlook} (prompt 126): kind
 * {@value #OUTLOOK_KIND} with {@code externalId = {region}:{family}:{windowStart}}, and kind {@value #INDEX_KIND} with
 * {@code externalId = {indexId}:{region}}; and their object references ({@code record:{sourceId}/{externalId}}, the
 * external id percent-encoded).
 */
public final class OutlookRecords {

    /** The source id. */
    public static final String SOURCE_ID = "foresignal-outlook";

    /** The kind of an outlook window record. */
    public static final String OUTLOOK_KIND = "regional-outlook";

    /** The kind of an index value record. */
    public static final String INDEX_KIND = "regional-index";

    private OutlookRecords() {}

    /** The external id of an outlook window. */
    public static String outlookExternalId(String region, String family, String windowStart) {
        return region + ":" + family + ":" + windowStart;
    }

    /** The external id of an index's region record. */
    public static String indexExternalId(String indexId, String region) {
        return indexId + ":" + region;
    }

    /** The object reference of a record of this source. */
    public static String recordRef(String externalId) {
        return "record:" + SOURCE_ID + "/" + URLEncoder.encode(externalId, StandardCharsets.UTF_8);
    }
}
