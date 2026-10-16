package ai.foresignal.export;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** The {@code holds} block of {@code /v1/system} (prompt 111): count, held rows and bytes. */
@Component
public class HoldsSystemBlock implements SystemBlockContributor {

    private final HoldRepository holds;
    private final HoldService service;

    /** Creates the block. */
    public HoldsSystemBlock(HoldRepository holds, HoldService service) {
        this.holds = holds;
        this.service = service;
    }

    @Override
    public String key() {
        return "holds";
    }

    @Override
    public Map<String, Object> block() {
        HoldService.Counts counts = service.totals();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", holds.count());
        out.put("rows", counts.rows());
        out.put("bytes", counts.bytes());
        return out;
    }
}
