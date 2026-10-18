from enum import StrEnum


class GetTileAgg(StrEnum):
    AUTO = "auto"
    GRID = "grid"
    HEX = "hex"
    NONE = "none"

    def __str__(self) -> str:
        return str(self.value)
