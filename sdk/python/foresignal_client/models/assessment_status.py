from enum import StrEnum


class AssessmentStatus(StrEnum):
    DIGEST_ONLY = "digest-only"
    FAILED = "failed"
    OK = "ok"
    PARTIAL = "partial"

    def __str__(self) -> str:
        return str(self.value)
