package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Deep-link templates clients and notifications use (OpenAPI {@code ClientLinks}, prompt 023).
 *
 * <p>Defaults use the {@code foresignal://} scheme; operators may set verified {@code https://}
 * links instead. Placeholders in braces are filled by the caller: {@code {id}}, {@code {source}},
 * {@code {externalId}}, {@code {briefingId}}, {@code {publicBaseUrl}}, {@code {code}} and (R2-007) {@code {token}}.
 *
 * @param alert an alert, default {@value #DEFAULT_ALERT}
 * @param record a record, default {@value #DEFAULT_RECORD}
 * @param briefingRun a briefing run, default {@value #DEFAULT_BRIEFING_RUN}
 * @param pair a pairing QR payload, default {@value #DEFAULT_PAIR}
 * @param share a share link opened in a client (prompt 112, placeholder {@code {key}}); no default: share
 *     links redirect with {@code ?open=1} only when the operator sets it
 * @param activate the account activation link of the welcome email (R2-007, placeholders {@code {publicBaseUrl}}
 *     and {@code {token}}), default {@value #DEFAULT_ACTIVATE}
 * @param passwordReset the password reset link of the reset email (R2-007), default {@value #DEFAULT_PASSWORD_RESET}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientLinks(String alert, String record, String briefingRun, String pair, String share,
        String activate, String passwordReset) {

    /** The four templates of prompt 023, without a share template. */
    public ClientLinks(String alert, String record, String briefingRun, String pair) {
        this(alert, record, briefingRun, pair, null);
    }

    /** The templates of prompts 023 and 112, without the account links of R2-007. */
    public ClientLinks(String alert, String record, String briefingRun, String pair, String share) {
        this(alert, record, briefingRun, pair, share, null, null);
    }

    public static final String DEFAULT_ALERT = "foresignal://alerts/{id}";
    public static final String DEFAULT_RECORD = "foresignal://records/{source}/{externalId}";
    public static final String DEFAULT_BRIEFING_RUN = "foresignal://briefings/{briefingId}/runs/{id}";
    public static final String DEFAULT_PAIR = "foresignal://pair?u={publicBaseUrl}&c={code}";
    public static final String DEFAULT_ACTIVATE = "foresignal://activate?u={publicBaseUrl}&t={token}";
    public static final String DEFAULT_PASSWORD_RESET = "foresignal://reset-password?u={publicBaseUrl}&t={token}";

    /** The shipped templates. */
    public static ClientLinks defaults() {
        return new ClientLinks(DEFAULT_ALERT, DEFAULT_RECORD, DEFAULT_BRIEFING_RUN, DEFAULT_PAIR, null,
                DEFAULT_ACTIVATE, DEFAULT_PASSWORD_RESET);
    }

    /** This value with every missing template taken from {@link #defaults()}. */
    public ClientLinks withDefaults() {
        return new ClientLinks(
                alert == null ? DEFAULT_ALERT : alert,
                record == null ? DEFAULT_RECORD : record,
                briefingRun == null ? DEFAULT_BRIEFING_RUN : briefingRun,
                pair == null ? DEFAULT_PAIR : pair,
                share,
                activate == null ? DEFAULT_ACTIVATE : activate,
                passwordReset == null ? DEFAULT_PASSWORD_RESET : passwordReset);
    }

    /**
     * {@code template} with each {@code {name}} replaced by its value; values are inserted as
     * given, so the caller encodes what needs encoding.
     */
    public static String fill(String template, Map<String, String> values) {
        String out = template;
        for (Map.Entry<String, String> e : values.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
