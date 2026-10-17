package ai.foresignal.outlook.regional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code regional_outlook} rows (prompt 126). */
public interface RegionalOutlookRepository extends JpaRepository<RegionalOutlookEntity, String> {

    /** The newest outlook of a region and family. */
    Optional<RegionalOutlookEntity> findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc(String region,
            String family);

    /** The outlooks of a region and family, newest first. */
    List<RegionalOutlookEntity> findByRegionAndFamilyOrderByPublishedAtDescIdDesc(String region, String family,
            Pageable page);

    /** The latest outlooks, by id (the list endpoint). */
    List<RegionalOutlookEntity> findByLatestTrueAndIdGreaterThanOrderByIdAsc(String after, Pageable page);

    /** The latest outlooks, every one. */
    List<RegionalOutlookEntity> findByLatestTrueOrderByIdAsc();

    /** The outlooks changed since a time, by id (the record generator). */
    Slice<RegionalOutlookEntity> findByUpdatedAtGreaterThanEqualOrderByIdAsc(Instant since, Pageable page);

    /** Every outlook, by id (a full generator pass). */
    Slice<RegionalOutlookEntity> findAllByOrderByIdAsc(Pageable page);
}
