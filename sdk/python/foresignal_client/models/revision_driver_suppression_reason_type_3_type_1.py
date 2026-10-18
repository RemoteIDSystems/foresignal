from enum import StrEnum


class RevisionDriverSuppressionReasonType3Type1(StrEnum):
    CAPPED = "capped"
    DAMPED = "damped"
    SAME_EVIDENCE_GROUP = "same-evidence-group"
    SAME_FAMILY_DAY = "same-family-day"

    def __str__(self) -> str:
        return str(self.value)
