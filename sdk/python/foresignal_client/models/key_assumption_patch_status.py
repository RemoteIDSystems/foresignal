from enum import StrEnum


class KeyAssumptionPatchStatus(StrEnum):
    BROKEN = "broken"
    CAVEATED = "caveated"
    SOLID = "solid"
    UNSUPPORTED = "unsupported"

    def __str__(self) -> str:
        return str(self.value)
