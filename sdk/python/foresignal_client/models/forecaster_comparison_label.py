from enum import StrEnum


class ForecasterComparisonLabel(StrEnum):
    INSUFFICIENT = "insufficient"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)
