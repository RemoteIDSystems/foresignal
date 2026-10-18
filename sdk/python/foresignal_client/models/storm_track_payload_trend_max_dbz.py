from enum import StrEnum


class StormTrackPayloadTrendMaxDbz(StrEnum):
    FALLING = "falling"
    RISING = "rising"
    STEADY = "steady"

    def __str__(self) -> str:
        return str(self.value)
