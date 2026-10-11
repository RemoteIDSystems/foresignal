package ai.elaro.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.elaro.source.spi.S3Listing;
import ai.elaro.source.spi.UpstreamFormatException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class S3ListingParserTest {

    private static final String TWO_OBJECTS =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
              <Name>noaa-mrms-pds</Name>
              <Prefix>CONUS/</Prefix>
              <IsTruncated>true</IsTruncated>
              <Contents>
                <Key>CONUS/a.grib2.gz</Key>
                <LastModified>2026-03-01T12:00:00.000Z</LastModified>
                <ETag>&quot;abc123&quot;</ETag>
                <Size>1048576</Size>
              </Contents>
              <Contents>
                <Key>CONUS/b.grib2.gz</Key>
                <LastModified>2026-03-01T12:02:00.000Z</LastModified>
                <ETag>&quot;def456&quot;</ETag>
                <Size>2097152</Size>
              </Contents>
            </ListBucketResult>
            """;

    @Test
    void parsesObjects() {
        S3Listing listing = S3ListingParser.parse(TWO_OBJECTS);

        assertThat(listing.objects()).hasSize(2);
        assertThat(listing.objects().get(0).key()).isEqualTo("CONUS/a.grib2.gz");
        assertThat(listing.objects().get(0).size()).isEqualTo(1_048_576L);
        assertThat(listing.objects().get(0).lastModified())
                .isEqualTo(Instant.parse("2026-03-01T12:00:00Z"));
    }

    @Test
    @DisplayName("the ETag's surrounding quotes are stripped")
    void stripsEtagQuotes() {
        assertThat(S3ListingParser.parse(TWO_OBJECTS).objects().get(0).etag()).isEqualTo("abc123");
    }

    @Test
    @DisplayName("a truncated page reports the key to continue after")
    void reportsNextStartAfter() {
        S3Listing listing = S3ListingParser.parse(TWO_OBJECTS);

        assertThat(listing.truncated()).isTrue();
        assertThat(listing.nextStartAfter()).isEqualTo("CONUS/b.grib2.gz");
    }

    @Test
    @DisplayName("a complete page reports no continuation")
    void completePageHasNoContinuation() {
        S3Listing listing =
                S3ListingParser.parse(
                        TWO_OBJECTS.replace(
                                "<IsTruncated>true</IsTruncated>", "<IsTruncated>false</IsTruncated>"));

        assertThat(listing.truncated()).isFalse();
        assertThat(listing.nextStartAfter()).isNull();
    }

    @Test
    void parsesAnEmptyListing() {
        S3Listing listing =
                S3ListingParser.parse(
                        "<ListBucketResult><IsTruncated>false</IsTruncated></ListBucketResult>");

        assertThat(listing.isEmpty()).isTrue();
        assertThat(listing.truncated()).isFalse();
    }

    @Test
    @DisplayName("a size that is not a number does not lose the object")
    void toleratesUnparseableSize() {
        S3Listing listing =
                S3ListingParser.parse(
                        """
                        <ListBucketResult>
                          <Contents><Key>k</Key><Size>huge</Size></Contents>
                          <IsTruncated>false</IsTruncated>
                        </ListBucketResult>
                        """);

        assertThat(listing.objects()).hasSize(1);
        assertThat(listing.objects().get(0).size()).isZero();
    }

    @Test
    @DisplayName("an external entity is never expanded")
    void refusesExternalEntities() {
        // An XML parser with DTDs enabled turns a remote response into a file-read primitive. The
        // parser disables them, so this is rejected outright rather than resolved.
        String xxe =
                """
                <?xml version="1.0"?>
                <!DOCTYPE r [<!ENTITY secret SYSTEM "file:///etc/passwd">]>
                <ListBucketResult>
                  <Contents><Key>&secret;</Key><Size>1</Size></Contents>
                  <IsTruncated>false</IsTruncated>
                </ListBucketResult>
                """;

        assertThatThrownBy(() -> S3ListingParser.parse(xxe))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a billion-laughs expansion is refused for the same reason")
    void refusesEntityExpansion() {
        String bomb =
                """
                <?xml version="1.0"?>
                <!DOCTYPE r [
                  <!ENTITY a "aaaaaaaaaa">
                  <!ENTITY b "&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;">
                ]>
                <ListBucketResult><Contents><Key>&b;</Key></Contents></ListBucketResult>
                """;

        assertThatThrownBy(() -> S3ListingParser.parse(bomb))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "not xml", "<unclosed>"})
    @DisplayName("unparseable XML is reported as an upstream format problem")
    void rejectsGarbage(String xml) {
        assertThatThrownBy(() -> S3ListingParser.parse(xml))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> S3ListingParser.parse(null))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
