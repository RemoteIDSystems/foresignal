from enum import StrEnum


class EdpStreamFieldsObservedAtMode(StrEnum):
    RECEIVED = "received"
    UPSTREAM = "upstream"

    def __str__(self) -> str:
        return str(self.value)
