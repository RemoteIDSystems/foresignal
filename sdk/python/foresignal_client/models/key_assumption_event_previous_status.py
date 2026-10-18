from enum import StrEnum


class KeyAssumptionEventPreviousStatus(StrEnum):
    BROKEN = "broken"
    CAVEATED = "caveated"
    SOLID = "solid"
    UNSUPPORTED = "unsupported"

    def __str__(self) -> str:
        return str(self.value)
