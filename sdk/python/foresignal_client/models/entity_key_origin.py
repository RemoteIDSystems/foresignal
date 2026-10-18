from enum import StrEnum


class EntityKeyOrigin(StrEnum):
    CO_ASSERTED = "CO_ASSERTED"
    OBSERVED = "OBSERVED"
    OPERATOR = "OPERATOR"
    REGISTRY = "REGISTRY"

    def __str__(self) -> str:
        return str(self.value)
