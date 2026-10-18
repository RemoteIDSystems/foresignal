from enum import StrEnum


class WorkspaceContextPlaybackType0State(StrEnum):
    PAUSED = "paused"
    PLAYING = "playing"

    def __str__(self) -> str:
        return str(self.value)
