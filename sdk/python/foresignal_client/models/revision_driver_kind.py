from enum import StrEnum


class RevisionDriverKind(StrEnum):
    BASE_RATE = "base-rate"
    EVIDENCE = "evidence"
    INDICATOR = "indicator"
    OPERATOR = "operator"
    REVIEW = "review"

    def __str__(self) -> str:
        return str(self.value)
