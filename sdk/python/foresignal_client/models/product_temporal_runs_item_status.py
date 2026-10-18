from enum import StrEnum


class ProductTemporalRunsItemStatus(StrEnum):
    COMPLETE = "complete"
    PARTIAL = "partial"

    def __str__(self) -> str:
        return str(self.value)
