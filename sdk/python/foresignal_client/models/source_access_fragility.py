from enum import StrEnum


class SourceAccessFragility(StrEnum):
    HIGH = "high"
    LOW = "low"
    MEDIUM = "medium"

    def __str__(self) -> str:
        return str(self.value)
