package ai.foresignal.space;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Orekit startup self-test (prompt 064): once the application is ready, on a virtual thread so startup is
 * not delayed, it propagates the bundled ISS element set ({@value #RESOURCE}) and checks the first pass over
 * the bundled observer against the fixture's expected pass (computed independently with Skyfield): AOS and LOS
 * within {@value #TIME_TOLERANCE_SECONDS} s, maximum elevation within {@value #ELEVATION_TOLERANCE_DEG}°.
 *
 * <p>A failure is logged as an error and kept in {@link #result()}; it does not stop the application, since only
 * pass prediction depends on it. Disabled with {@code foresignal.space.self-test=false}.
 */
@Component
public class OrekitSelfTest {

    private static final Logger log = LoggerFactory.getLogger(OrekitSelfTest.class);

    /** The bundled fixture. */
    public static final String RESOURCE = "space/selftest-iss.json";

    /** The tolerated AOS and LOS error, seconds. */
    public static final double TIME_TOLERANCE_SECONDS = 5;

    /** The tolerated maximum-elevation error, degrees. */
    public static final double ELEVATION_TOLERANCE_DEG = 0.2;

    /**
     * The outcome of a run.
     *
     * @param passed true when the predicted pass matched the fixture
     * @param detail what was compared, or why it failed
     */
    public record Result(boolean passed, String detail) {}

    private final JsonMapper json;
    private final boolean enabled;
    private volatile Result result;

    /** Creates the self-test. */
    public OrekitSelfTest(JsonMapper json, @Value("${foresignal.space.self-test:true}") boolean enabled) {
        this.json = json;
        this.enabled = enabled;
    }

    /** Starts the run in the background once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (enabled) {
            Thread.ofVirtual().name("foresignal-orekit-self-test").start(this::run);
        }
    }

    /** The last result, or null before the first run. */
    public Result result() {
        return result;
    }

    /** Runs the check now and returns its result. */
    public Result run() {
        Result r;
        try {
            r = check(load());
        } catch (RuntimeException | IOException e) {
            r = new Result(false, "self-test could not run: " + e);
        }
        result = r;
        if (r.passed()) {
            log.info("Orekit self-test passed: {}", r.detail());
        } else {
            log.error("Orekit self-test FAILED; predicted satellite passes may be wrong: {}", r.detail());
        }
        return r;
    }

    private Map<String, Object> load() throws IOException {
        try (InputStream in = OrekitSelfTest.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IOException(RESOURCE + " is missing from the classpath");
            }
            return json.readValue(in, new TypeReference<Map<String, Object>>() {});
        }
    }

    /** Compares the first predicted pass of {@code fixture} with its expected pass. */
    @SuppressWarnings("unchecked")
    static Result check(Map<String, Object> fixture) {
        OmmElements elements = OmmElements.fromPayload((Map<String, Object>) fixture.get("elements"));
        Map<String, Object> o = (Map<String, Object>) fixture.get("observer");
        Observer observer = new Observer(num(o.get("lat")), num(o.get("lon")), num(o.get("altM")));
        Instant from = Instant.parse(String.valueOf(fixture.get("from")));
        Duration horizon = Duration.ofHours((long) num(fixture.get("horizonHours")));
        Map<String, Object> expected = (Map<String, Object>) fixture.get("expected");
        List<PredictedPass> passes = PassPredictor.predict(elements, observer, from, horizon,
                num(fixture.get("minElevationDeg")));
        if (passes.isEmpty()) {
            return new Result(false, "no pass predicted");
        }
        PredictedPass p = passes.get(0);
        double aosError = Math.abs(Duration.between(Instant.parse(String.valueOf(expected.get("aos"))), p.aos())
                .toMillis() / 1000.0);
        double losError = Math.abs(Duration.between(Instant.parse(String.valueOf(expected.get("los"))), p.los())
                .toMillis() / 1000.0);
        double elError = Math.abs(num(expected.get("maxElevationDeg")) - p.maxElevationDeg());
        String detail = String.format(java.util.Locale.ROOT,
                "ISS pass AOS %s (error %.2f s), LOS %s (error %.2f s), max elevation %.2f° (error %.3f°)",
                p.aos(), aosError, p.los(), losError, p.maxElevationDeg(), elError);
        boolean ok = aosError <= TIME_TOLERANCE_SECONDS && losError <= TIME_TOLERANCE_SECONDS
                && elError <= ELEVATION_TOLERANCE_DEG;
        return new Result(ok, detail);
    }

    private static double num(Object value) {
        return value instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(value));
    }
}
