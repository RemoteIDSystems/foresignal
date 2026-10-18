from enum import StrEnum


class KeyAssumptionStatus(StrEnum):
    BROKEN = "broken"
    CAVEATED = "caveated"
    SOLID = "solid"
    UNSUPPORTED = "unsupported"

    def __str__(self) -> str:
        return str(self.value)
