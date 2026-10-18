from enum import StrEnum


class BenchmarkStatusComponentsItemGatingProposerAutoExecution(StrEnum):
    ALLOWED = "allowed"
    DEGRADED = "degraded"

    def __str__(self) -> str:
        return str(self.value)
