from enum import StrEnum


class OverlandBatchLocationsItemType(StrEnum):
    FEATURE = "Feature"

    def __str__(self) -> str:
        return str(self.value)
