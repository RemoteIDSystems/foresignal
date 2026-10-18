from enum import StrEnum


class WatchlistInputNormalize(StrEnum):
    DIGITS = "digits"
    LOWER = "lower"
    NONE = "none"
    UPPER = "upper"

    def __str__(self) -> str:
        return str(self.value)
