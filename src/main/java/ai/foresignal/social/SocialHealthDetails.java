package ai.foresignal.social;

import ai.foresignal.collect.HealthDetailsContributor;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Social collection health in the 006 source health {@code details} (prompt 135): for a streaming social adapter the
 * stream's lag, cursor, connection time and last gap; for a paid one this month's spend and refusals.
 */
@Component
public class SocialHealthDetails implements HealthDetailsContributor {

    private final StreamLedger ledger;
    private final XSpend spend;

    /** Creates the contributor. */
    public SocialHealthDetails(StreamLedger ledger, XSpend spend) {
        this.ledger = ledger;
        this.spend = spend;
    }

    @Override
    public Map<String, Object> details(SourceDescriptor descriptor) {
        SocialPlatformCatalog.Definition d = SocialPlatformCatalog.get(descriptor.id()).orElse(null);
        if (d == null) {
            return Map.of();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (d.type().startsWith("STREAMING")) {
            StreamLedger.State st = ledger.state(d.id());
            out.put("streamConnectedSince", st.connectedSince() == null ? null : st.connectedSince().toString());
            out.put("streamLagSeconds", st.lagSeconds());
            out.put("streamCursor", st.cursor());
            if (st.lastGap() != null) {
                Map<String, Object> gap = new LinkedHashMap<>();
                gap.put("from", st.lastGap().from().toString());
                gap.put("to", st.lastGap().to() == null ? null : st.lastGap().to().toString());
                gap.put("replayed", st.lastGap().replayed());
                gap.put("reason", st.lastGap().reason());
                out.put("streamLastGap", gap);
            }
        }
        if (d.defaultMonthlyCapUsd() != null) {
            try {
                XSpend.Month m = spend.month(d.id());
                out.put("spentUsdThisMonth", m.spentUsd());
                out.put("refusedThisMonth", m.refused());
            } catch (RuntimeException e) {
                // the spend view is optional here
            }
        }
        out.values().removeIf(java.util.Objects::isNull);
        return out;
    }
}
