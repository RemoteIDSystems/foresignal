from enum import StrEnum


class FeltReportEstimatePayloadCoefficientSetType1(StrEnum):
    CALIFORNIA = "california"
    CEUS = "ceus"

    def __str__(self) -> str:
        return str(self.value)
