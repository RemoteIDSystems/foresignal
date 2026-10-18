from enum import StrEnum


class CaptureModeMode(StrEnum):
    DOWNSAMPLED = "downsampled"
    EVENTS_ONLY = "events-only"
    FRAMES = "frames"
    FULL = "full"
    LATEST_PER_INTERVAL = "latest-per-interval"
    ROLLUP_ONLY = "rollup-only"

    def __str__(self) -> str:
        return str(self.value)
