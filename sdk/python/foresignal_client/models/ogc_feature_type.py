from enum import StrEnum


class OgcFeatureType(StrEnum):
    FEATURE = "Feature"

    def __str__(self) -> str:
        return str(self.value)
