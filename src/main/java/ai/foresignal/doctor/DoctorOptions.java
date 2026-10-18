package ai.foresignal.doctor;

import ai.foresignal.api.error.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;

/**
 * The options of a doctor run (OpenAPI {@code DoctorOptions}).
 *
 * @param probe probe the live upstream through the budgeted client (default true)
 * @param llm {@code auto} (an explanation when a chat provider is assigned), {@code on} or {@code off}
 * @param detail {@code compact} (≤ 6k tokens of LLM context, fits small local models) or {@code full} (≤ 30k)
 */
public record DoctorOptions(boolean probe, String llm, String detail) {

    /** {@code llm=auto}. */
    public static final String LLM_AUTO = "auto";

    /** {@code llm=on}. */
    public static final String LLM_ON = "on";

    /** {@code llm=off}. */
    public static final String LLM_OFF = "off";

    /** {@code detail=compact}. */
    public static final String COMPACT = "compact";

    /** {@code detail=full}. */
    public static final String FULL = "full";

    /** The defaults: probe, llm auto, compact. */
    public static final DoctorOptions DEFAULTS = new DoctorOptions(true, LLM_AUTO, COMPACT);

    /** Normalizes and checks the members (400 {@code VALIDATION_FAILED} for an unknown value). */
    public DoctorOptions {
        llm = llm == null ? LLM_AUTO : llm.strip().toLowerCase(Locale.ROOT);
        detail = detail == null ? COMPACT : detail.strip().toLowerCase(Locale.ROOT);
        if (!llm.equals(LLM_AUTO) && !llm.equals(LLM_ON) && !llm.equals(LLM_OFF)) {
            throw ValidationException.forField("llm", "must be auto, on or off");
        }
        if (!detail.equals(COMPACT) && !detail.equals(FULL)) {
            throw ValidationException.forField("detail", "must be compact or full");
        }
    }

    /** The JSON form; omitted members take their defaults. */
    @JsonCreator
    public static DoctorOptions of(@JsonProperty("probe") Boolean probe, @JsonProperty("llm") String llm,
            @JsonProperty("detail") String detail) {
        return new DoctorOptions(probe == null || probe, llm, detail);
    }

    /** True for {@code detail=full}. */
    public boolean full() {
        return FULL.equals(detail);
    }
}
