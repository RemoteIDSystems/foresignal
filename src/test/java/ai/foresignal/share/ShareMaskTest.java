package ai.foresignal.share;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShareMaskTest {

    static Map<String, Object> payload() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("title", "Home sensor");
        p.put("kind", "air-quality");
        p.put("observedAt", "2026-10-16T12:00:00Z");
        p.put("pm25", 12.5);
        p.put("observer", Map.of("id", "me"));
        p.put("deviceId", "abc");
        p.put("streetAddress", "1 Main St");
        p.put("homeLat", 30.1);
        return p;
    }

    @Test
    @DisplayName("the privacy mask keeps only title, kind and observedAt")
    void masked() {
        assertThat(ShareMask.payload(payload(), true, null)).containsOnlyKeys("title", "kind", "observedAt");
    }

    @Test
    @DisplayName("an explicit allowlist keeps its keys, but never personal fields under the mask")
    void allowlist() {
        assertThat(ShareMask.payload(payload(), true, List.of("pm25", "deviceId", "title")))
                .containsOnlyKeys("pm25", "title");
        assertThat(ShareMask.payload(payload(), false, List.of("pm25", "deviceId"))).containsOnlyKeys("pm25", "deviceId");
    }

    @Test
    @DisplayName("without mask or allowlist the payload is unchanged")
    void unmasked() {
        assertThat(ShareMask.payload(payload(), false, null)).isEqualTo(payload());
        assertThat(ShareMask.payload(null, true, null)).isEmpty();
    }

    @Test
    @DisplayName("coordinates snap to a grid of the precision in meters, also inside lines and collections")
    void precision() {
        Map<String, Object> point = Map.of("type", "Point", "coordinates", List.of(-97.74321, 30.26789, 150.0));
        Map<String, Object> snapped = ShareMask.geometry(point, 1000);
        @SuppressWarnings("unchecked")
        List<Object> c = (List<Object>) snapped.get("coordinates");
        double step = 1000 / 111_320.0;
        assertThat(Math.abs((Double) c.get(0) - -97.74321)).isLessThanOrEqualTo(step / 2 + 1e-6);
        assertThat(Math.abs((Double) c.get(1) - 30.26789)).isLessThanOrEqualTo(step / 2 + 1e-6);
        assertThat(c.get(2)).isEqualTo(150.0);
        assertThat(ShareMask.snap(30.26789, 1000)).isEqualTo(ShareMask.snap(30.2681, 1000));
        Map<String, Object> line = Map.of("type", "LineString", "coordinates", List.of(List.of(0.001, 0.001),
                List.of(0.002, 0.002)));
        assertThat(ShareMask.geometry(line, 1000).get("coordinates")).isEqualTo(List.of(List.of(0.0, 0.0),
                List.of(0.0, 0.0)));
        Map<String, Object> collection = Map.of("type", "GeometryCollection", "geometries", List.of(point));
        assertThat(ShareMask.geometry(collection, 1000).get("geometries")).isEqualTo(List.of(snapped));
        assertThat(ShareMask.geometry(point, null)).isSameAs(point);
    }

    @Test
    @DisplayName("observer, device and address-like keys are personal")
    void personal() {
        assertThat(ShareMask.personal("observer")).isTrue();
        assertThat(ShareMask.personal("deviceName")).isTrue();
        assertThat(ShareMask.personal("ipAddress")).isTrue();
        assertThat(ShareMask.personal("postcode")).isTrue();
        assertThat(ShareMask.personal("mag")).isFalse();
        assertThat(ShareMask.personal(null)).isFalse();
    }
}
