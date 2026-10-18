from enum import StrEnum


class CustomSourceFormatsAlertmanagerResolved(StrEnum):
    RECORD = "record"
    TOMBSTONE = "tombstone"

    def __str__(self) -> str:
        return str(self.value)
