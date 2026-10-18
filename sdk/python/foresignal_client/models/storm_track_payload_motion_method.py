from enum import StrEnum


class StormTrackPayloadMotionMethod(StrEnum):
    CENTROID_V1 = "centroid-v1"
    TITAN_LITE_V1 = "titan-lite-v1"

    def __str__(self) -> str:
        return str(self.value)
