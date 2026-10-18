from enum import StrEnum


class SatellitePassPayloadStatus(StrEnum):
    DECODED = "decoded"
    FAILED = "failed"
    MISSED = "missed"
    PREDICTED = "predicted"
    RECEIVING = "receiving"
    RECORDED = "recorded"

    def __str__(self) -> str:
        return str(self.value)
