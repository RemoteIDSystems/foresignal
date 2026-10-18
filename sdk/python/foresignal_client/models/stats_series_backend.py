from enum import StrEnum


class StatsSeriesBackend(StrEnum):
    MIXED = "mixed"
    RAW = "raw"
    ROLLUP = "rollup"

    def __str__(self) -> str:
        return str(self.value)
