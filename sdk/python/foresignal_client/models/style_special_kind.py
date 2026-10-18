from enum import StrEnum


class StyleSpecialKind(StrEnum):
    NOCOVERAGE = "noCoverage"
    NODATA = "noData"
    NOECHO = "noEcho"

    def __str__(self) -> str:
        return str(self.value)
