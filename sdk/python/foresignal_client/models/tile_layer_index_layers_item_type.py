from enum import StrEnum


class TileLayerIndexLayersItemType(StrEnum):
    KIND = "kind"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)
