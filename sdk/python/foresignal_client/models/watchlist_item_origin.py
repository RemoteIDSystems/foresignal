from enum import StrEnum


class WatchlistItemOrigin(StrEnum):
    CSV = "csv"
    JSON = "json"
    MANUAL = "manual"

    def __str__(self) -> str:
        return str(self.value)
