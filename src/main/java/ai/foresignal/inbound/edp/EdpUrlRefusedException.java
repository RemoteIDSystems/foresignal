package ai.foresignal.inbound.edp;

import ai.foresignal.source.spi.AdapterException;

/** A provider URL the {@link EdpUrlGuard} refuses (EDP §7, invariant EXT-6). */
public class EdpUrlRefusedException extends AdapterException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with the reason. */
    public EdpUrlRefusedException(String message) {
        super(message);
    }
}
