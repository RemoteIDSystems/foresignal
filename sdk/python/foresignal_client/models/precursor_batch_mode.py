from enum import StrEnum


class PrecursorBatchMode(StrEnum):
    MINE = "mine"
    RETEST = "retest"

    def __str__(self) -> str:
        return str(self.value)
