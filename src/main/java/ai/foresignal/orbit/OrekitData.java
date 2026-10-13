package ai.foresignal.orbit;

import org.orekit.data.ClasspathCrawler;
import org.orekit.data.DataContext;
import org.orekit.data.LazyLoadedDataContext;

/**
 * Foresignal's Orekit data (prompt 054): a {@link LazyLoadedDataContext} fed only by a
 * {@link ClasspathCrawler} over the pinned subset in {@code src/main/resources/orekit-data/}:
 *
 * <ul>
 *   <li>{@value #LEAP_SECONDS}: the UTC−TAI history (USNO {@code tai-utc.dat});
 *   <li>{@value #EOP}: IERS Rapid Service/Prediction Center Earth orientation parameters (IAU 2000),
 *       trimmed to 2024-01-01 onward. Outside its range Orekit uses zero corrections, which is far
 *       below the accuracy an approximate swath needs.
 * </ul>
 *
 * <p>Both come from the {@code orekit-data} repository (commit 2422a7d0, 2026-10-06;
 * {@code THIRD_PARTY_NOTICES.md}). The context is built on first use only, so nothing Orekit-related
 * loads at startup. {@code DataContext.getDefault()} is never used: it would look for an
 * {@code orekit.data.path} on the host.
 */
public final class OrekitData {

    /** The leap-second file on the classpath. */
    public static final String LEAP_SECONDS = "orekit-data/tai-utc.dat";

    /** The EOP file on the classpath. */
    public static final String EOP = "orekit-data/finals2000A.all";

    private OrekitData() {}

    /** Holds the context; initialized on first access (lazy holder idiom). */
    private static final class Holder {

        static final DataContext CONTEXT = create();

        private static DataContext create() {
            LazyLoadedDataContext context = new LazyLoadedDataContext();
            context.getDataProvidersManager().addProvider(
                    new ClasspathCrawler(OrekitData.class.getClassLoader(), LEAP_SECONDS, EOP));
            return context;
        }
    }

    /** The data context, created at first use. */
    public static DataContext context() {
        return Holder.CONTEXT;
    }
}
