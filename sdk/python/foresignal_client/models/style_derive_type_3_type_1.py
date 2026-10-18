from enum import StrEnum


class StyleDeriveType3Type1(StrEnum):
    SPEED = "speed"

    def __str__(self) -> str:
        return str(self.value)
