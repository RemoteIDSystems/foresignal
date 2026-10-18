from enum import StrEnum


class GetProductTileTmsFormat(StrEnum):
    PNG = "png"
    WEBP = "webp"

    def __str__(self) -> str:
        return str(self.value)
