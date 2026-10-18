from enum import StrEnum


class AchEvidenceRelevance(StrEnum):
    H = "H"
    L = "L"
    M = "M"

    def __str__(self) -> str:
        return str(self.value)
