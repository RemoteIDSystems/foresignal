from enum import StrEnum


class PrecursorTestResultVerdict(StrEnum):
    INSUFFICIENT = "insufficient"
    NOTABLE = "notable"
    NO_EVIDENCE_OF_PRECEDENCE = "no-evidence-of-precedence"
    WEAK = "weak"

    def __str__(self) -> str:
        return str(self.value)
