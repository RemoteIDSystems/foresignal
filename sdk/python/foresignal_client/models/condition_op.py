from enum import StrEnum


class ConditionOp(StrEnum):
    EXISTS = "exists"
    IN = "in"
    INWATCHLIST = "inWatchlist"
    MATCHES = "matches"
    NOTIN = "notIn"
    NOTINWATCHLIST = "notInWatchlist"
    STARTSWITH = "startsWith"
    VALUE_0 = ">"
    VALUE_1 = ">="
    VALUE_2 = "<"
    VALUE_3 = "<="
    VALUE_4 = "=="
    VALUE_5 = "!="

    def __str__(self) -> str:
        return str(self.value)
