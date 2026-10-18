from enum import StrEnum


class RecordMappingSpreadType0Type(StrEnum):
    BOOLEAN = "boolean"
    INTEGER = "integer"
    NUMBER = "number"
    STRING = "string"

    def __str__(self) -> str:
        return str(self.value)
