from enum import StrEnum


class ListIntelEventsStatusItem(StrEnum):
    ACTIVE = "active"
    DISMISSED = "dismissed"
    EMERGING = "emerging"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
