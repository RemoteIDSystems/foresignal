from enum import StrEnum


class GetProductTileOnExpired(StrEnum):
    TRANSPARENT = "transparent"

    def __str__(self) -> str:
        return str(self.value)
