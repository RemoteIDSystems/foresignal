from enum import StrEnum


class ListRecordsSort(StrEnum):
    DISTANCE = "distance"

    def __str__(self) -> str:
        return str(self.value)
