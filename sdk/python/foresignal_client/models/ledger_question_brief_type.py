from enum import StrEnum


class LedgerQuestionBriefType(StrEnum):
    BINARY = "binary"
    MULTIPLE = "multiple"
    NUMERIC = "numeric"
    ORDERED = "ordered"

    def __str__(self) -> str:
        return str(self.value)
