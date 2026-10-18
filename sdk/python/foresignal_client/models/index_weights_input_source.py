from enum import StrEnum


class IndexWeightsInputSource(StrEnum):
    DEFAULT = "default"
    FITTED = "fitted"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
