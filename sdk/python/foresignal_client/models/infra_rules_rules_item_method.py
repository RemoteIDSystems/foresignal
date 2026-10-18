from enum import StrEnum


class InfraRulesRulesItemMethod(StrEnum):
    DECLARED = "declared"
    INFERRED = "inferred"
    PROXIMITY = "proximity"
    TOPOLOGICAL = "topological"

    def __str__(self) -> str:
        return str(self.value)
