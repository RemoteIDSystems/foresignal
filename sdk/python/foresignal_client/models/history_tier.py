from enum import StrEnum


class HistoryTier(StrEnum):
    STANDARD = "STANDARD"
    TRACK = "TRACK"

    def __str__(self) -> str:
        return str(self.value)
