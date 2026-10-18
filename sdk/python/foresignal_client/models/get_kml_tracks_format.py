from enum import StrEnum


class GetKmlTracksFormat(StrEnum):
    KML = "kml"
    KMZ = "kmz"

    def __str__(self) -> str:
        return str(self.value)
