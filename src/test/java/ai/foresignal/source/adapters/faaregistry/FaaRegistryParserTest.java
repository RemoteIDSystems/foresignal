package ai.foresignal.source.adapters.faaregistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

/** The FAA registry parser and its privacy guard (prompt 098). */
class FaaRegistryParserTest {

    private static final String MASTER_HEADER =
            "N-NUMBER,SERIAL NUMBER,MFR MDL CODE,ENG MFR MDL,YEAR MFR,TYPE REGISTRANT,NAME,STREET,CITY,STATE,"
                    + "ZIP CODE,COUNTRY,CERT ISSUE DATE,STATUS CODE,MODE S CODE HEX,FRACT OWNER,KIT MFR,KIT MODEL,";

    private static final String INDIVIDUAL =
            "123AB,1234,S22,CONT,1975,1,JOHN DOE,1 MAIN ST,AUSTIN,TX,78701,US,20180101,A,AB1234,N,,,";

    private static final String CORPORATION =
            "456CD,5678,S22,CONT,1980,3,ACME LLC,2 ELM ST,DALLAS,TX,75001,US,20190101,A,CD5678,N,,,";

    private static final String ACFTREF_HEADER =
            "CODE,MFR,MODEL,TYPE-ACFT,TYPE-ENG,AC-CAT,BUILD-CERT-IND,NO-ENG,NO-SEATS,AC-WEIGHT,SPEED,"
                    + "TC-DATA-SHEET,TC-DATA-HOLDER,";

    private static final String ACFTREF_ROW = "S22,CESSNA,172,4,1,1,0,1,4,CLASS 1,H,3A12,CESSNA,";

    @Test
    void parsesMasterRowsJoinedToTheReference() {
        List<NormalizedRecord> records = parse(false);
        assertThat(records).hasSize(2);
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).isEqualTo("N123AB");
        assertThat(first.kind()).isEqualTo(FaaRegistryParser.KIND);
        assertThat(first.payload()).containsEntry("nNumber", "123AB");
        assertThat(first.payload()).containsEntry("serial", "1234");
        assertThat(first.payload()).containsEntry("mfrMdlCode", "S22");
        assertThat(first.payload()).containsEntry("manufacturer", "CESSNA");
        assertThat(first.payload()).containsEntry("model", "172");
        assertThat(first.payload()).containsEntry("registrantType", "Individual");
        assertThat(first.payload()).containsEntry("modeSHex", "ab1234");
    }

    @Test
    void neverCopiesOwnerPiiColumns() {
        for (NormalizedRecord record : parse(false)) {
            for (String column : FaaRegistryParser.PII_COLUMNS) {
                assertThat(record.payload()).doesNotContainKey(column);
                assertThat(record.payload()).doesNotContainKey(column.toLowerCase());
            }
            assertThat(record.payload()).doesNotContainKey("ownerName");
        }
    }

    @Test
    void nonIndividualOwnerNamesKeepsOnlyCorporateRegistrants() {
        List<NormalizedRecord> records = parse(true);
        assertThat(records.get(0).payload()).doesNotContainKey("ownerName");
        assertThat(records.get(1).payload()).containsEntry("ownerName", "ACME LLC");
    }

    @Test
    void rejectsZipSlipEntries() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("../evil.txt"));
            zip.write("x".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        assertThatThrownBy(() -> FaaRegistryParser.parse(out.toByteArray(), false, r -> {}))
                .isInstanceOf(UpstreamFormatException.class);
    }

    private static List<NormalizedRecord> parse(boolean nonIndividualOwnerNames) {
        List<NormalizedRecord> records = new ArrayList<>();
        FaaRegistryParser.parse(archive(), nonIndividualOwnerNames, records::addAll);
        return records;
    }

    private static byte[] archive() {
        return zip(Map.of(
                "MASTER.txt", MASTER_HEADER + "\n" + INDIVIDUAL + "\n" + CORPORATION + "\n",
                "ACFTREF.txt", ACFTREF_HEADER + "\n" + ACFTREF_ROW + "\n"));
    }

    private static byte[] zip(Map<String, String> entries) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(out)) {
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
