from enum import StrEnum


class DoctorReportLlmType0Detail(StrEnum):
    COMPACT = "compact"
    FULL = "full"

    def __str__(self) -> str:
        return str(self.value)
