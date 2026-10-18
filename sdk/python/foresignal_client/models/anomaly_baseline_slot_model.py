from enum import StrEnum


class AnomalyBaselineSlotModel(StrEnum):
    NB = "nb"
    POISSON = "poisson"

    def __str__(self) -> str:
        return str(self.value)
