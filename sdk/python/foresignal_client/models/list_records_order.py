from enum import StrEnum


class ListRecordsOrder(StrEnum):
    ID = "id"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)
