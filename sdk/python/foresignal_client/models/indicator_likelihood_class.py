from enum import StrEnum


class IndicatorLikelihoodClass(StrEnum):
    HIGH = "high"
    LOW = "low"
    MEDIUM = "medium"
    NEUTRAL = "neutral"

    def __str__(self) -> str:
        return str(self.value)
