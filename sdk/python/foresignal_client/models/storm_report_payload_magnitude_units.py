from enum import StrEnum


class StormReportPayloadMagnitudeUnits(StrEnum):
    EF = "EF"
    IN = "in"
    MPH = "mph"

    def __str__(self) -> str:
        return str(self.value)
