from enum import StrEnum


class IndicatorLikelihoodDirection(StrEnum):
    SUPPORTS = "supports"
    WEAKENS = "weakens"

    def __str__(self) -> str:
        return str(self.value)
