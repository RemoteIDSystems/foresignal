from enum import StrEnum


class GetSeriesPointsFormat(StrEnum):
    CSV = "csv"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)
