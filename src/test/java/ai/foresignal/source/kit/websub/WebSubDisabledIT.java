package ai.foresignal.source.kit.websub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedKitFamily;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import ai.foresignal.test.IntegrationTest;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * WebSub's default (prompt 073): with {@code foresignal.kits.websub.enabled=false} no subscription is ever attempted
 * and both callbacks answer 404, even for a well-formed request and without a token (the paths are open).
 */
@IntegrationTest
class WebSubDisabledIT {

    @Autowired MockMvc mvc;
    @Autowired WebSubService websub;
    @Autowired FeedKitFamily feeds;
    @Autowired AppSettingService settings;
    @Autowired Clock clock;

    @Test
    @DisplayName("disabled by default: the callbacks are 404 and nothing is subscribed")
    void disabled() throws Exception {
        assertThat(websub.enabled()).isFalse();
        String path = "/v1/websub/feed-nhc-atlantic/" + "A".repeat(43);
        mvc.perform(get(path).param("hub.mode", "subscribe").param("hub.topic", "https://www.nhc.noaa.gov/index-at.xml")
                .param("hub.challenge", "c")).andExpect(status().isNotFound());
        mvc.perform(post(path).contentType("application/rss+xml").content("<rss/>").header("X-Hub-Signature",
                "sha256=00")).andExpect(status().isNotFound());

        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example", null, null, null, null));
        FeedSourceSpec s = feeds.spec("feed-nhc-atlantic");
        FeedSourceSpec wanting = new FeedSourceSpec(s.id(), s.name(), s.description(), s.url(), s.kind(),
                s.language(), s.categories(), s.maxItems(), s.recordTtl(), s.ttlAnchor(), s.observedAt(), s.interval(),
                s.minInterval(), s.budget(), s.license(), s.termsUrl(), s.attribution(), s.homepage(), s.itemMapper(),
                Boolean.TRUE, s.retention(), s.maxResponseBytes());
        StubHubContext ctx = new StubHubContext(s.id(), null, clock);
        websub.afterPoll(ctx, wanting, new FeedDocument(FeedDocument.RSS2, "t", null,
                "https://www.nhc.noaa.gov/index-at.xml", List.of("https://8.8.8.8/hub"), null, List.of(), List.of()));
        websub.maintain(ctx, wanting);
        assertThat(ctx.requests).isEmpty();
        assertThat(websub.subscription(s.id())).isEmpty();
        assertThat(websub.active(s.id(), clock.instant())).isFalse();
    }
}
