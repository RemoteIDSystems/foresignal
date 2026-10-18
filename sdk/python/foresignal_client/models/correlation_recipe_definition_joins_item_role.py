from enum import StrEnum


class CorrelationRecipeDefinitionJoinsItemRole(StrEnum):
    CONTEXT = "context"
    CONTRADICTING = "contradicting"
    SUPPORTING = "supporting"

    def __str__(self) -> str:
        return str(self.value)
