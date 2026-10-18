from enum import StrEnum


class EdpStreamRetentionClass(StrEnum):
    BULK_EVENT = "bulk-event"
    EVENT = "event"
    REFERENCE = "reference"
    SERIES = "series"
    SOCIAL = "social"
    STATE = "state"
    TRACK = "track"

    def __str__(self) -> str:
        return str(self.value)
