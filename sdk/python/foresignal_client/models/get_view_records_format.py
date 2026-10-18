from enum import StrEnum


class GetViewRecordsFormat(StrEnum):
    CSV = "csv"
    GEOJSON = "geojson"
    GEOJSONSEQ = "geojsonseq"
    JSON = "json"
    NDJSON = "ndjson"

    def __str__(self) -> str:
        return str(self.value)
