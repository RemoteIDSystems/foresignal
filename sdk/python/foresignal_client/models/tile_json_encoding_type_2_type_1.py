from enum import StrEnum


class TileJsonEncodingType2Type1(StrEnum):
    CUSTOM = "custom"
    TERRARIUM = "terrarium"

    def __str__(self) -> str:
        return str(self.value)
