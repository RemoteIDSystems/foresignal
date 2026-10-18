from enum import StrEnum


class EntityAliasOrigin(StrEnum):
    CO_ASSERTED = "CO_ASSERTED"
    OBSERVED = "OBSERVED"
    OPERATOR = "OPERATOR"
    REGISTRY = "REGISTRY"

    def __str__(self) -> str:
        return str(self.value)
