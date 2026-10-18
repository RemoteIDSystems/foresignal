from enum import StrEnum


class SeriesCatalogEntryTransform(StrEnum):
    DIFFERENCE = "difference"
    LEVEL = "level"
    LOG_LEVEL = "log-level"
    LOG_RETURN = "log-return"

    def __str__(self) -> str:
        return str(self.value)
