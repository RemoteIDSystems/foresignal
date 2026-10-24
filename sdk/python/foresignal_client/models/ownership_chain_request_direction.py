from enum import StrEnum


class OwnershipChainRequestDirection(StrEnum):
    BOTH = "both"
    IN = "in"
    OUT = "out"

    def __str__(self) -> str:
        return str(self.value)
