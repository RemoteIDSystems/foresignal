from enum import StrEnum


class PrecursorCorrelationSignType2Type1(StrEnum):
    NEGATIVE = "negative"
    POSITIVE = "positive"

    def __str__(self) -> str:
        return str(self.value)
