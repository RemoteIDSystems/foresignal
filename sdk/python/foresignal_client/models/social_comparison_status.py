from enum import StrEnum


class SocialComparisonStatus(StrEnum):
    CONTRADICTED = "contradicted"
    CORROBORATED = "corroborated"
    INSUFFICIENT_COVERAGE = "insufficient-coverage"
    PENDING = "pending"
    UNOBSERVED = "unobserved"

    def __str__(self) -> str:
        return str(self.value)
