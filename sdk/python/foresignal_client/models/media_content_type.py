from enum import StrEnum


class MediaContentType(StrEnum):
    APPLICATIONCZMLJSON = "application/czml+json"
    AUDIOMPEG = "audio/mpeg"
    AUDIOOGG = "audio/ogg"
    IMAGEJPEG = "image/jpeg"
    IMAGEPNG = "image/png"
    IMAGETIFF = "image/tiff"

    def __str__(self) -> str:
        return str(self.value)
