from enum import StrEnum


class StyleType(StrEnum):
    CATEGORICAL = "categorical"
    CONTINUOUS = "continuous"
    DISCRETE = "discrete"

    def __str__(self) -> str:
        return str(self.value)
