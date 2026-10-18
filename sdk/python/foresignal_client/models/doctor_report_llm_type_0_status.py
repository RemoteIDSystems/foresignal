from enum import StrEnum


class DoctorReportLlmType0Status(StrEnum):
    BUDGET_REFUSED = "budget-refused"
    FAILED = "failed"
    NO_PROVIDER = "no-provider"
    OFF = "off"
    OK = "ok"
    TIMEOUT = "timeout"

    def __str__(self) -> str:
        return str(self.value)
