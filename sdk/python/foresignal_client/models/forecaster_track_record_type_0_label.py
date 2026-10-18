from enum import StrEnum


class ForecasterTrackRecordType0Label(StrEnum):
    INSUFFICIENT = "insufficient"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)
