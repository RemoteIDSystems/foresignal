from enum import StrEnum


class MediaMatchResultQuerySource(StrEnum):
    PDQ = "pdq"
    UPLOAD = "upload"

    def __str__(self) -> str:
        return str(self.value)
