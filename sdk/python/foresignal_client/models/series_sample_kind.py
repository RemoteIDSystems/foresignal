from enum import StrEnum


class SeriesSampleKind(StrEnum):
    FORECAST = "forecast"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)
