from enum import StrEnum


class CycleRunStatus(StrEnum):
    DIGEST_ONLY = "digest-only"
    FAILED = "failed"
    OK = "ok"
    PARTIAL = "partial"
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)
