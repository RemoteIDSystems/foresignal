from enum import StrEnum


class AnalogResultMethod(StrEnum):
    FEATURE_ONLY = "feature-only"
    FUSED = "fused"

    def __str__(self) -> str:
        return str(self.value)
