from enum import StrEnum


class SourceSummaryMode(StrEnum):
    DERIVED = "derived"
    POLLING = "polling"
    PUSH = "push"
    STREAMING = "streaming"

    def __str__(self) -> str:
        return str(self.value)
