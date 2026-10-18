from enum import StrEnum


class IngestRecordsPathKeyFormat(StrEnum):
    ECOWITT = "ecowitt"
    GMC = "gmc"
    GPSLOGGER = "gpslogger"
    MAPPING = "mapping"
    NATIVE = "native"
    OSMAND = "osmand"
    OVERLAND = "overland"
    OWNTRACKS = "owntracks"

    def __str__(self) -> str:
        return str(self.value)
