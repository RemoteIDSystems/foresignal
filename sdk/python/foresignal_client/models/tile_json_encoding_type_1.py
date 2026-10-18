from enum import StrEnum


class TileJsonEncodingType1(StrEnum):
    CUSTOM = "custom"
    TERRARIUM = "terrarium"

    def __str__(self) -> str:
        return str(self.value)
