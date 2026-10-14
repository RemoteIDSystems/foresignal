package ai.foresignal.source.adapters.meteoalarm;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers one {@link MeteoAlarmAdapter} per reviewed MeteoAlarm member (prompt 089): {@code meteoalarm-<slug>}
 * for each of the 38 members of {@link MeteoAlarmCountries#REVIEWED}. Every source ships disabled.
 *
 * <p>The member list is static and code-reviewed; no caller-supplied country reaches the API.
 */
@Component
public class MeteoAlarmFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /** Creates the family. */
    @Autowired
    public MeteoAlarmFamily(@Value("${" + MeteoAlarmAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(baseUrlOverride, MeteoAlarmCountries.REVIEWED);
    }

    /** Creates the family from an explicit list (tests). */
    public MeteoAlarmFamily(String baseUrlOverride, List<MeteoAlarmCountries.Country> countries) {
        List<String> problems = MeteoAlarmCountries.problems(countries);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the MeteoAlarm member list is invalid: "
                    + problems);
        }
        List<SourceAdapter> built = new ArrayList<>();
        for (MeteoAlarmCountries.Country country : countries) {
            built.add(new MeteoAlarmAdapter(country, baseUrlOverride));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
