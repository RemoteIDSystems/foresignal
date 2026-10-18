from enum import StrEnum


class SavedViewInputTimeField(StrEnum):
    EFFECTIVE = "effective"
    EVENT = "event"
    INGESTED = "ingested"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)
