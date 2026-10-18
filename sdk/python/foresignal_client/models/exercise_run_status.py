from enum import StrEnum


class ExerciseRunStatus(StrEnum):
    CANCELLED = "CANCELLED"
    DONE = "DONE"
    FAILED = "FAILED"
    PLANNED = "PLANNED"
    RUNNING = "RUNNING"

    def __str__(self) -> str:
        return str(self.value)
