package ai.foresignal.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Fails any test during which something asked to resolve a host outside this machine (checkpoint
 * 041: "fail any test that opens a socket outside localhost"). Registered for every test through
 * {@code META-INF/services/org.junit.jupiter.api.extension.Extension} and extension autodetection;
 * the refusals come from {@link LocalOnlyResolverProvider}.
 */
public final class NoExternalNetworkExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(NoExternalNetworkExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(NS).put("mark", LocalOnlyResolverProvider.REFUSED.size());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Integer mark = context.getStore(NS).get("mark", Integer.class);
        List<String> refused = LocalOnlyResolverProvider.REFUSED;
        List<String> during = refused.subList(mark == null ? 0 : Math.min(mark, refused.size()), refused.size());
        assertThat(List.copyOf(during))
                .as("hosts outside localhost this test tried to reach (tests must stay on localhost)")
                .isEmpty();
    }
}
