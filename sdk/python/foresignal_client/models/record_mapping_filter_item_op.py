from enum import StrEnum


class RecordMappingFilterItemOp(StrEnum):
    EXISTS = "exists"
    IN = "in"
    VALUE_0 = "=="
    VALUE_1 = "!="

    def __str__(self) -> str:
        return str(self.value)
