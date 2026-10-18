from enum import StrEnum


class GetProductTileTmsOnExpired(StrEnum):
    TRANSPARENT = "transparent"

    def __str__(self) -> str:
        return str(self.value)
