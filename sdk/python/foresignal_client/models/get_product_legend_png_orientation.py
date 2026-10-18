from enum import StrEnum


class GetProductLegendPngOrientation(StrEnum):
    HORIZONTAL = "horizontal"
    VERTICAL = "vertical"

    def __str__(self) -> str:
        return str(self.value)
