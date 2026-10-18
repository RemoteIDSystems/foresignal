from enum import StrEnum


class AchEvidenceSource(StrEnum):
    ALERT = "alert"
    EVIDENCE_ITEM = "evidence-item"
    FINDING = "finding"
    INDICATOR = "indicator"
    INTEL_EVENT = "intel_event"
    MANUAL = "manual"
    RECORD = "record"
    SOCIAL_CLAIM = "social-claim"

    def __str__(self) -> str:
        return str(self.value)
