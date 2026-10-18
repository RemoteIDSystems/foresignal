from enum import StrEnum


class GetForecastQuestionHistoryStep(StrEnum):
    VALUE_0 = "1d"

    def __str__(self) -> str:
        return str(self.value)
