from enum import StrEnum


class AnalystProfileNotifyMinPriority(StrEnum):
    HIGH = "high"
    LOW = "low"
    NORMAL = "normal"

    def __str__(self) -> str:
        return str(self.value)
