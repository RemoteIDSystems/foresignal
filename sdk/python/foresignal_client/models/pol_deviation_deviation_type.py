from enum import StrEnum


class PolDeviationDeviationType(StrEnum):
    EXPECTED_ABSENT = "EXPECTED_ABSENT"
    NEW_PLACE = "NEW_PLACE"
    UNUSUAL_TIME = "UNUSUAL_TIME"

    def __str__(self) -> str:
        return str(self.value)
