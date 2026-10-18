from enum import StrEnum


class BenchmarkStatusComponentsItemGatingLlmEnsembleWeight(StrEnum):
    ALLOWED = "allowed"
    DEGRADED = "degraded"

    def __str__(self) -> str:
        return str(self.value)
