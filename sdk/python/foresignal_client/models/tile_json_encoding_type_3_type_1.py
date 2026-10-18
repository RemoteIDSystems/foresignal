from enum import StrEnum


class TileJsonEncodingType3Type1(StrEnum):
    CUSTOM = "custom"
    TERRARIUM = "terrarium"

    def __str__(self) -> str:
        return str(self.value)
