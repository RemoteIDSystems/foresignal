from enum import StrEnum


class HypothesisSetEventStatus(StrEnum):
    ACTIVE = "active"
    ARCHIVED = "archived"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
