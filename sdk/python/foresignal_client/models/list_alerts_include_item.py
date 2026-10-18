from enum import StrEnum


class ListAlertsIncludeItem(StrEnum):
    CONTEXT = "context"

    def __str__(self) -> str:
        return str(self.value)
