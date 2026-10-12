package ai.foresignal.briefing;

import ai.foresignal.api.support.BboxParser;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Component;

/** The {@link ScopeResolver} of prompt 029: sources, kinds, bbox, rule ids and the health flag as given. */
@Component
public class DefaultScopeResolver implements ScopeResolver {

    @Override
    public Resolved resolve(BriefingScope scope) {
        BriefingScope s = scope == null ? BriefingScope.all() : scope;
        return new Resolved(s.sources(), s.kinds(), BboxParser.parse(s.bbox()), new LinkedHashSet<>(s.ruleIds()),
                s.includeHealth());
    }
}
