from enum import StrEnum


class OutlookWindowExceedanceType1(StrEnum):
    ABOVE_P95 = "above-p95"
    BELOW_P05 = "below-p05"

    def __str__(self) -> str:
        return str(self.value)
