from enum import StrEnum


class ForecastHistoryType(StrEnum):
    BINARY = "binary"
    MULTIPLE = "multiple"
    NUMERIC = "numeric"
    ORDERED = "ordered"

    def __str__(self) -> str:
        return str(self.value)
