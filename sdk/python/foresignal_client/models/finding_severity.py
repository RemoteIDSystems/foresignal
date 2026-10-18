from enum import StrEnum


class FindingSeverity(StrEnum):
    INFO = "info"
    NOTABLE = "notable"
    SIGNIFICANT = "significant"

    def __str__(self) -> str:
        return str(self.value)
