from enum import StrEnum


class QueryHistoryTimeField(StrEnum):
    EFFECTIVE = "effective"
    EVENT = "event"
    INGESTED = "ingested"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)
