package ai.foresignal.source.formats.cap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CapParser} against the CAP conformance set and the captured tsunami.gov documents (prompt 067). */
class CapParserTest {

    private static CapAlert fixture(String name) {
        return CapParser.parse(AdapterFixtures.bytes("cap", name));
    }

    @Test
    @DisplayName("the header, info, parameters and areas of a CAP 1.2 alert are read, times as OffsetDateTime")
    void polygonAlert() {
        CapAlert alert = fixture("polygon.xml");

        assertThat(alert.capVersion()).isEqualTo("1.2");
        assertThat(alert.identifier()).isEqualTo("POLY-1");
        assertThat(alert.sender()).isEqualTo("alerts@example.org");
        assertThat(alert.sent()).isEqualTo(OffsetDateTime.of(2026, 10, 13, 12, 0, 0, 0, ZoneOffset.UTC));
        assertThat(alert.status()).isEqualTo("Actual");
        assertThat(alert.msgType()).isEqualTo("Alert");
        assertThat(alert.scope()).isEqualTo("Public");
        assertThat(alert.references()).isEmpty();
        CapAlert.Info info = alert.firstInfo();
        assertThat(info.language()).as("CAP default language").isEqualTo("en-US");
        assertThat(info.categories()).containsExactly("Met");
        assertThat(info.event()).isEqualTo("Flood Warning");
        assertThat(info.effective()).isEqualTo(OffsetDateTime.parse("2026-10-13T12:00:00Z"));
        assertThat(info.onset()).isEqualTo(OffsetDateTime.parse("2026-10-13T13:00:00Z"));
        assertThat(info.expires()).isEqualTo(OffsetDateTime.parse("2026-10-13T18:00:00Z"));
        assertThat(info.headline()).isEqualTo("Flood Warning for the valley");
        assertThat(info.parameter("VTEC")).containsExactly("/O.NEW.KEWX.FL.W.0001.261013T1300Z-261013T1800Z/");
        assertThat(info.areas()).hasSize(2);
        assertThat(info.areas().get(0).polygons()).hasSize(1);
        assertThat(info.areas().get(0).geocodes()).containsExactly(new CapAlert.NamedValue("SAME", "048453"));
    }

    @Test
    @DisplayName("circles and geocode-only areas keep their raw text and codes")
    void circleAndGeocodes() {
        CapAlert circle = fixture("circle.xml");
        assertThat(circle.firstInfo().areas().get(0).circles()).containsExactly("32.9525,-115.5527 10");
        assertThat(circle.firstInfo().areas().get(1).circles()).containsExactly("32.9525,-115.5527 0");

        CapAlert geocodes = fixture("geocode-only.xml");
        CapAlert.Area area = geocodes.firstInfo().areas().get(0);
        assertThat(area.polygons()).isEmpty();
        assertThat(area.circles()).isEmpty();
        assertThat(area.geocodes()).extracting(CapAlert.NamedValue::value).containsExactly("COZ031", "COZ033", "008049");
    }

    @Test
    @DisplayName("several info blocks keep their order and languages")
    void multiLanguage() {
        CapAlert alert = fixture("multi-language.xml");

        assertThat(alert.infos()).extracting(CapAlert.Info::language).containsExactly("fr-CA", "en-CA");
    }

    @Test
    @DisplayName("references are parsed into sender, identifier and sent; a malformed triple is skipped")
    void references() {
        CapAlert alert = fixture("references.xml");

        assertThat(alert.references()).hasSize(2);
        assertThat(alert.references().get(0)).isEqualTo(new CapAlert.Reference("alerts@example.org", "REF-2",
                OffsetDateTime.parse("2026-10-13T11:30:00Z")));
        assertThat(alert.references().get(1).key()).isEqualTo("alerts@example.org:REF-1");
        assertThat(CapParser.references("a,b,not-a-time")).singleElement()
                .satisfies(r -> assertThat(r.sent()).isNull());
        assertThat(CapParser.references("  ")).isEmpty();
    }

    @Test
    @DisplayName("a Cancel without info parses with no info block")
    void cancelWithoutInfo() {
        CapAlert alert = fixture("cancel-no-info.xml");

        assertThat(alert.msgType()).isEqualTo("Cancel");
        assertThat(alert.infos()).isEmpty();
        assertThat(alert.firstInfo()).isNull();
        assertThat(alert.references()).singleElement().satisfies(r -> assertThat(r.identifier()).isEqualTo("POLY-1"));
    }

    @Test
    @DisplayName("XXE and entity-expansion documents are refused before anything is resolved")
    void xxeRejected() {
        assertThatThrownBy(() -> fixture("xxe.xml")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> fixture("billion-laughs.xml")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapParser.rootElementName(AdapterFixtures.bytes("cap", "xxe.xml")))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a 3 MB alert is refused at the 2 MB cap; a smaller cap applies when given")
    void sizeCap() {
        String padding = "x".repeat(3 * 1024 * 1024);
        String big = new String(AdapterFixtures.bytes("cap", "polygon.xml"), StandardCharsets.UTF_8)
                .replace("River flooding is expected.", padding);
        byte[] bytes = big.getBytes(StandardCharsets.UTF_8);
        assertThat(bytes.length).isGreaterThan(3 * 1024 * 1024);

        assertThatThrownBy(() -> CapParser.parse(bytes)).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("exceeds");
        byte[] small = AdapterFixtures.bytes("cap", "polygon.xml");
        assertThatThrownBy(() -> CapParser.parse(new ByteArrayInputStream(small), new CapParseOptions(100, 1000)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapParser.parse(new ByteArrayInputStream(small), new CapParseOptions(1_000_000, 5)))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("elements");
    }

    @Test
    @DisplayName("CAP 1.1 is accepted with its version recorded; CAP 1.0 and un-namespaced alerts are refused")
    void versions() {
        assertThat(fixture("cap11.xml").capVersion()).isEqualTo("1.1");
        assertThatThrownBy(() -> fixture("cap10.xml")).isInstanceOf(UpstreamFormatException.class);
        String bare = "<alert><identifier>x</identifier><sender>s</sender><sent>2026-10-13T12:00:00Z</sent>"
                + "<status>Actual</status><msgType>Alert</msgType><scope>Public</scope></alert>";
        assertThatThrownBy(() -> CapParser.parse(bare.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        String feed = "<feed xmlns=\"" + CapParser.NAMESPACE_1_2 + "\"/>";
        assertThatThrownBy(() -> CapParser.parse(feed.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a missing required element, an unreadable time, empty and malformed input are refused")
    void requiredElements() {
        assertThatThrownBy(() -> fixture("missing-sender.xml")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("sender");
        String badTime = new String(AdapterFixtures.bytes("cap", "cap11.xml"), StandardCharsets.UTF_8)
                .replace("2026-10-13T12:00:00-00:00", "yesterday");
        assertThatThrownBy(() -> CapParser.parse(badTime.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapParser.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CapParser.parse("<alert".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the captured tsunami.gov alerts parse; the enveloped XML Signature is skipped")
    void capturedTsunamiAlerts() {
        CapAlert paaq = CapParser.parse(AdapterFixtures.bytes("tsunami-warnings", "PAAQCAP.xml"));
        assertThat(paaq.identifier()).isEqualTo("PAAQ-1-tmuiel");
        assertThat(paaq.incidents()).isEqualTo("tmuiel");
        assertThat(paaq.codes()).containsExactly("IPAWSv1.0");
        assertThat(paaq.source()).isEqualTo("NTWC");
        assertThat(paaq.firstInfo().onset()).isNull();
        assertThat(paaq.firstInfo().resources()).hasSize(3);
        assertThat(paaq.firstInfo().parameter("EventLatLon")).containsExactly("-0.200,-19.200 0.000");

        CapAlert pheb = CapParser.parse(AdapterFixtures.bytes("tsunami-warnings", "PHEBCAP.xml"));
        assertThat(pheb.identifier()).isEqualTo("PHEB-1-26286001");
        assertThat(pheb.firstInfo().headline()).as("a blank headline is null").isNull();
        assertThat(pheb.firstInfo().parameter("NWSUGC")).singleElement().asString().contains("PRZ001>003");
        assertThat(CapParser.rootElementName(AdapterFixtures.bytes("tsunami-warnings", "PHEBAtom.xml"))).isEqualTo("feed");
    }
}
