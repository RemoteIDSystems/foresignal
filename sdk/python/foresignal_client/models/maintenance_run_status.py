from enum import StrEnum


class MaintenanceRunStatus(StrEnum):
    DONE = "done"
    FAILED = "failed"
    PARTIAL = "partial"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)
