from enum import StrEnum


class SeriesCatalogEntryFamily(StrEnum):
    ATTENTION = "attention"
    COUNT = "count"
    INDEX = "index"
    OPEN_INTEREST = "open-interest"
    POSITION = "position"
    PRICE = "price"
    RATE = "rate"
    STRUCTURAL = "structural"
    TONE = "tone"
    VOLUME = "volume"

    def __str__(self) -> str:
        return str(self.value)
