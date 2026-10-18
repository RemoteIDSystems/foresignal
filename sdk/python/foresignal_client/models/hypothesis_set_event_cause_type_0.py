from enum import StrEnum


class HypothesisSetEventCauseType0(StrEnum):
    BASE_RATE_REFRESH = "base-rate-refresh"
    CREATED = "created"
    EVIDENCE_ADDED = "evidence-added"
    EVIDENCE_REMOVED = "evidence-removed"
    INDICATOR_ABSENT = "indicator-absent"
    INDICATOR_OBSERVED = "indicator-observed"
    OPERATOR = "operator"
    REVIEW = "review"

    def __str__(self) -> str:
        return str(self.value)
