from enum import StrEnum


class QueryFdsnEventsOrderby(StrEnum):
    MAGNITUDE = "magnitude"
    MAGNITUDE_ASC = "magnitude-asc"
    TIME = "time"
    TIME_ASC = "time-asc"

    def __str__(self) -> str:
        return str(self.value)
