from enum import StrEnum


class GetStatsGridMode(StrEnum):
    RAW = "raw"
    ROLLUP = "rollup"

    def __str__(self) -> str:
        return str(self.value)
