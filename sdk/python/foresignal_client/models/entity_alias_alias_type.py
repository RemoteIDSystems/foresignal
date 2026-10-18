from enum import StrEnum


class EntityAliasAliasType(StrEnum):
    FORMER_NAME = "FORMER_NAME"
    NAME = "NAME"
    OPERATOR_LABEL = "OPERATOR_LABEL"

    def __str__(self) -> str:
        return str(self.value)
