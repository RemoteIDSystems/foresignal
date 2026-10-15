package ai.foresignal.intel;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** {@code population_h3} access (prompt 101). */
public interface PopulationH3Repository extends JpaRepository<PopulationH3Entity, PopulationH3Entity.Key> {

    /** The population summed over {@code cells} at {@code res}, or null when none are present. */
    @Query("select sum(p.pop) from PopulationH3Entity p where p.res = ?1 and p.h3 in ?2")
    Double sumOver(short res, Collection<Long> cells);

    /** How many cells are stored at a resolution (0 means the pack is not installed). */
    long countByRes(short res);
}
