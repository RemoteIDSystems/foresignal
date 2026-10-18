from enum import StrEnum


class ExportInvestigationFormat(StrEnum):
    GEOJSON = "geojson"
    JSON = "json"
    MD = "md"

    def __str__(self) -> str:
        return str(self.value)
