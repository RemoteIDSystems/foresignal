from enum import StrEnum


class SituationLinkDecisionAction(StrEnum):
    ALREADY_MEMBER = "already-member"
    JOIN = "join"
    SEED = "seed"
    SKIP = "skip"

    def __str__(self) -> str:
        return str(self.value)
