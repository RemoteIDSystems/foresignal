from enum import StrEnum


class ExportSpecMode(StrEnum):
    ASOF = "asOf"
    ATSEQ = "atSeq"
    CURRENT = "current"
    HISTORY = "history"

    def __str__(self) -> str:
        return str(self.value)
