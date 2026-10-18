from enum import StrEnum


class DoctorReportStatus(StrEnum):
    BROKEN = "BROKEN"
    DEGRADED_INFO = "DEGRADED_INFO"
    OK = "OK"

    def __str__(self) -> str:
        return str(self.value)
