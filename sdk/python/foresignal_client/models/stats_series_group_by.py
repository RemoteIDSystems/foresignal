from enum import StrEnum


class StatsSeriesGroupBy(StrEnum):
    KIND = "kind"
    NONE = "none"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)
