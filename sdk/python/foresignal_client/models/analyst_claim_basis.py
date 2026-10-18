from enum import StrEnum


class AnalystClaimBasis(StrEnum):
    EVIDENCE = "evidence"
    INFERENCE = "inference"
    SPECULATION = "speculation"

    def __str__(self) -> str:
        return str(self.value)
