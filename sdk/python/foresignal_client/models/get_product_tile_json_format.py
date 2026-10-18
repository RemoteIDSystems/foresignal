from enum import StrEnum


class GetProductTileJsonFormat(StrEnum):
    PNG = "png"
    WEBP = "webp"

    def __str__(self) -> str:
        return str(self.value)
