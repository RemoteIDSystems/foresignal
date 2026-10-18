from enum import StrEnum


class ForecastQuestionSummaryType(StrEnum):
    BINARY = "binary"
    MULTIPLE = "multiple"
    NUMERIC = "numeric"
    ORDERED = "ordered"

    def __str__(self) -> str:
        return str(self.value)
