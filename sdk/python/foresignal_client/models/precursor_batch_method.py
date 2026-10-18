from enum import StrEnum


class PrecursorBatchMethod(StrEnum):
    BH = "BH"
    BY = "BY"

    def __str__(self) -> str:
        return str(self.value)
