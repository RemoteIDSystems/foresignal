from enum import StrEnum


class PrecursorCorrelationSignType1(StrEnum):
    NEGATIVE = "negative"
    POSITIVE = "positive"

    def __str__(self) -> str:
        return str(self.value)
