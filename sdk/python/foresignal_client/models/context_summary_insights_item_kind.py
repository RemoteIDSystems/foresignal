from enum import StrEnum


class ContextSummaryInsightsItemKind(StrEnum):
    BAND_CHANGE = "band-change"
    FINDING = "finding"
    FORECAST = "forecast"

    def __str__(self) -> str:
        return str(self.value)
