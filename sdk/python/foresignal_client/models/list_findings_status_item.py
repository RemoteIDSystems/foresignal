from enum import StrEnum


class ListFindingsStatusItem(StrEnum):
    DISMISSED = "dismissed"
    OPEN = "open"
    SUPERSEDED = "superseded"

    def __str__(self) -> str:
        return str(self.value)
