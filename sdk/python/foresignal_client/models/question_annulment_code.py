from enum import StrEnum


class QuestionAnnulmentCode(StrEnum):
    COVERAGE_GAP = "COVERAGE_GAP"
    LOW_DETECTABILITY = "LOW_DETECTABILITY"
    OPERATOR = "OPERATOR"

    def __str__(self) -> str:
        return str(self.value)
