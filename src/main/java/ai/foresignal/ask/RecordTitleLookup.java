package ai.foresignal.ask;

import java.util.Optional;

/**
 * The title of a record version by its persisted seq, for citations whose tool output carried no
 * title ({@link VersionTitleLookup} resolves it with the shared {@code TitleResolver}).
 */
@FunctionalInterface
public interface RecordTitleLookup {

    /**
     * The title of record {@code id} at {@code seq}.
     *
     * @param id {@code source:externalId}
     * @param seq the persisted version seq
     * @return the title, when the version is still retained and has one
     */
    Optional<String> title(String id, long seq);
}
