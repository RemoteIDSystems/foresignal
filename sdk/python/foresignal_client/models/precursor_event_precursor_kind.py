from enum import StrEnum


class PrecursorEventPrecursorKind(StrEnum):
    EVENT = "event"
    SERIES = "series"

    def __str__(self) -> str:
        return str(self.value)
