from enum import StrEnum


class StatsGridFeaturesItemType(StrEnum):
    FEATURE = "Feature"

    def __str__(self) -> str:
        return str(self.value)
