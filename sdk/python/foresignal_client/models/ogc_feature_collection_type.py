from enum import StrEnum


class OgcFeatureCollectionType(StrEnum):
    FEATURECOLLECTION = "FeatureCollection"

    def __str__(self) -> str:
        return str(self.value)
