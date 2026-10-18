from enum import StrEnum


class StatsGridBackend(StrEnum):
    RAW = "raw"
    ROLLUP = "rollup"

    def __str__(self) -> str:
        return str(self.value)
