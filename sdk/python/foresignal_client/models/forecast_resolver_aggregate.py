from enum import StrEnum


class ForecastResolverAggregate(StrEnum):
    LAST = "last"
    MAX = "max"
    MEAN = "mean"
    SUM = "sum"

    def __str__(self) -> str:
        return str(self.value)
