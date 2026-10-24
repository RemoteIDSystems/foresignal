from enum import StrEnum


class SourceRelevanceVerdict(StrEnum):
    MARGINAL = "marginal"
    NOT_MEANINGFUL = "not-meaningful"
    RELEVANT = "relevant"
    UNRATED = "unrated"

    def __str__(self) -> str:
        return str(self.value)
