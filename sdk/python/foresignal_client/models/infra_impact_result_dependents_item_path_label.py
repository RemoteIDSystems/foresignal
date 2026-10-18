from enum import StrEnum


class InfraImpactResultDependentsItemPathLabel(StrEnum):
    CONFIRMED = "confirmed"
    LIKELY = "likely"
    POSSIBLE = "possible"
    SPECULATIVE = "speculative"

    def __str__(self) -> str:
        return str(self.value)
