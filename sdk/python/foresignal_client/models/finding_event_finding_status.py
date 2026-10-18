from enum import StrEnum


class FindingEventFindingStatus(StrEnum):
    DISMISSED = "dismissed"
    OPEN = "open"
    SUPERSEDED = "superseded"

    def __str__(self) -> str:
        return str(self.value)
