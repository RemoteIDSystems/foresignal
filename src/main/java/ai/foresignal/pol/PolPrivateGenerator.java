package ai.foresignal.pol;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * {@code pattern-of-life-private} (prompt 114): the {@link PolGenerator} of the private subjects (a member on a
 * private source, 039). The source is private by descriptor, so its records stay off every non-private egress path
 * (invariant EGR-1), and the normalizer caps their coordinates; the engine already stores the subject's stays and
 * places at its sources' precision cap.
 */
@Component
public class PolPrivateGenerator extends PolGenerator {

    /** The source id. */
    public static final String ID = "pattern-of-life-private";

    /** Creates the private generator. */
    public PolPrivateGenerator(@Lazy PolEngine engine, PolSubjectRepository subjects, PolPlaceRepository places) {
        super(engine, subjects, places, buildDescriptor(ID, true));
    }

    @Override
    protected boolean privateClass() {
        return true;
    }
}
