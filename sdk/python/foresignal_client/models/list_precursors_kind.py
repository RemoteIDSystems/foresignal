from enum import StrEnum


class ListPrecursorsKind(StrEnum):
    EVENT = "event"
    SERIES = "series"

    def __str__(self) -> str:
        return str(self.value)
