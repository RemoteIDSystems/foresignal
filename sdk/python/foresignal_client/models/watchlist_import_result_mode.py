from enum import StrEnum


class WatchlistImportResultMode(StrEnum):
    MERGE = "merge"
    REPLACE = "replace"

    def __str__(self) -> str:
        return str(self.value)
