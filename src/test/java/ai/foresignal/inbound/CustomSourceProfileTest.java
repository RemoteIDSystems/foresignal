package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.inbound.format.NativeFormat;
import ai.foresignal.inbound.signature.NoneVerifier;
import ai.foresignal.inbound.signature.SignatureVerifiers;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code CustomSourceInput} members of prompt 039: {@code profile: personal-location},
 * {@code allowPathKey} and {@code ingestAllowCidrs}, and the descriptor the profile produces.
 */
class CustomSourceProfileTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    final CustomSourceInputReader reader = new CustomSourceInputReader(
            new IngestFormatRegistry(List.of(new NativeFormat(JSON, new GeoJsonIngestMapper(JSON)))),
            new SignatureVerifiers(List.of(new NoneVerifier())), JSON);

    CustomSourceDefinition read(String json) {
        return reader.read(JSON.readTree(json.replace('\'', '"')), null, Set.of());
    }

    @Test
    @DisplayName("personal-location fills in private, kinds, OBSERVED anchor, 24h TTL, significant fields and tracker field specs")
    void personalLocationDefaults() {
        CustomSourceDefinition d = read("{'id':'custom-family','name':'Family','profile':'personal-location'}");
        assertThat(d.profile()).isEqualTo("personal-location");
        assertThat(d.privateSource()).isTrue();
        assertThat(d.kinds()).containsExactly("tracker", "tracker-transition");
        assertThat(d.observedAtMode()).isEqualTo("UPSTREAM");
        assertThat(d.ttlAnchor()).isEqualTo("OBSERVED");
        assertThat(d.recordTtl()).isEqualTo("24h");
        assertThat(d.significantFields()).containsExactly("motion", "charging");
        assertThat(d.embedFields()).isEmpty();
        assertThat(d.fieldSpecs()).containsKeys("speedMs", "courseDeg", "altitudeM", "battery", "charging", "motion");
        assertThat(d.fieldSpecs().get("speedMs").unit()).isEqualTo("m/s");
        assertThat(d.fieldSpecs().get("courseDeg").unit()).isEqualTo("deg");
        assertThat(d.fieldSpecs().get("altitudeM").unit()).isEqualTo("m");
        assertThat(d.fieldSpecs().get("battery").unit()).isEqualTo("%");
    }

    @Test
    @DisplayName("explicit input wins over the profile, except private, which cannot be unset")
    void explicitInputWins() {
        CustomSourceDefinition d = read("{'id':'custom-fam2','name':'F','profile':'personal-location',"
                + "'kinds':['tracker'],'recordTtl':'2h','ttlAnchor':'RECEIVED','significantFields':['motion'],"
                + "'fieldSpecs':{'battery':{'type':'integer','unit':'%'}}}");
        assertThat(d.kinds()).containsExactly("tracker");
        assertThat(d.recordTtl()).isEqualTo("2h");
        assertThat(d.ttlAnchor()).isEqualTo("RECEIVED");
        assertThat(d.significantFields()).containsExactly("motion");
        assertThat(d.fieldSpecs().get("battery").type().name()).isEqualTo("INTEGER");

        assertThatThrownBy(() -> read("{'id':'custom-fam3','name':'F','profile':'personal-location','private':false}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).contains("private"));
        assertThatThrownBy(() -> read("{'id':'custom-fam4','name':'F','profile':'personal-location','kinds':['car']}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).contains("kinds"));
        assertThatThrownBy(() -> read("{'id':'custom-fam5','name':'F','profile':'personal-location','embedFields':['name']}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).contains("embedFields"));
        assertThatThrownBy(() -> read("{'id':'custom-fam6','name':'F','kinds':['a'],'profile':'other'}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).contains("profile"));
    }

    @Test
    @DisplayName("allowPathKey and ingestAllowCidrs are accepted, CIDRs normalized, bad blocks refused")
    void pathKeyAndCidrs() {
        CustomSourceDefinition plain = read("{'id':'custom-p1','name':'P','kinds':['a']}");
        assertThat(plain.allowPathKey()).isFalse();
        assertThat(plain.ingestAllowCidrs()).isNull();
        assertThat(plain.profile()).isNull();

        CustomSourceDefinition d = read("{'id':'custom-p2','name':'P','kinds':['a'],'allowPathKey':true,"
                + "'ingestAllowCidrs':['203.0.113.7/24','2001:db8::/32']}");
        assertThat(d.allowPathKey()).isTrue();
        assertThat(d.ingestAllowCidrs()).containsExactly("203.0.113.0/24", "2001:db8:0:0:0:0:0:0/32");

        assertThatThrownBy(() -> read("{'id':'custom-p3','name':'P','kinds':['a'],'ingestAllowCidrs':['nope']}"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(FieldError::field).contains("ingestAllowCidrs[0]"));
        assertThatThrownBy(() -> read("{'id':'custom-p4','name':'P','kinds':['a'],'allowPathKey':'yes'}"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the profile's descriptor has SAMPLED 30s/10min/50m, 7-day retention, STANDARD tier and is private")
    void descriptor() {
        CustomSourceEntity row = new CustomSourceEntity("custom-family");
        row.setName("Family");
        row.setKinds(List.of("tracker", "tracker-transition"));
        row.setRecordTtl("24h");
        row.setTtlAnchor("OBSERVED");
        row.setObservedAtMode("UPSTREAM");
        row.setSignificantFields(List.of("motion", "charging"));
        row.setPrivateSource(true);
        row.setRateLimitPerMinute(60);
        row.setProfile("personal-location");
        SourceDescriptor descriptor = CustomSourceDescriptors.toDescriptor(row, JSON);
        assertThat(descriptor.privateSource()).isTrue();
        assertThat(descriptor.historyTier().name()).isEqualTo("STANDARD");
        assertThat(descriptor.defaultRetention()).isEqualTo(Duration.ofDays(7));
        assertThat(descriptor.privatePrecisionDecimals()).isEqualTo(7);
        IngestPolicy policy = descriptor.defaultIngestPolicy();
        assertThat(policy).isInstanceOf(IngestPolicy.Sampled.class);
        IngestPolicy.Sampled sampled = (IngestPolicy.Sampled) policy;
        assertThat(sampled.minInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(sampled.keyframe()).isEqualTo(Duration.ofMinutes(10));
        assertThat(sampled.minDeviationMeters()).isEqualTo(50.0);

        row.setProfile(null);
        assertThat(CustomSourceDescriptors.toDescriptor(row, JSON).defaultIngestPolicy())
                .isNotInstanceOf(IngestPolicy.Sampled.class);
    }
}
