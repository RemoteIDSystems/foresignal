from enum import StrEnum


class ExportJobFormat(StrEnum):
    COG = "cog"
    FLATGEOBUF = "flatgeobuf"
    GEOPACKAGE = "geopackage"
    GEOPARQUET = "geoparquet"
    GPX = "gpx"
    JSON_BUNDLE = "json-bundle"
    MBTILES = "mbtiles"
    PMTILES = "pmtiles"
    ZIP = "zip"

    def __str__(self) -> str:
        return str(self.value)
