from enum import StrEnum


class NarrativeEventChangedItem(StrEnum):
    EVIDENCEGROUPCOUNT = "evidenceGroupCount"
    MERGED = "merged"
    POSTCOUNT = "postCount"
    SPLIT = "split"
    STATE = "state"
    STATUS = "status"
    TITLE = "title"
    VELOCITY = "velocity"

    def __str__(self) -> str:
        return str(self.value)
