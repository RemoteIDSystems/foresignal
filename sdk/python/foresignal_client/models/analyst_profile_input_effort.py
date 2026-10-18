from enum import StrEnum


class AnalystProfileInputEffort(StrEnum):
    LOW = "low"
    MEDIUM = "medium"

    def __str__(self) -> str:
        return str(self.value)
