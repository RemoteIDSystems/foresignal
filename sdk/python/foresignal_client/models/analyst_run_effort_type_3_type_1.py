from enum import StrEnum


class AnalystRunEffortType3Type1(StrEnum):
    LOW = "low"
    MEDIUM = "medium"

    def __str__(self) -> str:
        return str(self.value)
