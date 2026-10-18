from enum import StrEnum


class AnalystRunStatus(StrEnum):
    BUDGET_EXCEEDED = "budget_exceeded"
    FAILED = "failed"
    NOOP = "noop"
    OK = "ok"
    PARTIAL = "partial"
    QUEUED = "queued"
    RUNNING = "running"

    def __str__(self) -> str:
        return str(self.value)
