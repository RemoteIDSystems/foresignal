package ai.foresignal.source.adapters.cftc;

import java.util.List;
import java.util.Locale;

/**
 * The three CFTC Commitments of Traders reports {@code cftc-cot} reads (prompt 117), with their Socrata datasets on
 * {@code publicreporting.cftc.gov} and the measures each yields.
 *
 * <p>The dataset ids were confirmed against the live portal on 2026-10-16: legacy futures {@code 6dca-aqww},
 * disaggregated futures {@code 72hh-3qpy}, TFF futures {@code gpe5-46if}; their futures-and-options variants are
 * {@code jun7-fc8e}, {@code kh3c-gbw2} and {@code yw9f-hn96} ({@code futonly_or_combined = Combined}).
 *
 * <p>Each {@link Measure} names the column (or the long and short columns) it reads, with alternates, because the
 * CFTC's column names are irregular ({@code swap__positions_short_all} has two underscores, some long/short pairs
 * drop the {@code _all} suffix). The names were read from the captured fixtures.
 */
public enum CotReport {

    /** The legacy report: commercial, non-commercial and non-reportable positions. */
    LEGACY("legacy", "6dca-aqww", "jun7-fc8e", List.of(
            Measure.level("oi", "open interest", "open_interest_all"),
            Measure.net("nc_net", "non-commercial net",
                    List.of("noncomm_positions_long_all"), List.of("noncomm_positions_short_all")),
            Measure.net("comm_net", "commercial net",
                    List.of("comm_positions_long_all"), List.of("comm_positions_short_all")),
            Measure.net("nr_net", "non-reportable net",
                    List.of("nonrept_positions_long_all"), List.of("nonrept_positions_short_all")))),

    /** The disaggregated report: producer/merchant, swap dealers, managed money, other reportables. */
    DISAGGREGATED("disaggregated", "72hh-3qpy", "kh3c-gbw2", List.of(
            Measure.level("oi", "open interest", "open_interest_all"),
            Measure.level("mm_long", "managed money long", "m_money_positions_long_all"),
            Measure.level("mm_short", "managed money short", "m_money_positions_short_all"),
            Measure.net("mm_net", "managed money net",
                    List.of("m_money_positions_long_all"), List.of("m_money_positions_short_all")),
            Measure.netPctOi("mm_net_pct_oi", "managed money net, % of open interest",
                    List.of("m_money_positions_long_all"), List.of("m_money_positions_short_all")),
            Measure.net("pm_net", "producer/merchant net",
                    List.of("prod_merc_positions_long", "prod_merc_positions_long_all"),
                    List.of("prod_merc_positions_short", "prod_merc_positions_short_all")),
            Measure.net("sd_net", "swap dealer net",
                    List.of("swap_positions_long_all", "swap_positions_long"),
                    List.of("swap__positions_short_all", "swap_positions_short_all", "swap_positions_short")),
            Measure.net("or_net", "other reportable net",
                    List.of("other_rept_positions_long", "other_rept_positions_long_all"),
                    List.of("other_rept_positions_short", "other_rept_positions_short_all")),
            Measure.net("nr_net", "non-reportable net",
                    List.of("nonrept_positions_long_all"), List.of("nonrept_positions_short_all")))),

    /** The Traders in Financial Futures report: dealers, asset managers, leveraged funds, other reportables. */
    TFF("tff", "gpe5-46if", "yw9f-hn96", List.of(
            Measure.level("oi", "open interest", "open_interest_all"),
            Measure.net("dealer_net", "dealer net",
                    List.of("dealer_positions_long_all", "dealer_positions_long"),
                    List.of("dealer_positions_short_all", "dealer_positions_short")),
            Measure.net("am_net", "asset manager net",
                    List.of("asset_mgr_positions_long", "asset_mgr_positions_long_all"),
                    List.of("asset_mgr_positions_short", "asset_mgr_positions_short_all")),
            Measure.net("lev_net", "leveraged funds net",
                    List.of("lev_money_positions_long", "lev_money_positions_long_all"),
                    List.of("lev_money_positions_short", "lev_money_positions_short_all")),
            Measure.net("or_net", "other reportable net",
                    List.of("other_rept_positions_long", "other_rept_positions_long_all"),
                    List.of("other_rept_positions_short", "other_rept_positions_short_all"))));

    /** The open interest column every report carries. */
    static final String OPEN_INTEREST = "open_interest_all";

    private final String wire;
    private final String futuresDataset;
    private final String combinedDataset;
    private final List<Measure> measures;

    CotReport(String wire, String futuresDataset, String combinedDataset, List<Measure> measures) {
        this.wire = wire;
        this.futuresDataset = futuresDataset;
        this.combinedDataset = combinedDataset;
        this.measures = measures;
    }

    /** The name in the {@code reports} parameter and in series ids. */
    public String wire() {
        return wire;
    }

    /** The Socrata dataset: the futures-only one, or the futures-and-options one. */
    public String dataset(boolean futuresOnly) {
        return futuresOnly ? futuresDataset : combinedDataset;
    }

    /** The measures this report yields, in series order. */
    public List<Measure> measures() {
        return measures;
    }

    /** The report named {@code wire} (case-insensitive), or null. */
    public static CotReport fromWire(String wire) {
        if (wire == null) {
            return null;
        }
        String lower = wire.strip().toLowerCase(Locale.ROOT);
        for (CotReport report : values()) {
            if (report.wire.equals(lower)) {
                return report;
            }
        }
        return null;
    }

    /** How a measure is computed. */
    public enum Kind {
        /** One column as it is. */
        LEVEL,
        /** Long minus short. */
        NET,
        /** (Long minus short) ÷ open interest × 100. */
        NET_PCT_OI
    }

    /**
     * One measure of a report.
     *
     * @param id the measure id in the series id ({@code mm_net})
     * @param label the human-readable label
     * @param kind how it is computed
     * @param longColumns the column (for a level) or the long column, first match wins
     * @param shortColumns the short column, first match wins; empty for a level
     */
    public record Measure(String id, String label, Kind kind, List<String> longColumns, List<String> shortColumns) {

        /** Copies the column lists. */
        public Measure {
            longColumns = List.copyOf(longColumns);
            shortColumns = List.copyOf(shortColumns);
        }

        static Measure level(String id, String label, String column) {
            return new Measure(id, label, Kind.LEVEL, List.of(column), List.of());
        }

        static Measure net(String id, String label, List<String> longs, List<String> shorts) {
            return new Measure(id, label, Kind.NET, longs, shorts);
        }

        static Measure netPctOi(String id, String label, List<String> longs, List<String> shorts) {
            return new Measure(id, label, Kind.NET_PCT_OI, longs, shorts);
        }

        /** True for open interest. */
        public boolean openInterest() {
            return "oi".equals(id);
        }
    }
}
