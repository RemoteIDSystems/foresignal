from enum import StrEnum


class ListReferenceImportsStatus(StrEnum):
    CANCELLED = "cancelled"
    DONE = "done"
    FAILED = "failed"
    QUEUED = "queued"
    RUNNING = "running"
    SKIPPED = "skipped"

    def __str__(self) -> str:
        return str(self.value)
