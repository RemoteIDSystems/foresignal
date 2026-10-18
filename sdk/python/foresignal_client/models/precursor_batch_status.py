from enum import StrEnum


class PrecursorBatchStatus(StrEnum):
    CANCELLED = "cancelled"
    DONE = "done"
    FAILED = "failed"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)
