package ai.foresignal.inbound.edp.binding;

import ai.foresignal.analytics.registry.AnalysisSeriesEntity;
import ai.foresignal.analytics.registry.AnalysisSeriesRegistrar;
import ai.foresignal.analytics.registry.AnalysisSeriesRepository;
import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.inbound.edp.EdpPeriods;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Target {@code series} (prompt 138): observations become the analysis series
 * {@code series:ext-{id}/{kind}/{seriesKey}} ({@link ExternalSeriesReader}), registered in the 119 analysis-series
 * registry when their periods are days or weeks (other granularities are readable but not analyzed). Configuration:
 * {@code unit}, {@code transform} ({@code none}, {@code log1p}, {@code diff}), {@code releaseLagDays},
 * {@code family} (signal family), {@code regionCode}; each optional.
 */
@Component
public class SeriesBindingTarget implements BindingTargetHandler {

    private static final Logger log = LoggerFactory.getLogger(SeriesBindingTarget.class);

    /** The transforms a series binding accepts. */
    public static final List<String> TRANSFORMS = List.of("none", "log1p", "diff");

    private final AnalysisSeriesRepository rows;
    private final ObjectProvider<AnalysisSeriesRegistrar> registrar;
    private final Clock clock;

    /** Creates the target. */
    public SeriesBindingTarget(AnalysisSeriesRepository rows, ObjectProvider<AnalysisSeriesRegistrar> registrar,
            Clock clock) {
        this.rows = rows;
        this.registrar = registrar;
        this.clock = clock;
    }

    @Override
    public String target() {
        return BindingVocabulary.SERIES;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        BindingConfig.string(config, "unit", errors);
        BindingConfig.oneOf(config, "transform", TRANSFORMS, errors);
        BindingConfig.range(config, "releaseLagDays", 0, 3650, errors);
        BindingConfig.string(config, "family", errors);
        BindingConfig.string(config, "seriesFamily", errors);
        String region = BindingConfig.text(config, "regionCode");
        if (region != null && !region.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
            errors.add(new FieldError("config.regionCode", "must be a 040 region code"));
        }
        return errors;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        String key = item.text("seriesKey");
        if (key == null) {
            return BindingEffect.error(item, "the observation has no seriesKey");
        }
        if (EdpPeriods.parse(item.text("t")) == null) {
            return BindingEffect.error(item, "the observation's period t is not readable");
        }
        return BindingEffect.of(item, "series-point", ExternalSeriesReader.ref(item.sourceId(), stream.kind(), key));
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        BindingEffect effect = plan(binding, stream, item);
        if (effect.failed()) {
            return effect;
        }
        String frequency = ExternalSeriesReader.registryFrequency(EdpPeriods.parse(item.text("t")));
        if (frequency != null) {
            register(effect.targetRef(), frequency, binding.getConfig());
        }
        return effect;
    }

    /** Registers the series in the 119 registry once (automatic origin), within the registry cap. */
    void register(String ref, String frequency, Map<String, Object> config) {
        if (rows.existsById(ref)) {
            return;
        }
        AnalysisSeriesRegistrar r = registrar.getIfAvailable();
        int max = r == null ? AnalysisSeriesRegistrar.DEFAULT_MAX : r.max();
        if (rows.count() >= max) {
            log.info("Not registering {}: the analysis-series registry is at its cap of {}", ref, max);
            return;
        }
        Instant now = clock.instant();
        AnalysisSeriesEntity row = new AnalysisSeriesEntity(ref, "auto", now);
        row.setDetectors(AnalysisSeriesService.DEFAULT_DETECTORS);
        row.setEnabled(true);
        row.setFrequency(frequency);
        row.setFamily(BindingConfig.text(config, "family"));
        row.setRegionCode(BindingConfig.text(config, "regionCode"));
        row.setVolume(1);
        row.setUpdatedAt(now);
        rows.save(row);
    }
}
