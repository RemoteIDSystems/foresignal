package ai.foresignal.collab;

import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code attachment} table. */
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, String> {

    /** How many attachments share a stored file. */
    long countBySha256(String sha256);
}
