from enum import StrEnum


class ExposureRequestPopulation(StrEnum):
    AUTO = "auto"
    OFF = "off"
    REQUIRED = "required"

    def __str__(self) -> str:
        return str(self.value)
