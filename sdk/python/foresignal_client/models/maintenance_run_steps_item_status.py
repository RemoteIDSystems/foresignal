from enum import StrEnum


class MaintenanceRunStepsItemStatus(StrEnum):
    DONE = "done"
    FAILED = "failed"
    PENDING = "pending"
    RUNNING = "running"
    SKIPPED = "skipped"

    def __str__(self) -> str:
        return str(self.value)
