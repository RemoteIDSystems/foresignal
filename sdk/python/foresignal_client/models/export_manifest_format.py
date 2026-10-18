from enum import StrEnum


class ExportManifestFormat(StrEnum):
    FLATGEOBUF = "flatgeobuf"
    GEOPACKAGE = "geopackage"
    GEOPARQUET = "geoparquet"
    GPX = "gpx"
    PMTILES = "pmtiles"

    def __str__(self) -> str:
        return str(self.value)
