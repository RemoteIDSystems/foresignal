from enum import StrEnum


class AssessmentSectionsItemKey(StrEnum):
    BLUF = "bluf"
    COLLECTION_GAPS = "collection-gaps"
    FORECASTS = "forecasts"
    HYPOTHESES = "hypotheses"
    INDICATORS = "indicators"
    KEY_INSIGHTS = "key-insights"
    OUTLOOK = "outlook"
    RISKS = "risks"
    SITUATIONS = "situations"
    SOCIAL = "social"
    SOURCES = "sources"
    SPECULATION = "speculation"
    WHAT_CHANGED = "what-changed"

    def __str__(self) -> str:
        return str(self.value)
