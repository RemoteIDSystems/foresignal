from enum import StrEnum


class FeltReportEstimatePayloadMethod(StrEnum):
    IPE_AW2007 = "ipe-aw2007"
    SHAKEMAP = "shakemap"

    def __str__(self) -> str:
        return str(self.value)
