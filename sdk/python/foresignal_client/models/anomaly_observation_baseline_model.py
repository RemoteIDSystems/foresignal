from enum import StrEnum


class AnomalyObservationBaselineModel(StrEnum):
    NB = "nb"
    POISSON = "poisson"

    def __str__(self) -> str:
        return str(self.value)
