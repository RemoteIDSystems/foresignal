from enum import StrEnum


class PrecursorWindowStatsVerdict(StrEnum):
    FAILED = "failed"
    INSUFFICIENT = "insufficient"
    NOTABLE = "notable"
    NO_EVIDENCE_OF_PRECEDENCE = "no-evidence-of-precedence"
    VALIDATED = "validated"
    WEAK = "weak"

    def __str__(self) -> str:
        return str(self.value)
