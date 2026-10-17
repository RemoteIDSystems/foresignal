package ai.foresignal.baserate.reference;

import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.adapters.acled.AcledAdapter;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The production {@link AcledHistoryClient} (prompt 121): {@link AcledAdapter#historyPage} with the {@code acled}
 * source's own credentials, tokens and budget (the 081 budget), so a history import never exceeds what the operator
 * configured for ACLED.
 */
@Component
public class AcledAdapterHistoryClient implements AcledHistoryClient {

    private final AcledAdapter adapter;
    private final ReferenceHttp http;
    private final CredentialsAccess credentials;
    private final Clock clock;

    /** Creates the client. */
    public AcledAdapterHistoryClient(AcledAdapter adapter, ReferenceHttp http, CredentialsAccess credentials,
            Clock clock) {
        this.adapter = adapter;
        this.http = http;
        this.credentials = credentials;
        this.clock = clock;
    }

    @Override
    public boolean credentialsPresent() {
        Map<String, String> values = credentials.forSource(AcledAdapter.ID);
        String email = values.get(AcledAdapter.EMAIL);
        String password = values.get(AcledAdapter.PASSWORD);
        return email != null && !email.isBlank() && password != null && !password.isEmpty();
    }

    @Override
    public byte[] page(String isoNumeric, LocalDate from, LocalDate to, int page) throws Exception {
        Map<String, String> values = credentials.forSource(AcledAdapter.ID);
        return adapter.historyPage(http.http(AcledAdapter.ID), clock.instant(), values.get(AcledAdapter.EMAIL).strip(),
                values.get(AcledAdapter.PASSWORD), isoNumeric, from, to, page);
    }
}
