from enum import StrEnum


class MeshChannelCommonConfigRegion(StrEnum):
    ANZ = "ANZ"
    EU_433 = "EU_433"
    EU_868 = "EU_868"
    OTHER = "OTHER"
    UK_868 = "UK_868"
    US = "US"

    def __str__(self) -> str:
        return str(self.value)
