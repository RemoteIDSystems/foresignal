from enum import StrEnum


class BriefingRunStatus(StrEnum):
    FAILED = "failed"
    FALLBACK = "fallback"
    RUNNING = "running"
    SUCCEEDED = "succeeded"

    def __str__(self) -> str:
        return str(self.value)
