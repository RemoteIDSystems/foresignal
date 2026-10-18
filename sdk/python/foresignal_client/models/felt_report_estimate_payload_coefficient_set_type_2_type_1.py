from enum import StrEnum


class FeltReportEstimatePayloadCoefficientSetType2Type1(StrEnum):
    CALIFORNIA = "california"
    CEUS = "ceus"

    def __str__(self) -> str:
        return str(self.value)
