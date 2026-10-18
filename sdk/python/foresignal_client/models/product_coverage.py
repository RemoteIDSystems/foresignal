from enum import StrEnum


class ProductCoverage(StrEnum):
    CONUS = "CONUS"
    GLOBAL = "global"
    STATION_DISC = "station-disc"
    SWATH = "swath"

    def __str__(self) -> str:
        return str(self.value)
