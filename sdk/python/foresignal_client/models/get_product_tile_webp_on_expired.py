from enum import StrEnum


class GetProductTileWebpOnExpired(StrEnum):
    TRANSPARENT = "transparent"

    def __str__(self) -> str:
        return str(self.value)
