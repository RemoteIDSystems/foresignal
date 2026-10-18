from enum import StrEnum


class GetStaticMapTheme(StrEnum):
    DARK = "dark"
    LIGHT = "light"

    def __str__(self) -> str:
        return str(self.value)
