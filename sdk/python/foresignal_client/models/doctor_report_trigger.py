from enum import StrEnum


class DoctorReportTrigger(StrEnum):
    AUTO = "AUTO"
    MANUAL = "MANUAL"
    MCP = "MCP"

    def __str__(self) -> str:
        return str(self.value)
