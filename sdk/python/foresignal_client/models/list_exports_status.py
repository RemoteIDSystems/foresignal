from enum import StrEnum


class ListExportsStatus(StrEnum):
    CANCELLED = "CANCELLED"
    DONE = "DONE"
    EXPIRED = "EXPIRED"
    FAILED = "FAILED"
    QUEUED = "QUEUED"
    RUNNING = "RUNNING"

    def __str__(self) -> str:
        return str(self.value)
