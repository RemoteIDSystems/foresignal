from enum import StrEnum


class ForecastAreaPayloadModel(StrEnum):
    AIFS = "aifs"
    GFS = "gfs"
    HRRR = "hrrr"
    IFS = "ifs"

    def __str__(self) -> str:
        return str(self.value)
