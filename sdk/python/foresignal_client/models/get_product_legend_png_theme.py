from enum import StrEnum


class GetProductLegendPngTheme(StrEnum):
    DARK = "dark"
    LIGHT = "light"

    def __str__(self) -> str:
        return str(self.value)
