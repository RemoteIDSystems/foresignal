from enum import StrEnum


class AnalyticFindingStatus(StrEnum):
    DISMISSED = "dismissed"
    OPEN = "open"
    SUPERSEDED = "superseded"

    def __str__(self) -> str:
        return str(self.value)
