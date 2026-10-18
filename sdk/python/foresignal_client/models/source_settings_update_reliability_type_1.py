from enum import StrEnum


class SourceSettingsUpdateReliabilityType1(StrEnum):
    A = "A"
    B = "B"
    C = "C"
    D = "D"
    E = "E"
    F = "F"

    def __str__(self) -> str:
        return str(self.value)
