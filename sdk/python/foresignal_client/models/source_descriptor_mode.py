from enum import StrEnum


class SourceDescriptorMode(StrEnum):
    DERIVED = "derived"
    POLLING = "polling"
    PUSH = "push"
    STREAMING = "streaming"

    def __str__(self) -> str:
        return str(self.value)
