from enum import StrEnum


class ExportRecordFormat(StrEnum):
    GEOJSON = "geojson"
    GPX = "gpx"
    NDJSON = "ndjson"

    def __str__(self) -> str:
        return str(self.value)
