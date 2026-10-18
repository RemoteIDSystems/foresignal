from enum import StrEnum


class StyleDeriveType1(StrEnum):
    SPEED = "speed"

    def __str__(self) -> str:
        return str(self.value)
