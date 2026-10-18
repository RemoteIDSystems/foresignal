from enum import StrEnum


class InfraImpactResultDependentsItemPathItemLabel(StrEnum):
    CONFIRMED = "confirmed"
    LIKELY = "likely"
    POSSIBLE = "possible"
    SPECULATIVE = "speculative"

    def __str__(self) -> str:
        return str(self.value)
