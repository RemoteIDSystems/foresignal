package ai.foresignal.chronicle.network;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Chronicle links (prompt 134). */
public interface ChronicleLinkRepository extends JpaRepository<ChronicleLinkEntity, String> {

    /** The link of a pair. */
    @Query("select l from ChronicleLinkEntity l where l.a = :a and l.b = :b")
    Optional<ChronicleLinkEntity> findPair(@Param("a") String a, @Param("b") String b);

    /** The links with {@code node} as a side, oldest first. */
    @Query("select l from ChronicleLinkEntity l where l.a = :node or l.b = :node order by l.firstFound, l.id")
    List<ChronicleLinkEntity> findBySide(@Param("node") String node);

    /** The links with a side among {@code nodes}. */
    @Query("select l from ChronicleLinkEntity l where l.a in :nodes or l.b in :nodes order by l.firstFound, l.id")
    List<ChronicleLinkEntity> findBySides(@Param("nodes") Collection<String> nodes);

    /** Every link in a status. */
    List<ChronicleLinkEntity> findByStatusOrderByIdAsc(String status);

    /** Removes the links of a deleted chronicle or withdrawn sketch. */
    @Modifying
    @Transactional
    @Query("delete from ChronicleLinkEntity l where l.a = :node or l.b = :node")
    int deleteBySide(@Param("node") String node);
}
