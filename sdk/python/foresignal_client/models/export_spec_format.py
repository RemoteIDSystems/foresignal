from enum import StrEnum


class ExportSpecFormat(StrEnum):
    FLATGEOBUF = "flatgeobuf"
    GEOPACKAGE = "geopackage"
    GEOPARQUET = "geoparquet"
    GPX = "gpx"

    def __str__(self) -> str:
        return str(self.value)
