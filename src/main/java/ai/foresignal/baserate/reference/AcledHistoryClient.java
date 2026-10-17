package ai.foresignal.baserate.reference;

import java.time.LocalDate;

/**
 * The ACLED path of the history import (prompt 121): the 081 ACLED client with the operator's credentials. Kept as an
 * interface so the importer is testable without a network.
 */
public interface AcledHistoryClient {

    /** True when the {@code acled} source's email and password credentials are both set. */
    boolean credentialsPresent();

    /**
     * Reads one page of events of one country.
     *
     * @param isoNumeric the country's ISO 3166 numeric code
     * @param from the first event date
     * @param to the last event date
     * @param page the page number, from 1
     * @return the response body ({@code {success, data: [...]}})
     * @throws Exception when the request fails (credentials refused, budget spent, I/O)
     */
    byte[] page(String isoNumeric, LocalDate from, LocalDate to, int page) throws Exception;
}
