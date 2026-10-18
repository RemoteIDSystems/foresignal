from enum import StrEnum


class IndicatorSignalType(StrEnum):
    INTELEVENT = "intelEvent"
    MANUAL = "manual"
    PRECURSOR = "precursor"
    QUESTION = "question"
    RULE = "rule"
    SERIES = "series"
    SITUATION = "situation"

    def __str__(self) -> str:
        return str(self.value)
