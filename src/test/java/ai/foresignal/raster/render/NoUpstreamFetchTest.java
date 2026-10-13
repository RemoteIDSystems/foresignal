package ai.foresignal.raster.render;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.net.TestOutbound;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * No request path fetches a third-party basemap (prompt 055; Weather-Imagery-Design §0.1, §8): with the
 * operator basemap unset a map makes no outbound call at all; a public tile server such as
 * {@code tile.openstreetmap.org} is refused (and never contacted); a template without placeholders is
 * refused; only a tile server on the operator's own network is fetched, inside the request.
 */
class NoUpstreamFetchTest extends WireMockSupport {

    /** A resolver that puts {@code tile.openstreetmap.org} on a public address and {@code tiles.lan} on the LAN. */
    static OutboundUrlPolicy policy() {
        return OutboundUrlPolicy.withoutOverrides(host -> switch (host) {
            case "tile.openstreetmap.org" -> new InetAddress[] {InetAddress.getByAddress(host, new byte[] {(byte) 151,
                101, 2, (byte) 217})};
            case "tiles.lan" -> new InetAddress[] {InetAddress.getByAddress(host, new byte[] {(byte) 192, (byte) 168,
                1, 10})};
            case "localhost" -> new InetAddress[] {InetAddress.getByAddress(host, new byte[] {127, 0, 0, 1})};
            default -> throw new UnknownHostException(host);
        });
    }

    @Test
    @DisplayName("operator basemap unset: drawing a full map makes zero outbound HTTP calls")
    void unsetMakesNoCalls() {
        PinnedHttp pinned = mock(PinnedHttp.class);
        OutboundUrlPolicy policy = mock(OutboundUrlPolicy.class);
        OperatorBasemap operator = new OperatorBasemap("", policy, pinned);
        assertThat(operator.configured()).isFalse();
        MapRenderer renderer = new MapRenderer(new BasemapStore(), new MapFixtures.BandRenderer(), operator, null);
        assertThat(renderer.draw(MapFixtures.full(false)).png()).isNotEmpty();
        verifyNoInteractions(pinned, policy);
        WIREMOCK.verify(0, getRequestedFor(urlMatching(".*")));
    }

    @Test
    @DisplayName("a public basemap URL is rejected, never contacted, and the map falls back to Natural Earth")
    void publicRejected() {
        PinnedHttp pinned = mock(PinnedHttp.class);
        OperatorBasemap operator = new OperatorBasemap("https://tile.openstreetmap.org/{z}/{x}/{y}.png", policy(),
                pinned);
        assertThat(operator.problem("https://tile.openstreetmap.org/{z}/{x}/{y}.png")).get().asString()
                .contains("not on the local network");
        BufferedImage canvas = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        assertThat(operator.draw(canvas.createGraphics(), -98, 29, -97, 30, 64, 64, 0)).isFalse();
        MapRenderer renderer = new MapRenderer(new BasemapStore(), new MapFixtures.BandRenderer(), operator, null);
        assertThat(renderer.draw(MapFixtures.full(false)).png()).isNotEmpty();
        verifyNoInteractions(pinned);
    }

    @Test
    @DisplayName("a template without {z}/{x}/{y} is refused; a LAN host is accepted")
    void templateChecks() {
        OperatorBasemap operator = new OperatorBasemap(null, policy(), mock(PinnedHttp.class));
        assertThat(operator.problem("http://tiles.lan/tile.png")).isPresent();
        assertThat(operator.problem("http://tiles.lan/{z}/{x}/{y}.png")).isEmpty();
        assertThat(OperatorBasemap.fill("http://t/{z}/{x}/{y}.png", 7, 29, 53)).isEqualTo("http://t/7/29/53.png");
        assertThat(OperatorBasemap.zoom(2 * WebMercator.HALF, 256)).isZero();
    }

    @Test
    @DisplayName("a LAN tile server is fetched inside the request and its tiles replace the Natural Earth fills")
    void lanTilesDrawn() throws Exception {
        BufferedImage tile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = tile.createGraphics();
        g.setColor(new java.awt.Color(0x00FF00));
        g.fillRect(0, 0, 256, 256);
        g.dispose();
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(tile, "png", png);
        WIREMOCK.stubFor(get(urlMatching("/tiles/\\d+/\\d+/\\d+\\.png")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "image/png").withBody(png.toByteArray())));
        OperatorBasemap operator = new OperatorBasemap(baseUrl() + "/tiles/{z}/{x}/{y}.png",
                TestOutbound.offlinePolicy(), TestOutbound.pinnedHttp());
        MapSpec spec = new MapSpec(MapFixtures.BOX, 400, 300, java.util.List.of(), null, null, java.util.List.of(),
                false, false, null);
        MapRenderer renderer = new MapRenderer(new BasemapStore(), new MapFixtures.BandRenderer(), operator, null);
        BufferedImage map = ImageIO.read(new ByteArrayInputStream(renderer.draw(spec).png()));
        MapRenderer.Layout layout = MapRenderer.layout(spec);
        assertThat(map.getRGB(200, layout.mapTop() + layout.mapHeight() / 2) & 0xFFFFFF).isEqualTo(0x00FF00);
        WIREMOCK.verify(getRequestedFor(urlMatching("/tiles/.*")));
    }
}
