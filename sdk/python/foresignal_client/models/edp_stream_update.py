from enum import StrEnum


class EdpStreamUpdate(StrEnum):
    INCREMENTAL = "incremental"
    SNAPSHOT = "snapshot"

    def __str__(self) -> str:
        return str(self.value)
