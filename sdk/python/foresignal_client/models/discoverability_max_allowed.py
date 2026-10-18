from enum import StrEnum


class DiscoverabilityMaxAllowed(StrEnum):
    INSTANCE = "instance"
    NETWORK = "network"
    NONE = "none"

    def __str__(self) -> str:
        return str(self.value)
