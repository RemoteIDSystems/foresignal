from enum import StrEnum


class ChronicleExportRequestFormat(StrEnum):
    GEOPARQUET = "geoparquet"
    GPKG = "gpkg"
    JSON_BUNDLE = "json-bundle"

    def __str__(self) -> str:
        return str(self.value)
