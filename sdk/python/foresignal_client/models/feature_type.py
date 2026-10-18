from enum import StrEnum


class FeatureType(StrEnum):
    FEATURE = "Feature"

    def __str__(self) -> str:
        return str(self.value)
