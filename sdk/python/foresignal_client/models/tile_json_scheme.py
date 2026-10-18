from enum import StrEnum


class TileJsonScheme(StrEnum):
    XYZ = "xyz"

    def __str__(self) -> str:
        return str(self.value)
