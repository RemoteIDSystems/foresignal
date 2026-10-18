from enum import StrEnum


class ProductExportRequestFormat(StrEnum):
    COG = "cog"
    MBTILES = "mbtiles"
    PMTILES = "pmtiles"

    def __str__(self) -> str:
        return str(self.value)
