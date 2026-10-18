from enum import StrEnum


class EntityKeyRole(StrEnum):
    ANCHOR = "ANCHOR"
    WEAK = "WEAK"

    def __str__(self) -> str:
        return str(self.value)
