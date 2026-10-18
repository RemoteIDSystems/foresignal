from enum import StrEnum


class GetProductDataTileEncoding(StrEnum):
    CUSTOM = "custom"
    TERRARIUM = "terrarium"

    def __str__(self) -> str:
        return str(self.value)
