from enum import StrEnum


class GetObservabilityIncludeItem(StrEnum):
    ESTIMATES = "estimates"

    def __str__(self) -> str:
        return str(self.value)
