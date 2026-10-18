from enum import StrEnum


class SensorSensorKind(StrEnum):
    RASTER_PRODUCT = "raster-product"
    RECEIVER = "receiver"
    SATELLITE = "satellite"
    SCENE = "scene"
    SOURCE = "source"
    STATION = "station"

    def __str__(self) -> str:
        return str(self.value)
