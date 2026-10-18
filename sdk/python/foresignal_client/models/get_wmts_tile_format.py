from enum import StrEnum


class GetWmtsTileFormat(StrEnum):
    PNG = "png"
    WEBP = "webp"

    def __str__(self) -> str:
        return str(self.value)
