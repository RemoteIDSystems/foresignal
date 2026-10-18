from enum import StrEnum


class ListSourceFetchesTriggerItem(StrEnum):
    MANUAL = "MANUAL"
    PUSH = "PUSH"
    SCHEDULED = "SCHEDULED"
    STREAM = "STREAM"

    def __str__(self) -> str:
        return str(self.value)
