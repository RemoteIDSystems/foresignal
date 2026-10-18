from enum import StrEnum


class NarrativeSummaryState(StrEnum):
    ACTIVE = "active"
    DORMANT = "dormant"
    EMERGING = "emerging"
    FADING = "fading"

    def __str__(self) -> str:
        return str(self.value)
