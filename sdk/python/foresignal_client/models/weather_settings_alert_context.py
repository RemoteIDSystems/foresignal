from enum import StrEnum


class WeatherSettingsAlertContext(StrEnum):
    AUTO = "auto"
    OFF = "off"

    def __str__(self) -> str:
        return str(self.value)
