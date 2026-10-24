from enum import StrEnum


class GetIntelCalibrationScopeScope(StrEnum):
    INTEL_EVENT = "intel-event"
    PRECURSOR_ACTIVE = "precursor-active"

    def __str__(self) -> str:
        return str(self.value)
