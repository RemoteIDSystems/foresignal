from enum import StrEnum


class PrecursorCandidateInputKind(StrEnum):
    EVENT = "event"
    SERIES = "series"

    def __str__(self) -> str:
        return str(self.value)
