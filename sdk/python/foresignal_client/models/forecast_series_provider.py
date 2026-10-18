from enum import StrEnum


class ForecastSeriesProvider(StrEnum):
    AIFS = "aifs"
    GFS = "gfs"
    HRRR = "hrrr"
    IFS = "ifs"
    NWS = "nws"

    def __str__(self) -> str:
        return str(self.value)
