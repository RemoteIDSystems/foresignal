package ai.elaro.collect;

import ai.elaro.source.spi.AdapterException;

/**
 * The request could not be completed: connection refused, DNS failure, timeout, reset.
 *
 * <p>Distinct from {@link UpstreamHttpException} because the two mean different things to health:
 * an HTTP error says the upstream is there and unhappy with us, while this says we could not reach
 * it at all.
 */
public class UpstreamIOException extends AdapterException {

    private static final long serialVersionUID = 1L;

    public UpstreamIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
