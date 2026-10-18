from enum import StrEnum


class IntelEventSummaryStatus(StrEnum):
    ACTIVE = "active"
    DISMISSED = "dismissed"
    EMERGING = "emerging"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
