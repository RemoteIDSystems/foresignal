from enum import StrEnum


class OutlookWindowStatus(StrEnum):
    ANNULLED = "annulled"
    OPEN = "open"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
