from enum import StrEnum


class ListHypothesisSetsStatus(StrEnum):
    ACTIVE = "active"
    ARCHIVED = "archived"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
