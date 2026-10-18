from enum import StrEnum


class SocialVersusObservedNarrativesItemStatus(StrEnum):
    CONTRADICTED = "contradicted"
    CORROBORATED = "corroborated"
    INSUFFICIENT_COVERAGE = "insufficient-coverage"
    MIXED = "mixed"
    PENDING = "pending"
    UNASSESSED = "unassessed"
    UNOBSERVED = "unobserved"

    def __str__(self) -> str:
        return str(self.value)
