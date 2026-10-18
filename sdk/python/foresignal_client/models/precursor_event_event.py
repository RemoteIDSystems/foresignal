from enum import StrEnum


class PrecursorEventEvent(StrEnum):
    PRECURSOR_RETIRED = "precursor.retired"
    PRECURSOR_STALE = "precursor.stale"
    PRECURSOR_VALIDATED = "precursor.validated"

    def __str__(self) -> str:
        return str(self.value)
