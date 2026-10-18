from enum import StrEnum


class AchEvidenceCaveatsItem(StrEnum):
    CIRCULAR_REPORTING = "CIRCULAR_REPORTING"

    def __str__(self) -> str:
        return str(self.value)
