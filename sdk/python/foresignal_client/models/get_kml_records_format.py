from enum import StrEnum


class GetKmlRecordsFormat(StrEnum):
    KML = "kml"
    KMZ = "kmz"

    def __str__(self) -> str:
        return str(self.value)
