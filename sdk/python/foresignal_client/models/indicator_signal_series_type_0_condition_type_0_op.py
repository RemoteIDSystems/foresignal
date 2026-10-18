from enum import StrEnum


class IndicatorSignalSeriesType0ConditionType0Op(StrEnum):
    GT = "gt"
    GTE = "gte"
    LT = "lt"
    LTE = "lte"

    def __str__(self) -> str:
        return str(self.value)
