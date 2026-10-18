from enum import StrEnum


class FireDetectionPayloadConfidenceClass(StrEnum):
    HIGH = "high"
    LOW = "low"
    NOMINAL = "nominal"

    def __str__(self) -> str:
        return str(self.value)
