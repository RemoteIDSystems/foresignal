package ai.foresignal.ical;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ICalWriterTest {

    static final Instant T = Instant.parse("2026-10-16T12:00:00.500Z");

    static ICalWriter.Event event(String summary, String status, Instant end) {
        return new ICalWriter.Event("launch-library:abc@inst1", 3, T, T, T, end, summary, "Line one\nLine, two; three\\",
                "LC-39A", new double[] {28.6, -80.6}, "foresignal://records/launch-library/abc", status,
                Map.of());
    }

    @Test
    @DisplayName("calendar properties, CRLF line ends and UTC times with Z")
    void calendar() {
        String ics = ICalWriter.write("1.2.3", "Foresignal launches", List.of(event("Falcon 9", "CONFIRMED",
                T.plusSeconds(3600))), false);
        assertThat(ics).startsWith("BEGIN:VCALENDAR\r\n").endsWith("END:VCALENDAR\r\n")
                .contains("PRODID:-//Foresignal//1.2.3//EN\r\n", "VERSION:2.0\r\n", "CALSCALE:GREGORIAN\r\n",
                        "METHOD:PUBLISH\r\n", "X-WR-CALNAME:Foresignal launches\r\n",
                        "REFRESH-INTERVAL;VALUE=DURATION:PT15M\r\n", "X-PUBLISHED-TTL:PT15M\r\n",
                        "DTSTART:20261016T120000Z\r\n", "DTEND:20261016T130000Z\r\n", "STATUS:CONFIRMED\r\n",
                        "GEO:28.600000;-80.600000\r\n", "LOCATION:LC-39A\r\n")
                .doesNotContain("X-FORESIGNAL-TRUNCATED");
        assertThat(ics.replace("\r\n", "")).doesNotContain("\n").doesNotContain("\r");
    }

    @Test
    @DisplayName("UID is stable and SEQUENCE is the record version")
    void uidSequence() {
        String ics = ICalWriter.write("v", "c", List.of(event("x", null, null)), false);
        assertThat(ics).contains("UID:launch-library:abc@inst1\r\n").contains("SEQUENCE:3\r\n")
                .contains("DTSTAMP:20261016T120000Z\r\n").contains("LAST-MODIFIED:20261016T120000Z\r\n")
                .doesNotContain("DTEND").doesNotContain("STATUS:");
    }

    @Test
    @DisplayName("backslash, semicolon, comma and newlines are escaped in TEXT values")
    void escaping() {
        assertThat(ICalWriter.escape("a\\b;c,d\ne\r\nf\rg")).isEqualTo("a\\\\b\\;c\\,d\\ne\\nf\\ng");
        String ics = ICalWriter.write("v", "c", List.of(event("x", null, null)), false);
        assertThat(ics).contains("DESCRIPTION:Line one\\nLine\\, two\\; three\\\\\r\n");
        assertThat(ICalWriter.escape(null)).isEmpty();
        assertThat(ICalWriter.escape("tab\there\u0001")).isEqualTo("tab\there");
    }

    @Test
    @DisplayName("lines fold at 75 octets, continuation lines start with a space")
    void folding() {
        String content = "SUMMARY:" + "x".repeat(200);
        String folded = ICalWriter.fold(content);
        for (String line : folded.split("\r\n")) {
            assertThat(line.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(75);
        }
        assertThat(folded.split("\r\n")[1]).startsWith(" ");
        assertThat(folded.replace("\r\n ", "")).isEqualTo(content);
    }

    @Test
    @DisplayName("folding never splits a UTF-8 sequence or a surrogate pair")
    void foldingMultibyte() {
        String content = "SUMMARY:" + "é".repeat(50) + "😀".repeat(30) + "日本".repeat(20);
        String folded = ICalWriter.fold(content);
        for (String line : folded.split("\r\n")) {
            byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
            assertThat(bytes.length).isLessThanOrEqualTo(75);
            assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo(line);
            assertThat(line.chars().noneMatch(c -> c == 0xFFFD)).isTrue();
        }
        assertThat(folded.replace("\r\n ", "")).isEqualTo(content);
        String[] lines = folded.split("\r\n");
        assertThat(Arrays.stream(lines).skip(1).allMatch(l -> l.startsWith(" "))).isTrue();
        assertThat(Character.isHighSurrogate(lines[1].charAt(lines[1].length() - 1))).isFalse();
    }

    @Test
    @DisplayName("canceled events, open-ended extras and the truncation flag")
    void canceledAndTruncated() {
        ICalWriter.Event open = new ICalWriter.Event("faa-tfr:1@i", 1, T, null, T, null, "TFR", null, null, null, null,
                "CANCELLED", Map.of("X-FORESIGNAL-OPEN-ENDED", "TRUE"));
        String ics = ICalWriter.write("v", "c", List.of(open), true);
        assertThat(ics).contains("STATUS:CANCELLED\r\n").contains("X-FORESIGNAL-OPEN-ENDED:TRUE\r\n")
                .contains("X-FORESIGNAL-TRUNCATED:TRUE\r\n").doesNotContain("LAST-MODIFIED");
    }

    @Test
    @DisplayName("an end not after the start is left out")
    void endNotAfterStart() {
        String ics = ICalWriter.write("v", "c", List.of(event("x", null, T)), false);
        assertThat(ics).doesNotContain("DTEND");
    }
}
