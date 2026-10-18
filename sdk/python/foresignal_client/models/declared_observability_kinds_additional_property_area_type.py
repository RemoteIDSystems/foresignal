from enum import StrEnum


class DeclaredObservabilityKindsAdditionalPropertyAreaType(StrEnum):
    BBOX = "bbox"
    GLOBAL = "global"
    REGIONS = "regions"

    def __str__(self) -> str:
        return str(self.value)
