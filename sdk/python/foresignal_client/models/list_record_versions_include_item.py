from enum import StrEnum


class ListRecordVersionsIncludeItem(StrEnum):
    OBSERVER = "observer"

    def __str__(self) -> str:
        return str(self.value)
