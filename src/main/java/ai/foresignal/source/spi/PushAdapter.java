package ai.foresignal.source.spi;

/**
 * A source whose data arrives through {@code POST /v1/ingest/{sourceId}} (Architecture §16).
 *
 * <p>A marker: there is nothing for Foresignal to call, because the push is inbound. It exists so that
 * {@link Mode#PUSH} can be checked against an implemented interface like every other mode, rather
 * than being the one mode the registry cannot verify.
 */
public interface PushAdapter extends SourceAdapter {}
