from enum import StrEnum


class PolSubjectInputSubjectType(StrEnum):
    ENTITY = "entity"
    RECORD = "record"
    WATCHLIST = "watchlist"

    def __str__(self) -> str:
        return str(self.value)
