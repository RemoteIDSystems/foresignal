from enum import StrEnum


class StyleDeriveType2Type1(StrEnum):
    SPEED = "speed"

    def __str__(self) -> str:
        return str(self.value)
