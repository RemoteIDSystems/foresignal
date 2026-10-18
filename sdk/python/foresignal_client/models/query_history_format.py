from enum import StrEnum


class QueryHistoryFormat(StrEnum):
    CSV = "csv"
    CZML = "czml"
    GEOJSON = "geojson"
    GEOJSONSEQ = "geojsonseq"
    JSON = "json"
    NDJSON = "ndjson"

    def __str__(self) -> str:
        return str(self.value)
