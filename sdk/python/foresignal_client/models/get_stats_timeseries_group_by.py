from enum import StrEnum


class GetStatsTimeseriesGroupBy(StrEnum):
    KIND = "kind"
    NONE = "none"
    REGION = "region"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)
