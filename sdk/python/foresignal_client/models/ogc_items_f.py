from enum import StrEnum


class OgcItemsF(StrEnum):
    GEOJSON = "geojson"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)
