from enum import StrEnum


class GetRecordTrackFormat(StrEnum):
    CZML = "czml"
    GEOJSON = "geojson"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)
