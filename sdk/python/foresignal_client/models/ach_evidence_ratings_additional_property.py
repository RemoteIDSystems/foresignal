from enum import StrEnum


class AchEvidenceRatingsAdditionalProperty(StrEnum):
    C = "C"
    CC = "CC"
    I = "I"
    II = "II"
    N = "N"
    NA = "NA"

    def __str__(self) -> str:
        return str(self.value)
