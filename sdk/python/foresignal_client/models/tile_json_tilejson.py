from enum import StrEnum


class TileJsonTilejson(StrEnum):
    VALUE_0 = "3.0.0"

    def __str__(self) -> str:
        return str(self.value)
