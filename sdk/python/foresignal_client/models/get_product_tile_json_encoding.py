from enum import StrEnum


class GetProductTileJsonEncoding(StrEnum):
    CUSTOM = "custom"
    TERRARIUM = "terrarium"

    def __str__(self) -> str:
        return str(self.value)
