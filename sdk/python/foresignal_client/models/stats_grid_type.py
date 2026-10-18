from enum import StrEnum


class StatsGridType(StrEnum):
    FEATURECOLLECTION = "FeatureCollection"

    def __str__(self) -> str:
        return str(self.value)
