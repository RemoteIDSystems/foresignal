package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** IEC 61162-450 TAG blocks (prompt 085): checksum, {@code c} in seconds and milliseconds, groups, none at all. */
class TagBlockTest {

    private static final String SENTENCE = "!AIVDM,1,1,,A,13m=TNPtAsPh4;0RAV`8h6g22000,0*6B";

    @Test
    @DisplayName("a Kystverket-style block: s and c (seconds) are read and the sentence is split off")
    void seconds() {
        TagBlock.Split split = TagBlock.strip("\\s:2573485,c:1791900000*01\\" + SENTENCE);

        assertThat(split.sentence()).isEqualTo(SENTENCE);
        assertThat(split.tag().source()).isEqualTo("2573485");
        assertThat(split.tag().time()).isEqualTo(Instant.ofEpochSecond(1_791_900_000L));
        assertThat(split.tag().fields()).containsEntry("s", "2573485").containsEntry("c", "1791900000");
        assertThat(split.tag().group()).isNull();
    }

    @Test
    @DisplayName("c with 13 digits is milliseconds")
    void millis() {
        String block = TagBlock.format(Map.of("c", "1791900000250"));
        TagBlock tag = TagBlock.strip(block + SENTENCE).tag();
        assertThat(tag.time()).isEqualTo(Instant.ofEpochMilli(1_791_900_000_250L));
    }

    @Test
    @DisplayName("g and n are parsed; format computes the checksum strip verifies")
    void groupAndCount() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("g", "2-3-1234");
        fields.put("n", "17");
        fields.put("s", "station");
        TagBlock tag = TagBlock.strip(TagBlock.format(fields) + SENTENCE).tag();

        assertThat(tag.group()).isEqualTo(new TagBlock.Group(2, 3, "1234"));
        assertThat(tag.lineCount()).isEqualTo(17L);
        assertThat(tag.time()).isNull();
    }

    @Test
    @DisplayName("a wrong checksum is refused as a bad checksum, a missing one as malformed")
    void checksum() {
        assertThatThrownBy(() -> TagBlock.strip("\\s:2573485,c:1791900000*02\\" + SENTENCE))
                .isInstanceOfSatisfying(TagBlock.TagBlockException.class, e -> assertThat(e.badChecksum()).isTrue());
        assertThatThrownBy(() -> TagBlock.strip("\\s:2573485,c:1791900000\\" + SENTENCE))
                .isInstanceOfSatisfying(TagBlock.TagBlockException.class, e -> assertThat(e.badChecksum()).isFalse());
        assertThatThrownBy(() -> TagBlock.strip("\\s:2573485" + SENTENCE))
                .isInstanceOf(TagBlock.TagBlockException.class);
    }

    @Test
    @DisplayName("a malformed parameter or group is refused")
    void malformed() {
        assertThatThrownBy(() -> TagBlock.strip(TagBlock.format(Map.of("g", "1-2")) + SENTENCE))
                .isInstanceOf(TagBlock.TagBlockException.class);
        assertThatThrownBy(() -> TagBlock.strip(TagBlock.format(Map.of("c", "soon")) + SENTENCE))
                .isInstanceOf(TagBlock.TagBlockException.class);
    }

    @Test
    @DisplayName("a line without a TAG block is not an error")
    void missing() {
        TagBlock.Split split = TagBlock.strip(SENTENCE);
        assertThat(split.tag()).isNull();
        assertThat(split.sentence()).isEqualTo(SENTENCE);
        assertThat(TagBlock.strip(null).sentence()).isEmpty();
    }
}
