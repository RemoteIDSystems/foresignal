from enum import StrEnum


class ClaimFramingLabelType2Type1(StrEnum):
    CHARGED = "charged"
    INFLAMMATORY = "inflammatory"
    NEUTRAL = "neutral"

    def __str__(self) -> str:
        return str(self.value)
