from enum import StrEnum


class MappingFieldType(StrEnum):
    BOOLEAN = "boolean"
    INTEGER = "integer"
    NUMBER = "number"
    STRING = "string"

    def __str__(self) -> str:
        return str(self.value)
