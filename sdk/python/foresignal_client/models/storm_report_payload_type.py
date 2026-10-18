from enum import StrEnum


class StormReportPayloadType(StrEnum):
    HAIL = "hail"
    TORNADO = "tornado"
    WIND = "wind"

    def __str__(self) -> str:
        return str(self.value)
