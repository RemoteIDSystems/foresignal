from enum import StrEnum


class PrecursorEventPrecursorStatus(StrEnum):
    CANDIDATE = "candidate"
    RETIRED = "retired"
    STALE = "stale"
    VALIDATED = "validated"

    def __str__(self) -> str:
        return str(self.value)
