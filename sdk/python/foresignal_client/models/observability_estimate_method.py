from enum import StrEnum


class ObservabilityEstimateMethod(StrEnum):
    CAPTURE_RECAPTURE = "capture-recapture"
    DECLARED = "declared"
    PLATFORM_VOLUME = "platform-volume"
    PRESENCE_PROXY = "presence-proxy"
    RECEIVER_RANGE = "receiver-range"

    def __str__(self) -> str:
        return str(self.value)
