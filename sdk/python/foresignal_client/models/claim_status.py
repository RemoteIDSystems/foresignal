from enum import StrEnum


class ClaimStatus(StrEnum):
    CONTRADICTED = "contradicted"
    CORROBORATED = "corroborated"
    INSUFFICIENT_COVERAGE = "insufficient-coverage"
    PENDING = "pending"
    UNASSESSED = "unassessed"
    UNOBSERVED = "unobserved"

    def __str__(self) -> str:
        return str(self.value)
