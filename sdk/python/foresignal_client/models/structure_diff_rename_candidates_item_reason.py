from enum import StrEnum


class StructureDiffRenameCandidatesItemReason(StrEnum):
    NAME_SIMILARITY = "name-similarity"
    SNAKE_CAMEL = "snake-camel"
    VALUE_DISTRIBUTION = "value-distribution"

    def __str__(self) -> str:
        return str(self.value)
