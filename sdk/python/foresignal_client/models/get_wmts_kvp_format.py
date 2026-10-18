from enum import StrEnum


class GetWmtsKvpFORMAT(StrEnum):
    IMAGEPNG = "image/png"
    IMAGEWEBP = "image/webp"

    def __str__(self) -> str:
        return str(self.value)
