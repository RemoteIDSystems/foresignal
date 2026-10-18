from enum import StrEnum


class ClaimPolarity(StrEnum):
    ASSERTED = "asserted"
    DENIED = "denied"
    QUESTIONED = "questioned"

    def __str__(self) -> str:
        return str(self.value)
