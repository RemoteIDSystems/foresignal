from enum import IntEnum


class GetSeriesForecastHorizon(IntEnum):
    VALUE_1 = 1
    VALUE_7 = 7
    VALUE_28 = 28

    def __str__(self) -> str:
        return str(self.value)
