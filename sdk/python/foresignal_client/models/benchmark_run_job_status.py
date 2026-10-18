from enum import StrEnum


class BenchmarkRunJobStatus(StrEnum):
    CANCELLED = "cancelled"
    DONE = "done"
    FAILED = "failed"
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)
