from enum import StrEnum


class AchEvidenceInputSource(StrEnum):
    ALERT = "alert"
    EVIDENCE_ITEM = "evidence-item"
    FINDING = "finding"
    INTEL_EVENT = "intel_event"
    MANUAL = "manual"
    RECORD = "record"
    SOCIAL_CLAIM = "social-claim"

    def __str__(self) -> str:
        return str(self.value)
