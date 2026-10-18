from enum import StrEnum


class NarrativeEventEvent(StrEnum):
    NARRATIVE_EMERGED = "narrative.emerged"
    NARRATIVE_UPDATED = "narrative.updated"

    def __str__(self) -> str:
        return str(self.value)
