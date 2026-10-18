from enum import StrEnum


class ListProductsCategory(StrEnum):
    IMAGERY = "imagery"
    LIGHTNING = "lightning"
    MODEL = "model"
    RADAR = "radar"
    SATELLITE = "satellite"

    def __str__(self) -> str:
        return str(self.value)
