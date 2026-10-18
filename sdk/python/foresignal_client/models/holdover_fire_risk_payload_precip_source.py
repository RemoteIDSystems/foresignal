from enum import StrEnum


class HoldoverFireRiskPayloadPrecipSource(StrEnum):
    MODEL = "model"
    MRMS = "mrms"

    def __str__(self) -> str:
        return str(self.value)
