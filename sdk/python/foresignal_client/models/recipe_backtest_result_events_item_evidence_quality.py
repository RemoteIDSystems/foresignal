from enum import StrEnum


class RecipeBacktestResultEventsItemEvidenceQuality(StrEnum):
    INSUFFICIENT = "insufficient"
    MODERATE = "moderate"
    STRONG = "strong"
    WEAK = "weak"

    def __str__(self) -> str:
        return str(self.value)
