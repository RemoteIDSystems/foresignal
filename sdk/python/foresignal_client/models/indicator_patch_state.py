from enum import StrEnum


class IndicatorPatchState(StrEnum):
    ABSENT = "absent"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)
