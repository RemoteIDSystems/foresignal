from enum import StrEnum


class IndexChangedEventWeightsOrigin(StrEnum):
    DEFAULT = "default"
    FITTED = "fitted"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
