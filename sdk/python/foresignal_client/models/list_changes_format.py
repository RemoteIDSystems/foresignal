from enum import StrEnum


class ListChangesFormat(StrEnum):
    CSV = "csv"
    GEOJSONSEQ = "geojsonseq"
    JSON = "json"
    NDJSON = "ndjson"

    def __str__(self) -> str:
        return str(self.value)
