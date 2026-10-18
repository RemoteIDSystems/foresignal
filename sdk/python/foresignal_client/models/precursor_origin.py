from enum import StrEnum


class PrecursorOrigin(StrEnum):
    AUTO = "auto"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
