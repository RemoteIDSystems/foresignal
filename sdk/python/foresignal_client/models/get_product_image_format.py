from enum import StrEnum


class GetProductImageFormat(StrEnum):
    JPG = "jpg"
    PNG = "png"
    WEBP = "webp"

    def __str__(self) -> str:
        return str(self.value)
