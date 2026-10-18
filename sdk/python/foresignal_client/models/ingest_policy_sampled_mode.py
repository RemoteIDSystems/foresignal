from enum import StrEnum


class IngestPolicySampledMode(StrEnum):
    SAMPLED = "SAMPLED"

    def __str__(self) -> str:
        return str(self.value)
