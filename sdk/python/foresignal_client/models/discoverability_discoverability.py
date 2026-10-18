from enum import StrEnum


class DiscoverabilityDiscoverability(StrEnum):
    INSTANCE = "instance"
    NETWORK = "network"
    NONE = "none"

    def __str__(self) -> str:
        return str(self.value)
