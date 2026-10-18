from enum import StrEnum


class ReplayResultOutcomeType2Type1(StrEnum):
    DIFFERS = "differs"
    IDENTICAL = "identical"
    NOT_REPLAYABLE = "not-replayable"

    def __str__(self) -> str:
        return str(self.value)
