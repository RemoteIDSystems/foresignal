from enum import StrEnum


class HypothesisStatus(StrEnum):
    CONFIRMED = "confirmed"
    EXPIRED = "expired"
    OPEN = "open"
    REFUTED = "refuted"

    def __str__(self) -> str:
        return str(self.value)
