from enum import StrEnum


class AnomalyBaselineSamplePath(StrEnum):
    ACTIVITY_ROLLUP = "activity_rollup"
    ACTIVITY_SAMPLE = "activity_sample"

    def __str__(self) -> str:
        return str(self.value)
