package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link ResourceThresholdsService} (prompt R2-014): parsing, validation and the stored round trip. */
class ResourceThresholdsServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static JsonNode node(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    private static ValidationException invalid(String body) {
        try {
            ResourceThresholdsService.parse(node(body));
        } catch (ValidationException e) {
            return e;
        }
        throw new AssertionError("expected a 400 for " + body);
    }

    @Test
    @DisplayName("an empty body is the defaults; null turns a member off")
    void defaultsAndOff() {
        assertThat(ResourceThresholdsService.parse(node("{}"))).isEqualTo(ResourceThresholds.defaults());
        ResourceThresholds off = ResourceThresholdsService.parse(node(
                "{'disk':null,'memory':null,'sourceStorm':null,'collectorBacklog':null}"));
        assertThat(off.disk().off()).isTrue();
        assertThat(off.memory().off()).isTrue();
        assertThat(off.sourceStorm().failedSources()).isNull();
        assertThat(off.collectorBacklog().triggerLagSeq()).isNull();
        assertThat(off.collectorBacklog().waitingPulls()).isNull();
    }

    @Test
    @DisplayName("a full body parses")
    void parses() {
        String id = UUID.randomUUID().toString();
        ResourceThresholds t = ResourceThresholdsService.parse(node("{'disk':{'warnFreePercent':20,"
                + "'criticalFreeBytes':5000000000},'memory':{'warnFreePercent':10,'criticalFreePercent':2.5},"
                + "'alertChannels':['" + id + "','" + id.toUpperCase() + "'],'sourceStorm':{'failedSources':3},"
                + "'collectorBacklog':{'triggerLagSeq':500,'waitingPulls':20,'sustainedSeconds':120}}"));
        assertThat(t.disk()).isEqualTo(new ResourceThresholds.Threshold(20.0, null, null, 5_000_000_000L));
        assertThat(t.memory().criticalFreePercent()).isEqualTo(2.5);
        assertThat(t.alertChannels()).as("normalized and de-duplicated").containsExactly(id);
        assertThat(t.sourceStorm().failedSources()).isEqualTo(3);
        assertThat(t.collectorBacklog()).isEqualTo(new ResourceThresholds.CollectorBacklog(500L, 20, 120));
    }

    @Test
    @DisplayName("critical must be below warn; percents 0-100; bytes and counts whole; unknown members refused")
    void validation() {
        assertThat(invalid("{'disk':{'warnFreePercent':5,'criticalFreePercent':10}}").getErrors())
                .extracting(e -> e.field()).containsExactly("disk.criticalFreePercent");
        assertThat(invalid("{'disk':{'warnFreeBytes':100,'criticalFreeBytes':100}}").getErrors())
                .extracting(e -> e.field()).containsExactly("disk.criticalFreeBytes");
        invalid("{'disk':{'warnFreePercent':101}}");
        invalid("{'memory':{'criticalFreePercent':-1}}");
        invalid("{'disk':{'warnFreeBytes':-5}}");
        invalid("{'disk':{'warnFreeBytes':1.5}}");
        invalid("{'disk':{'warnFreeGb':5}}");
        invalid("{'disk':'low'}");
        invalid("{'alertChannels':['not-a-uuid']}");
        invalid("{'alertChannels':'all'}");
        invalid("{'sourceStorm':{'failedSources':0}}");
        invalid("{'collectorBacklog':{'sustainedSeconds':10}}");
        invalid("{'collectorBacklog':{'sustainedSeconds':100000}}");
        invalid("{'cpu':{}}");
        assertThatThrownBy(() -> ResourceThresholdsService.parse(node("[]"))).isInstanceOf(ValidationException.class);
        assertThat(invalid("{'disk':{'warnFreePercent':200},'memory':{'warnFreePercent':-1},'cpu':1}").getErrors())
                .as("every problem in one 400").hasSize(3);
    }

    @Test
    @DisplayName("unknown alert channels are one 400 listing them, and nothing is stored")
    void unknownChannels() {
        AppSettingService settings = mock(AppSettingService.class);
        NotificationChannelRepository channels = mock(NotificationChannelRepository.class);
        UUID known = UUID.randomUUID();
        UUID missing1 = UUID.randomUUID();
        UUID missing2 = UUID.randomUUID();
        NotificationChannelEntity entity = mock(NotificationChannelEntity.class);
        when(entity.getId()).thenReturn(known);
        when(channels.findAllById(any())).thenReturn(List.of(entity));
        ResourceThresholdsService service = new ResourceThresholdsService(settings, channels);
        ResourceThresholds d = ResourceThresholds.defaults();
        ResourceThresholds value = new ResourceThresholds(d.disk(), d.memory(),
                List.of(known.toString(), missing1.toString(), missing2.toString()), d.sourceStorm(),
                d.collectorBacklog());

        assertThatThrownBy(() -> service.put(value)).isInstanceOfSatisfying(ValidationException.class, e -> {
            assertThat(e.getErrors()).hasSize(1);
            assertThat(e.getErrors().get(0).message()).contains(missing1.toString()).contains(missing2.toString())
                    .doesNotContain(known.toString());
        });
        verify(settings, never()).put(any(), any());
    }

    @Test
    @DisplayName("a stored value reads back equal through JSON, and put stores under the thresholds key")
    void roundTrip() {
        ResourceThresholds value = ResourceThresholdsService.parse(node(
                "{'disk':{'warnFreePercent':20,'criticalFreePercent':4},'memory':{'warnFreeBytes':1000},"
                        + "'collectorBacklog':{'waitingPulls':9}}"));
        String stored = JSON.writeValueAsString(value);
        assertThat(JSON.readValue(stored, ResourceThresholds.class)).isEqualTo(value);

        AppSettingService settings = mock(AppSettingService.class);
        NotificationChannelRepository channels = mock(NotificationChannelRepository.class);
        when(channels.findAllById(any())).thenReturn(List.of());
        new ResourceThresholdsService(settings, channels).put(value);
        verify(settings).put(eq(SettingKeys.THRESHOLDS), eq(value));
    }
}
