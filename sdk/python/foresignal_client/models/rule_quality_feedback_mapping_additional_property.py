from enum import StrEnum


class RuleQualityFeedbackMappingAdditionalProperty(StrEnum):
    NEGATIVE = "negative"
    NEUTRAL = "neutral"
    POSITIVE = "positive"

    def __str__(self) -> str:
        return str(self.value)
