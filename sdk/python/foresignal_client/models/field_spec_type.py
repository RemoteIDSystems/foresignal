from enum import StrEnum


class FieldSpecType(StrEnum):
    ARRAY = "ARRAY"
    BOOLEAN = "BOOLEAN"
    INTEGER = "INTEGER"
    NUMBER = "NUMBER"
    OBJECT = "OBJECT"
    STRING = "STRING"
    TIMESTAMP = "TIMESTAMP"

    def __str__(self) -> str:
        return str(self.value)
