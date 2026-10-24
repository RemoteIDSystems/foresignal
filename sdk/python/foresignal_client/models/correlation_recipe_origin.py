from enum import StrEnum


class CorrelationRecipeOrigin(StrEnum):
    BUNDLED = "bundled"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
