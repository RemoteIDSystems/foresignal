package ai.foresignal.outlook.index;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code regional_index_value} rows (prompt 126). */
public interface RegionalIndexValueRepository extends JpaRepository<RegionalIndexValueEntity, String> {

    /** One region's value on one day. */
    Optional<RegionalIndexValueEntity> findByDefIdAndRegionAndDay(String defId, String region, LocalDate day);

    /** One region's values over a range of days. */
    List<RegionalIndexValueEntity> findByDefIdAndRegionAndDayBetweenOrderByDayAsc(String defId, String region,
            LocalDate from, LocalDate to);

    /** Every region's values on one day. */
    List<RegionalIndexValueEntity> findByDefIdAndDayOrderByRegionAsc(String defId, LocalDate day);

    /** Every value of an index over a range of days. */
    List<RegionalIndexValueEntity> findByDefIdAndDayBetweenOrderByDayAscRegionAsc(String defId, LocalDate from,
            LocalDate to);

    /** The newest value of an index. */
    Optional<RegionalIndexValueEntity> findTop1ByDefIdOrderByDayDesc(String defId);

    /** The values computed since a time, by id (the record generator). */
    Slice<RegionalIndexValueEntity> findByComputedAtGreaterThanEqualOrderByIdAsc(Instant since, Pageable page);
}
